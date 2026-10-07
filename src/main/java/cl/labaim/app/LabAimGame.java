package cl.labaim.app;

import cl.labaim.core.math.Sensitivity;
import cl.labaim.core.model.Camera;
import cl.labaim.core.model.Target;
import cl.labaim.core.model.Viewport;
import cl.labaim.core.scenario.GridShot;
import cl.labaim.core.session.Session;
import cl.labaim.core.session.SessionStats;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.ScreenUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Bucle principal.
 *
 * <p>Toda la logica vive en {@code cl.labaim.core}, que no depende de libGDX. Esta
 * clase solo traduce: deltas del raton a la camara, clics a disparos, y estado a
 * pixeles.
 */
public final class LabAimGame extends ApplicationAdapter {

    private enum Estado { ESPERANDO, JUGANDO, RESULTADOS }

    private static final DateTimeFormatter ID_SESION =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    private static final Path CARPETA_SESIONES = Path.of("sessions");

    private final Settings settings;

    private Camera camera;
    private Renderer renderer;
    private Hud hud;
    private Viewport viewport;
    private Sensitivity sensitivity;

    private Estado estado = Estado.ESPERANDO;
    private Session session;
    private String ultimaRutaCsv = "(sin guardar)";
    private SessionStats ultimasStats = SessionStats.EMPTY;

    /** Se cachea porque no cambia durante la ronda y se usa en cada frame. */
    private double degreesPerCount;

    public LabAimGame(Settings settings) {
        this.settings = settings;
    }

    @Override
    public void create() {
        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();

        camera = new Camera();
        sensitivity = settings.sensitivity();
        degreesPerCount = sensitivity.degreesPerCount() * settings.countScale;
        viewport = Viewport.fromHorizontalFov(w, h, settings.horizontalFovDeg);
        renderer = new Renderer(w, h);
        hud = new Hud(w, h);

        RawMouse.enable();

        System.out.println("[LabAIM] " + sensitivity);
        System.out.printf("[LabAIM] FOV horizontal %.1f, vertical %.1f%n",
                viewport.horizontalFovDeg(), Math.toDegrees(viewport.vFovRad()));
    }

    @Override
    public void resize(int width, int height) {
        if (width == 0 || height == 0) return;
        viewport = Viewport.fromHorizontalFov(width, height, settings.horizontalFovDeg);
        renderer.resize(width, height);
        hud.resize(width, height);
    }

    @Override
    public void render() {
        long now = System.nanoTime();

        actualizarCamara();
        procesarTeclas(now);

        ScreenUtils.clear(renderer.colorFondo());

        switch (estado) {
            case ESPERANDO -> {
                hud.drawIdle("LabAIM", "ESPACIO para empezar   ESC para salir",
                        sensitivity, settings.horizontalFovDeg);
            }
            case JUGANDO -> {
                session.update(now);
                if (session.state() == Session.State.FINISHED) {
                    cerrarRonda();
                    break;
                }
                if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
                    session.shoot(now);
                }
                Target cercano = session.scenario().nearestTarget(camera);
                renderer.draw(camera, session.scenario().activeTargets(), viewport, cercano);
                hud.drawRunning(session.stats(), session.remainingSeconds(now));
            }
            case RESULTADOS -> hud.drawResults(ultimasStats, ultimaRutaCsv);
        }
    }

    private void actualizarCamara() {
        int dx = Gdx.input.getDeltaX();
        int dy = Gdx.input.getDeltaY();
        if (dx != 0 || dy != 0) {
            camera.applyMouseDelta(dx, dy, degreesPerCount);
        }
    }

    private void procesarTeclas(long now) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
            return;
        }
        boolean quiereEmpezar = Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
                || Gdx.input.isKeyJustPressed(Input.Keys.R);
        if (quiereEmpezar && estado != Estado.JUGANDO) {
            empezarRonda(now);
        }
    }

    private void empezarRonda(long now) {
        String id = LocalDateTime.now().format(ID_SESION);
        GridShot escenario = new GridShot(settings.scenarioConfig(System.nanoTime()));
        int fpsEsperado = Math.max(60, Gdx.graphics.getFramesPerSecond());
        session = new Session(escenario, camera, id, fpsEsperado);
        session.start(now);
        estado = Estado.JUGANDO;
    }

    private void cerrarRonda() {
        ultimasStats = session.stats();
        try {
            Path dir = session.telemetry().writeCsv(CARPETA_SESIONES);
            ultimaRutaCsv = dir.toAbsolutePath().toString();
            System.out.println("[LabAIM] Telemetria guardada en " + ultimaRutaCsv);
        } catch (IOException e) {
            ultimaRutaCsv = "ERROR al guardar: " + e.getMessage();
            System.err.println("[LabAIM] " + ultimaRutaCsv);
        }
        estado = Estado.RESULTADOS;
    }

    @Override
    public void dispose() {
        RawMouse.disable();
        if (renderer != null) renderer.dispose();
        if (hud != null) hud.dispose();
    }
}
