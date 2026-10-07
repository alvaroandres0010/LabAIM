package cl.labaim.core.telemetry;

import cl.labaim.core.model.Camera;
import cl.labaim.core.model.Target;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Registra la sesion y la vuelca a dos CSV.
 *
 * <p>{@code frames.csv} es la trayectoria de la mira muestreada a cada frame.
 * {@code events.csv} son las apariciones y los disparos. Juntos contienen todo lo que
 * necesita el analisis posterior: tiempo de reaccion, overshoot de flick, eficiencia
 * de trayecto y sesgo direccional salen de cruzar ambos archivos.
 *
 * <p>Nada se escribe a disco durante la partida. La escritura ocurre al cerrar la
 * sesion, para que ningun I/O se meta en el bucle de render.
 */
public final class TelemetryRecorder {

    private static final String FRAME_HEADER = "t_ns,yaw_deg,pitch_deg,nearest_target_id";

    private final FrameBuffer frames;
    private final List<GameEvent> events = new ArrayList<>();
    private final String sessionId;

    public TelemetryRecorder(String sessionId, double durationSeconds, int expectedFps) {
        this.sessionId = sessionId;
        this.frames = FrameBuffer.forSession(durationSeconds, expectedFps);
    }

    public void recordFrame(long tNanos, Camera cam, Target nearest) {
        frames.add(tNanos, cam.yawDeg(), cam.pitchDeg(), nearest == null ? 0 : nearest.id());
    }

    public void recordEvent(long tNanos, EventType type, Camera cam, Target target, double errorDeg) {
        events.add(new GameEvent(
                tNanos, type,
                target == null ? 0 : target.id(),
                target == null ? Double.NaN : target.yawDeg(),
                target == null ? Double.NaN : target.pitchDeg(),
                cam == null ? Double.NaN : cam.yawDeg(),
                cam == null ? Double.NaN : cam.pitchDeg(),
                errorDeg));
    }

    public FrameBuffer frames() { return frames; }
    public List<GameEvent> events() { return events; }
    public String sessionId() { return sessionId; }

    /**
     * Escribe los dos CSV dentro de {@code baseDir/sessionId/}.
     *
     * @return la carpeta creada
     */
    public Path writeCsv(Path baseDir) throws IOException {
        Path dir = baseDir.resolve(sessionId);
        Files.createDirectories(dir);

        try (BufferedWriter w = Files.newBufferedWriter(
                dir.resolve("frames.csv"), StandardCharsets.UTF_8)) {
            w.write(FRAME_HEADER);
            w.newLine();
            StringBuilder sb = new StringBuilder(64);
            for (int i = 0; i < frames.size(); i++) {
                sb.setLength(0);
                sb.append(frames.tNanos(i)).append(',')
                  .append(String.format(Locale.ROOT, "%.6f", frames.yawDeg(i))).append(',')
                  .append(String.format(Locale.ROOT, "%.6f", frames.pitchDeg(i))).append(',')
                  .append(frames.nearestTargetId(i));
                w.write(sb.toString());
                w.newLine();
            }
        }

        try (BufferedWriter w = Files.newBufferedWriter(
                dir.resolve("events.csv"), StandardCharsets.UTF_8)) {
            w.write(GameEvent.CSV_HEADER);
            w.newLine();
            for (GameEvent e : events) {
                w.write(e.toCsvRow());
                w.newLine();
            }
        }

        return dir;
    }
}
