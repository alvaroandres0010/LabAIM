package cl.labaim.core.session;

import cl.labaim.core.model.Camera;
import cl.labaim.core.model.Target;
import cl.labaim.core.scenario.GridShot;
import cl.labaim.core.scenario.ScenarioConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * La sesion se ejercita inyectando instantes de nanoTime a mano. No hace falta abrir
 * una ventana ni esperar 60 segundos reales para testear una ronda de 60 segundos.
 */
class SessionTest {

    private static final long UN_SEGUNDO = 1_000_000_000L;
    private static final int FPS = 240;
    private static final long FRAME = UN_SEGUNDO / FPS;

    private record Montaje(Session session, GridShot escenario, Camera camara) {
    }

    private static Montaje montar(long semilla) {
        Camera cam = new Camera();
        GridShot gs = new GridShot(ScenarioConfig.gridshot(semilla));
        return new Montaje(new Session(gs, cam, "test-" + semilla, FPS), gs, cam);
    }

    /** Simula una ronda disparando cada 0.25 s, alternando acierto y fallo deliberado. */
    private static Montaje simularRonda(long semilla) {
        Montaje m = montar(semilla);
        long t0 = UN_SEGUNDO;
        m.session.start(t0);
        int disparo = 0;
        for (long f = 0; f < (long) FPS * 60; f++) {
            long t = t0 + f * FRAME;
            m.session.update(t);
            if (m.session.state() != Session.State.RUNNING) break;
            if (f % 60 == 0) {
                Target objetivo = m.escenario.nearestTarget(m.camara);
                if (objetivo != null) {
                    // 2.25 grados es justo la mitad entre dos celdas: falla sin clipear al vecino.
                    double desvio = (disparo % 2 == 0) ? 0.2 : 2.25;
                    m.camara.setOrientation(objetivo.yawDeg() + desvio, objetivo.pitchDeg());
                    m.session.shoot(t);
                    disparo++;
                }
            }
        }
        m.session.finish(t0 + 60 * UN_SEGUNDO);
        return m;
    }

    @Test
    @DisplayName("una ronda simulada produce estadisticas coherentes")
    void estadisticas() {
        SessionStats st = simularRonda(99L).session.stats();
        assertEquals(240, st.shots());
        assertEquals(0.5, st.accuracy(), 0.02, "la simulacion falla uno de cada dos a proposito");
        assertEquals(st.shots() - st.hits(), st.misses());
        assertEquals(60.0, st.elapsedSeconds(), 0.01);
        assertTrue(st.avgTimeToHitMs() > 0);
    }

    @Test
    @DisplayName("se graba una muestra de telemetria por frame")
    void telemetriaPorFrame() {
        Montaje m = simularRonda(5L);
        assertEquals(FPS * 60, m.session.telemetry().frames().size(), 2);
    }

    @Test
    @DisplayName("cada impacto genera su evento y el SPAWN del reemplazo")
    void eventos() {
        Montaje m = simularRonda(5L);
        long hits = m.session.telemetry().events().stream()
                .filter(e -> e.type() == cl.labaim.core.telemetry.EventType.SHOT_HIT).count();
        long spawns = m.session.telemetry().events().stream()
                .filter(e -> e.type() == cl.labaim.core.telemetry.EventType.SPAWN).count();
        // Los 3 spawns iniciales mas uno por cada impacto.
        assertEquals(hits + 3, spawns);
    }

    @Test
    @DisplayName("los CSV se escriben completos y con cabecera")
    void escrituraCsv(@TempDir Path tmp) throws IOException {
        Montaje m = simularRonda(5L);
        Path dir = m.session.telemetry().writeCsv(tmp);

        List<String> frames = Files.readAllLines(dir.resolve("frames.csv"));
        List<String> events = Files.readAllLines(dir.resolve("events.csv"));

        assertEquals("t_ns,yaw_deg,pitch_deg,nearest_target_id", frames.get(0));
        assertEquals(m.session.telemetry().frames().size() + 1, frames.size());
        assertEquals(m.session.telemetry().events().size() + 1, events.size());
        assertTrue(events.get(1).contains("SESSION_START"));
        // Punto decimal con punto y no coma: si no, el CSV queda roto en locales como es-CL.
        assertTrue(frames.get(1).matches("\\d+,-?\\d+\\.\\d+,-?\\d+\\.\\d+,\\d+"), frames.get(1));
    }

    @Test
    @DisplayName("la ronda termina sola al vencer el tiempo")
    void terminaSola() {
        Montaje m = montar(1L);
        long t0 = UN_SEGUNDO;
        m.session.start(t0);
        assertEquals(Session.State.RUNNING, m.session.state());
        m.session.update(t0 + 59 * UN_SEGUNDO);
        assertEquals(Session.State.RUNNING, m.session.state());
        m.session.update(t0 + 60 * UN_SEGUNDO);
        assertEquals(Session.State.FINISHED, m.session.state());
    }

    @Test
    @DisplayName("disparar fuera de una ronda activa no hace nada")
    void disparoFueraDeRonda() {
        Montaje m = montar(1L);
        assertFalse(m.session.shoot(0L).hit());
        assertEquals(0, m.session.stats().shots());
    }

    @Test
    @DisplayName("no se puede iniciar dos veces la misma sesion")
    void doblesArranques() {
        Montaje m = montar(1L);
        m.session.start(UN_SEGUNDO);
        assertThrows(IllegalStateException.class, () -> m.session.start(UN_SEGUNDO));
    }

    @Test
    @DisplayName("el tiempo restante baja de 60 a 0")
    void tiempoRestante() {
        Montaje m = montar(1L);
        long t0 = UN_SEGUNDO;
        m.session.start(t0);
        assertEquals(60.0, m.session.remainingSeconds(t0), 1e-9);
        assertEquals(30.0, m.session.remainingSeconds(t0 + 30 * UN_SEGUNDO), 1e-9);
        assertEquals(0.0, m.session.remainingSeconds(t0 + 90 * UN_SEGUNDO), 1e-9);
    }
}
