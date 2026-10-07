package cl.labaim.core.math;

import cl.labaim.core.model.Camera;
import cl.labaim.core.model.Target;
import cl.labaim.core.model.Viewport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProjectionTest {

    private final Viewport vp = Viewport.fromHorizontalFov(1920, 1080, 103.0);
    private final Camera cam = new Camera(0, 0);

    private static Target at(double yaw, double pitch) {
        return new Target(1, yaw, pitch, 1.1, 0L);
    }

    @Test
    @DisplayName("un objetivo justo al frente cae en el pixel central")
    void objetivoCentrado() {
        var p = Projection.project(cam, at(0, 0), vp);
        assertTrue(p.visible());
        assertEquals(960.0, p.xPx(), 1e-9);
        assertEquals(540.0, p.yPx(), 1e-9);
    }

    @Test
    @DisplayName("el FOV horizontal sobrevive la conversion a vertical y vuelta")
    void fovIdaYVuelta() {
        assertEquals(103.0, vp.horizontalFovDeg(), 1e-9);
        assertEquals(70.53, Math.toDegrees(vp.vFovRad()), 0.01);
    }

    @Test
    @DisplayName("yaw positivo proyecta a la derecha, y es simetrico")
    void simetria() {
        var der = Projection.project(cam, at(10, 0), vp);
        var izq = Projection.project(cam, at(-10, 0), vp);
        assertTrue(der.xPx() > vp.centerX());
        assertTrue(izq.xPx() < vp.centerX());
        assertEquals(der.xPx() - vp.centerX(), vp.centerX() - izq.xPx(), 1e-9);
    }

    @Test
    @DisplayName("el desplazamiento sigue la tangente, no es lineal")
    void leyDeLaTangente() {
        double d5 = Projection.project(cam, at(5, 0), vp).xPx() - vp.centerX();
        double d10 = Projection.project(cam, at(10, 0), vp).xPx() - vp.centerX();
        double esperado = Math.tan(Math.toRadians(10)) / Math.tan(Math.toRadians(5));
        assertEquals(esperado, d10 / d5, 1e-9);
        // Si esto fuese un plano 2D el cociente seria 2.0 y el entrenamiento no transferiria.
        assertNotEquals(2.0, d10 / d5, 0.01);
    }

    @Test
    @DisplayName("lo que queda detras de la camara no se dibuja")
    void detrasNoVisible() {
        assertFalse(Projection.project(cam, at(180, 0), vp).visible());
        assertFalse(Projection.project(cam, at(91, 0), vp).visible());
    }

    @Test
    @DisplayName("el radio en pantalla crece al alejarse del centro")
    void radioCreceEnLosBordes() {
        double rCentro = Projection.project(cam, at(0, 0), vp).radiusPx();
        double rBorde = Projection.project(cam, at(40, 0), vp).radiusPx();
        assertTrue(rBorde > rCentro, "la perspectiva estira los objetivos lejos del centro");
    }
}
