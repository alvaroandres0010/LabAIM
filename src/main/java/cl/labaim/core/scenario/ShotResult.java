package cl.labaim.core.scenario;

import cl.labaim.core.model.Target;

/**
 * Resultado de un disparo.
 *
 * @param hit              si impacto algun objetivo
 * @param target           el objetivo impactado, o el mas cercano si fallo; nunca null si habia objetivos
 * @param angularErrorDeg  angulo entre la mira y el centro de ese objetivo
 * @param timeToHitNanos   tiempo desde que aparecio ese objetivo hasta este disparo
 */
public record ShotResult(boolean hit, Target target,
                         double angularErrorDeg, long timeToHitNanos) {

    public static final ShotResult NOTHING = new ShotResult(false, null, Double.NaN, 0L);
}
