package cl.labaim.core.model;

import cl.labaim.core.math.GameProfile;
import cl.labaim.core.math.Sensitivity;
import cl.labaim.core.math.Vec3;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CameraTest {

    private static final double EPS = 1e-12;

    @Test
    @DisplayName("100.000 updates de ida y vuelta devuelven el yaw exacto a cero")
    void sinDerivaAcumulada() {
        Camera cam = new Camera();
        double dpc = new Sensitivity(GameProfile.VALORANT, 0.643, 800).degreesPerCount();
        for (int i = 0; i < 50_000; i++) cam.applyMouseDelta(7, 0, dpc);
        for (int i = 0; i < 50_000; i++) cam.applyMouseDelta(-7, 0, dpc);
        // Con float este test falla: el error acumulado descalibra la sensibilidad.
        assertEquals(0.0, cam.yawDeg(), 1e-9);
    }

    @Test
    @DisplayName("mover counts_por_360 counts gira exactamente 360 grados")
    void giroCompleto() {
        Camera cam = new Camera();
        Sensitivity s = new Sensitivity(GameProfile.VALORANT, 1.0, 800);
        cam.applyMouseDelta(s.countsPer360(), 0, s.degreesPerCount());
        assertEquals(360.0, cam.yawDeg(), 1e-9);
    }

    @Test
    @DisplayName("el pitch se limita a +/- 89 grados")
    void pitchLimitado() {
        Camera cam = new Camera();
        cam.applyMouseDelta(0, -1_000_000, 0.07);
        assertEquals(Camera.MAX_PITCH_DEG, cam.pitchDeg(), EPS);
        cam.applyMouseDelta(0, 1_000_000, 0.07);
        assertEquals(-Camera.MAX_PITCH_DEG, cam.pitchDeg(), EPS);
    }

    @Test
    @DisplayName("raton a la derecha aumenta el yaw, raton abajo baja el pitch")
    void direccionDelInput() {
        Camera cam = new Camera();
        cam.applyMouseDelta(100, 0, 0.07);
        assertTrue(cam.yawDeg() > 0);
        cam.setOrientation(0, 0);
        cam.applyMouseDelta(0, 100, 0.07);
        assertTrue(cam.pitchDeg() < 0, "mover el raton hacia abajo debe mirar hacia abajo");
    }

    @Test
    @DisplayName("(right, up, forward) es ortonormal en cualquier orientacion")
    void baseOrtonormal() {
        for (double yaw = -350; yaw <= 350; yaw += 37) {
            for (double pitch = -85; pitch <= 85; pitch += 17) {
                Camera c = new Camera(yaw, pitch);
                Vec3 f = c.forward(), r = c.right(), u = c.up();
                assertEquals(0.0, f.dot(r), EPS, "forward . right");
                assertEquals(0.0, f.dot(u), EPS, "forward . up");
                assertEquals(0.0, r.dot(u), EPS, "right . up");
                assertEquals(1.0, f.length(), EPS);
                assertEquals(1.0, r.length(), EPS);
                assertEquals(1.0, u.length(), EPS);
            }
        }
    }

    @Test
    @DisplayName("en reposo la camara mira a +Z con up en +Y")
    void orientacionDeReposo() {
        Camera c = new Camera(0, 0);
        assertEquals(1.0, c.forward().z(), EPS);
        assertEquals(1.0, c.up().y(), EPS);
        assertEquals(1.0, c.right().x(), EPS);
    }
}
