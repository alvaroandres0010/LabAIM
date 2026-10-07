package cl.labaim.core.math;

import cl.labaim.core.model.Camera;
import cl.labaim.core.model.Target;

/**
 * Deteccion de impacto.
 *
 * <p>Deliberadamente NO compara distancias en pixeles contra el circulo dibujado.
 * Comparar angulos es exacto, no arrastra el error de la proyeccion, no depende de
 * la resolucion ni del FOV, y de paso entrega gratis el error angular del disparo,
 * que es el dato que alimenta todo el analisis posterior.
 */
public final class HitTest {

    private HitTest() {
    }

    /** Angulo entre la mira y el centro del objetivo, en grados. Cero es un disparo perfecto. */
    public static double angularErrorDeg(Camera cam, Target target) {
        return Angles.between(cam.forward(), target.direction());
    }

    /** true si la mira cae dentro del objetivo. */
    public static boolean hits(Camera cam, Target target) {
        return angularErrorDeg(cam, target) <= target.angularRadiusDeg();
    }

    /**
     * Error normalizado por el tamano del objetivo: 0 es el centro, 1 es justo el borde,
     * mas de 1 es fallo. Permite comparar precision entre escenarios con objetivos de
     * distinto tamano.
     */
    public static double normalizedError(Camera cam, Target target) {
        return angularErrorDeg(cam, target) / target.angularRadiusDeg();
    }
}
