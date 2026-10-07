package cl.labaim.core.math;

/** Utilidades de angulos. Todo en grados salvo que el nombre diga lo contrario. */
public final class Angles {

    private Angles() {
    }

    /** Lleva un angulo al rango [0, 360). */
    public static double normalize360(double deg) {
        double r = deg % 360.0;
        return r < 0 ? r + 360.0 : r;
    }

    /** Lleva un angulo al rango [-180, 180). Util para calcular diferencias con signo. */
    public static double wrapSigned(double deg) {
        double r = normalize360(deg + 180.0);
        return r - 180.0;
    }

    public static double clamp(double value, double min, double max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    /**
     * Direccion unitaria en el mundo para un par (yaw, pitch).
     *
     * <p>Convencion: yaw 0 mira hacia +Z, yaw creciente gira a la derecha,
     * pitch positivo mira hacia arriba (+Y).
     */
    public static Vec3 direction(double yawDeg, double pitchDeg) {
        double yaw = Math.toRadians(yawDeg);
        double pitch = Math.toRadians(pitchDeg);
        double cp = Math.cos(pitch);
        return new Vec3(cp * Math.sin(yaw), Math.sin(pitch), cp * Math.cos(yaw));
    }

    /**
     * Angulo entre dos direcciones, en grados.
     *
     * <p>Usa {@code atan2(|a x b|, a.b)} en vez de {@code acos(a.b)}: para angulos
     * pequenos (que es justo lo que medimos al analizar punteria) acos pierde casi
     * toda la precision porque su derivada explota cerca de 1.
     */
    public static double between(Vec3 a, Vec3 b) {
        double cross = a.cross(b).length();
        double dot = a.dot(b);
        return Math.toDegrees(Math.atan2(cross, dot));
    }

    /** Angulo entre dos pares (yaw, pitch), en grados. */
    public static double between(double yaw1, double pitch1, double yaw2, double pitch2) {
        return between(direction(yaw1, pitch1), direction(yaw2, pitch2));
    }
}
