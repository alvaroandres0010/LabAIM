package cl.labaim.core.math;

import cl.labaim.core.model.Camera;
import cl.labaim.core.model.Target;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HitTestTest {

    private final Target target = new Target(1, 10.0, 5.0, 1.1, 0L);

    @Test
    @DisplayName("apuntar al centro acierta con error cero")
    void centro() {
        Camera cam = new Camera(10.0, 5.0);
        assertTrue(HitTest.hits(cam, target));
        assertEquals(0.0, HitTest.angularErrorDeg(cam, target), 1e-9);
    }

    @Test
    @DisplayName("el borde del objetivo separa acierto de fallo con precision de 1e-7 grados")
    void borde() {
        assertTrue(HitTest.hits(new Camera(10.0, 5.0 + 1.1 - 1e-7), target));
        assertFalse(HitTest.hits(new Camera(10.0, 5.0 + 1.1 + 1e-7), target));
    }

    @Test
    @DisplayName("el error normalizado vale 1 exactamente en el borde")
    void errorNormalizado() {
        assertEquals(1.0, HitTest.normalizedError(new Camera(10.0, 5.0 + 1.1), target), 1e-6);
        assertEquals(0.0, HitTest.normalizedError(new Camera(10.0, 5.0), target), 1e-9);
    }

    @Test
    @DisplayName("atan2 mantiene la precision donde acos la pierde")
    void precisionEnAngulosDiminutos() {
        Vec3 a = Angles.direction(0, 0);
        Vec3 b = Angles.direction(1e-5, 0);
        assertEquals(1e-5, Angles.between(a, b), 1e-12);

        // acos(dot) para el mismo angulo pierde varios digitos significativos.
        double viaAcos = Math.toDegrees(Math.acos(Math.min(1.0, a.dot(b))));
        assertTrue(Math.abs(viaAcos - 1e-5) > 1e-9,
                "este test documenta por que NO usamos acos");
    }

    @Test
    @DisplayName("el error es simetrico respecto al centro del objetivo")
    void simetria() {
        double arriba = HitTest.angularErrorDeg(new Camera(10.0, 5.0 + 0.5), target);
        double abajo = HitTest.angularErrorDeg(new Camera(10.0, 5.0 - 0.5), target);
        assertEquals(arriba, abajo, 1e-9);
    }
}
