package cl.labaim.core.model;

import cl.labaim.core.math.Angles;
import cl.labaim.core.math.Vec3;

/**
 * Un objetivo. Vive en una direccion angular alrededor del jugador, no en un plano.
 *
 * <p>El tamano se guarda como radio angular (los grados que ocupa visto desde la
 * camara) y no como radio en metros. Asi el tamano aparente no depende de ninguna
 * distancia arbitraria y se puede comparar directamente con el error de punteria.
 *
 * @param id             identificador unico dentro de la sesion
 * @param yawDeg         posicion horizontal
 * @param pitchDeg       posicion vertical
 * @param angularRadiusDeg radio aparente en grados (Aimlabs ronda 1.0-1.5)
 * @param spawnNanos     instante de aparicion, de {@code System.nanoTime()}
 */
public record Target(int id, double yawDeg, double pitchDeg,
                     double angularRadiusDeg, long spawnNanos) {

    public Target {
        if (angularRadiusDeg <= 0 || angularRadiusDeg >= 90) {
            throw new IllegalArgumentException(
                    "angularRadiusDeg fuera de rango: " + angularRadiusDeg);
        }
    }

    /** Direccion unitaria hacia el centro del objetivo. */
    public Vec3 direction() {
        return Angles.direction(yawDeg, pitchDeg);
    }

    /**
     * Radio del objetivo en el espacio del mundo, asumiendo distancia 1.
     * Solo lo usa la proyeccion a pantalla; la deteccion de impacto es angular.
     */
    public double worldRadiusAtUnitDistance() {
        return Math.sin(Math.toRadians(angularRadiusDeg));
    }

    public long ageNanos(long nowNanos) {
        return nowNanos - spawnNanos;
    }
}
