package cl.labaim.core.scenario;

import cl.labaim.core.math.Angles;
import cl.labaim.core.math.HitTest;
import cl.labaim.core.model.Camera;
import cl.labaim.core.model.Target;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Gridshot: varios objetivos vivos sobre una rejilla de posiciones fijas. Al impactar
 * uno, reaparece en otra celda libre elegida al azar.
 *
 * <p>Es el escenario clasico de clicking y el que mas datos de flick genera por minuto.
 */
public final class GridShot implements Scenario {

    private final ScenarioConfig config;
    private final Random rng;

    /** Posiciones (yaw, pitch) de cada celda, precalculadas en start(). */
    private double[] cellYaw;
    private double[] cellPitch;

    /** Para cada celda, el indice en activeTargets o -1 si esta libre. */
    private int[] occupantIndex;

    private final List<Target> active = new ArrayList<>();
    private final List<Target> activeView = Collections.unmodifiableList(active);
    private final List<Integer> activeCell = new ArrayList<>();

    private int nextTargetId = 1;

    public GridShot(ScenarioConfig config) {
        this.config = config;
        this.rng = new Random(config.seed());
    }

    @Override
    public String name() {
        return "Gridshot";
    }

    @Override
    public ScenarioConfig config() {
        return config;
    }

    @Override
    public void start(long nowNanos) {
        int cols = config.gridCols();
        int rows = config.gridRows();
        int cells = cols * rows;

        cellYaw = new double[cells];
        cellPitch = new double[cells];
        occupantIndex = new int[cells];

        // Rejilla centrada en (0,0): con 5 columnas los offsets son -2,-1,0,1,2.
        double halfCols = (cols - 1) / 2.0;
        double halfRows = (rows - 1) / 2.0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int i = r * cols + c;
                cellYaw[i] = (c - halfCols) * config.spacingDeg();
                cellPitch[i] = (r - halfRows) * config.spacingDeg();
                occupantIndex[i] = -1;
            }
        }

        active.clear();
        activeCell.clear();
        nextTargetId = 1;

        for (int i = 0; i < config.simultaneousTargets(); i++) {
            spawnInFreeCell(nowNanos);
        }
    }

    @Override
    public List<Target> activeTargets() {
        return activeView;
    }

    @Override
    public void update(long nowNanos) {
        // Gridshot es estatico: los objetivos solo cambian al ser impactados.
    }

    @Override
    public ShotResult onShot(Camera cam, long nowNanos) {
        if (active.isEmpty()) {
            return ShotResult.NOTHING;
        }

        int bestIdx = nearestIndex(cam);
        Target best = active.get(bestIdx);
        double error = HitTest.angularErrorDeg(cam, best);

        if (error <= best.angularRadiusDeg()) {
            long ttk = nowNanos - best.spawnNanos();
            removeAt(bestIdx);
            spawnInFreeCell(nowNanos);
            return new ShotResult(true, best, error, ttk);
        }
        return new ShotResult(false, best, error, nowNanos - best.spawnNanos());
    }

    @Override
    public Target nearestTarget(Camera cam) {
        if (active.isEmpty()) return null;
        return active.get(nearestIndex(cam));
    }

    private int nearestIndex(Camera cam) {
        int best = 0;
        double bestErr = Double.MAX_VALUE;
        for (int i = 0; i < active.size(); i++) {
            double err = HitTest.angularErrorDeg(cam, active.get(i));
            if (err < bestErr) {
                bestErr = err;
                best = i;
            }
        }
        return best;
    }

    private void removeAt(int idx) {
        int cell = activeCell.get(idx);
        occupantIndex[cell] = -1;

        int last = active.size() - 1;
        active.set(idx, active.get(last));
        activeCell.set(idx, activeCell.get(last));
        active.remove(last);
        activeCell.remove(last);

        // El elemento movido cambio de indice; hay que reflejarlo en la tabla de celdas.
        if (idx < active.size()) {
            occupantIndex[activeCell.get(idx)] = idx;
        }
    }

    private void spawnInFreeCell(long nowNanos) {
        int cells = cellYaw.length;
        int free = cells - active.size();
        if (free <= 0) return;

        // Elegimos el n-esimo hueco libre en vez de reintentar al azar: tiempo acotado.
        int pick = rng.nextInt(free);
        int chosen = -1;
        for (int i = 0, seen = 0; i < cells; i++) {
            if (occupantIndex[i] == -1) {
                if (seen == pick) { chosen = i; break; }
                seen++;
            }
        }
        if (chosen < 0) return;

        Target t = new Target(nextTargetId++, cellYaw[chosen], cellPitch[chosen],
                config.targetRadiusDeg(), nowNanos);
        active.add(t);
        activeCell.add(chosen);
        occupantIndex[chosen] = active.size() - 1;
    }

    /** Angulo que ocupa la rejilla completa, util para encuadrar el FOV. */
    public double gridWidthDeg() {
        return (config.gridCols() - 1) * config.spacingDeg();
    }

    /** Distancia angular entre dos celdas cualesquiera, para validar la rejilla. */
    public double angleBetweenCells(int a, int b) {
        return Angles.between(cellYaw[a], cellPitch[a], cellYaw[b], cellPitch[b]);
    }
}
