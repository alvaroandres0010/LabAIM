package cl.labaim.core.scenario;

/**
 * Parametros de un escenario.
 *
 * @param gridCols          columnas de la rejilla de posiciones posibles
 * @param gridRows          filas de la rejilla
 * @param spacingDeg        separacion angular entre celdas, en grados
 * @param simultaneousTargets cuantos objetivos hay vivos a la vez
 * @param targetRadiusDeg   radio aparente de cada objetivo, en grados
 * @param durationSeconds   duracion de la ronda
 * @param seed              semilla del generador; fijala para que una sesion sea reproducible
 */
public record ScenarioConfig(int gridCols, int gridRows, double spacingDeg,
                             int simultaneousTargets, double targetRadiusDeg,
                             double durationSeconds, long seed) {

    public ScenarioConfig {
        if (gridCols < 1 || gridRows < 1) {
            throw new IllegalArgumentException("La rejilla debe tener al menos 1x1");
        }
        if (simultaneousTargets < 1 || simultaneousTargets > gridCols * gridRows) {
            throw new IllegalArgumentException(
                    "simultaneousTargets debe estar entre 1 y " + (gridCols * gridRows));
        }
        if (spacingDeg <= 0) throw new IllegalArgumentException("spacingDeg debe ser > 0");
        if (durationSeconds <= 0) throw new IllegalArgumentException("durationSeconds debe ser > 0");
    }

    /** Preset tipo Gridshot: rejilla 5x5, 3 objetivos vivos, 60 segundos. */
    public static ScenarioConfig gridshot(long seed) {
        return new ScenarioConfig(5, 5, 4.5, 3, 1.1, 60.0, seed);
    }

    /** Preset mas exigente: objetivos mas chicos y mas separados. */
    public static ScenarioConfig microshot(long seed) {
        return new ScenarioConfig(6, 6, 5.0, 2, 0.6, 60.0, seed);
    }

    public long durationNanos() {
        return (long) (durationSeconds * 1_000_000_000L);
    }
}
