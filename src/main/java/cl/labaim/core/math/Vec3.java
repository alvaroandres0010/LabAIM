package cl.labaim.core.math;

/**
 * Vector de 3 componentes en doble precision.
 *
 * <p>Usamos {@code double} y no {@code float} a proposito: el yaw de la camara se
 * actualiza miles de veces por sesion y con {@code float} el error de redondeo se
 * acumula hasta descalibrar la sensibilidad.
 *
 * <p>Es un {@code record} inmutable. Las instancias son pequenas y de vida muy corta,
 * asi que el analisis de escape de la JVM normalmente evita que lleguen al heap.
 */
public record Vec3(double x, double y, double z) {

    public static final Vec3 ZERO = new Vec3(0, 0, 0);

    public double dot(Vec3 o) {
        return x * o.x + y * o.y + z * o.z;
    }

    public Vec3 cross(Vec3 o) {
        return new Vec3(
                y * o.z - z * o.y,
                z * o.x - x * o.z,
                x * o.y - y * o.x);
    }

    public double length() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public Vec3 normalized() {
        double len = length();
        if (len == 0.0) {
            throw new IllegalStateException("No se puede normalizar el vector cero");
        }
        return new Vec3(x / len, y / len, z / len);
    }

    public Vec3 scale(double s) {
        return new Vec3(x * s, y * s, z * s);
    }
}
