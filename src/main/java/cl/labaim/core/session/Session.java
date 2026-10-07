package cl.labaim.core.session;

import cl.labaim.core.math.HitTest;
import cl.labaim.core.model.Camera;
import cl.labaim.core.model.Target;
import cl.labaim.core.scenario.Scenario;
import cl.labaim.core.scenario.ShotResult;
import cl.labaim.core.telemetry.EventType;
import cl.labaim.core.telemetry.TelemetryRecorder;

import java.util.List;

/**
 * Una ronda completa: arranca el escenario, cuenta el tiempo, acumula estadisticas y
 * alimenta la telemetria.
 *
 * <p>No depende de libGDX. Toda la logica de juego se puede ejercitar desde un test
 * inyectando instantes de {@code nanoTime} a mano, que es como estan escritos los
 * tests de este proyecto.
 */
public final class Session {

    public enum State { READY, RUNNING, FINISHED }

    private final Scenario scenario;
    private final Camera camera;
    private final TelemetryRecorder telemetry;

    private State state = State.READY;
    private long startNanos;
    private long endNanos;

    private int shots;
    private int hits;
    private long totalTimeToHitNanos;
    private double totalHitErrorDeg;

    public Session(Scenario scenario, Camera camera, String sessionId, int expectedFps) {
        this.scenario = scenario;
        this.camera = camera;
        this.telemetry = new TelemetryRecorder(
                sessionId, scenario.config().durationSeconds(), expectedFps);
    }

    public void start(long nowNanos) {
        if (state != State.READY) {
            throw new IllegalStateException("La sesion ya fue iniciada");
        }
        state = State.RUNNING;
        startNanos = nowNanos;
        camera.reset();
        scenario.start(nowNanos);

        telemetry.recordEvent(nowNanos, EventType.SESSION_START, camera, null, Double.NaN);
        for (Target t : scenario.activeTargets()) {
            telemetry.recordEvent(nowNanos, EventType.SPAWN, camera, t, Double.NaN);
        }
    }

    /** Llamar una vez por frame. Registra la muestra de telemetria y cierra la ronda al vencer el tiempo. */
    public void update(long nowNanos) {
        if (state != State.RUNNING) return;

        scenario.update(nowNanos);
        telemetry.recordFrame(nowNanos, camera, scenario.nearestTarget(camera));

        if (nowNanos - startNanos >= scenario.config().durationNanos()) {
            finish(nowNanos);
        }
    }

    /** Procesa un clic. Devuelve el resultado para que la capa grafica reaccione. */
    public ShotResult shoot(long nowNanos) {
        if (state != State.RUNNING) return ShotResult.NOTHING;

        List<Target> before = scenario.activeTargets();
        int countBefore = before.size();

        ShotResult result = scenario.onShot(camera, nowNanos);
        shots++;

        if (result.hit()) {
            hits++;
            totalTimeToHitNanos += result.timeToHitNanos();
            totalHitErrorDeg += result.angularErrorDeg();
            telemetry.recordEvent(nowNanos, EventType.SHOT_HIT, camera,
                    result.target(), result.angularErrorDeg());

            // El objetivo impactado se reemplaza por uno nuevo; hay que registrar su aparicion.
            if (scenario.activeTargets().size() == countBefore) {
                Target spawned = newestTarget();
                if (spawned != null) {
                    telemetry.recordEvent(nowNanos, EventType.SPAWN, camera, spawned, Double.NaN);
                }
            }
        } else {
            telemetry.recordEvent(nowNanos, EventType.SHOT_MISS, camera,
                    result.target(), result.angularErrorDeg());
        }
        return result;
    }

    private Target newestTarget() {
        Target newest = null;
        for (Target t : scenario.activeTargets()) {
            if (newest == null || t.id() > newest.id()) newest = t;
        }
        return newest;
    }

    public void finish(long nowNanos) {
        if (state != State.RUNNING) return;
        state = State.FINISHED;
        endNanos = nowNanos;
        telemetry.recordEvent(nowNanos, EventType.SESSION_END, camera, null, Double.NaN);
    }

    public SessionStats stats() {
        if (state == State.READY) return SessionStats.EMPTY;
        long ref = state == State.FINISHED ? endNanos : lastFrameNanos();
        double elapsed = (ref - startNanos) / 1_000_000_000.0;
        int misses = shots - hits;
        double accuracy = shots > 0 ? hits / (double) shots : 0.0;
        double avgTtk = hits > 0 ? totalTimeToHitNanos / (double) hits / 1_000_000.0 : 0.0;
        double avgErr = hits > 0 ? totalHitErrorDeg / hits : 0.0;
        return new SessionStats(shots, hits, misses, accuracy, avgTtk, avgErr, elapsed);
    }

    private long lastFrameNanos() {
        int n = telemetry.frames().size();
        return n > 0 ? telemetry.frames().tNanos(n - 1) : startNanos;
    }

    /** Segundos que quedan de ronda. */
    public double remainingSeconds(long nowNanos) {
        if (state != State.RUNNING) return 0.0;
        long left = scenario.config().durationNanos() - (nowNanos - startNanos);
        return Math.max(0.0, left / 1_000_000_000.0);
    }

    public State state() { return state; }
    public Scenario scenario() { return scenario; }
    public Camera camera() { return camera; }
    public TelemetryRecorder telemetry() { return telemetry; }

    /** Error angular del disparo si se disparara ahora mismo. Util para depurar. */
    public double currentAimErrorDeg() {
        Target t = scenario.nearestTarget(camera);
        return t == null ? Double.NaN : HitTest.angularErrorDeg(camera, t);
    }
}
