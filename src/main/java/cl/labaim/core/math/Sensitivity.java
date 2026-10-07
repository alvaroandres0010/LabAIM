package cl.labaim.core.math;

/**
 * Conversion entre sensibilidad del juego, DPI y cm/360.
 *
 * <pre>
 *   grados_por_count = constante_yaw * sens
 *   counts_por_360   = 360 / grados_por_count
 *   cm_por_360       = counts_por_360 / dpi * 2.54
 * </pre>
 *
 * <p>Comprobacion contra Aimlabs: perfil Valorant, sens 1, 800 DPI da 16.33 cm/360,
 * que es exactamente lo que reporta Aimlabs para esa configuracion.
 */
public final class Sensitivity {

    public static final double CM_PER_INCH = 2.54;

    private final GameProfile profile;
    private final double sens;
    private final double dpi;

    public Sensitivity(GameProfile profile, double sens, double dpi) {
        if (sens <= 0) throw new IllegalArgumentException("sens debe ser > 0, fue " + sens);
        if (dpi <= 0) throw new IllegalArgumentException("dpi debe ser > 0, fue " + dpi);
        this.profile = profile;
        this.sens = sens;
        this.dpi = dpi;
    }

    /** Grados que gira la camara por cada count del raton. Es lo que usa la Camera. */
    public double degreesPerCount() {
        return profile.degreesPerCountAtSens1() * sens;
    }

    public double countsPer360() {
        return 360.0 / degreesPerCount();
    }

    public double inchesPer360() {
        return countsPer360() / dpi;
    }

    public double cmPer360() {
        return inchesPer360() * CM_PER_INCH;
    }

    /** Sensibilidad del juego que produce un cm/360 dado. La inversa de {@link #cmPer360()}. */
    public static double sensForCmPer360(GameProfile profile, double cmPer360, double dpi) {
        double countsPer360 = cmPer360 / CM_PER_INCH * dpi;
        double degreesPerCount = 360.0 / countsPer360;
        return degreesPerCount / profile.degreesPerCountAtSens1();
    }

    public GameProfile profile() { return profile; }
    public double sens() { return sens; }
    public double dpi() { return dpi; }

    @Override
    public String toString() {
        return String.format("%s sens=%.4f dpi=%.0f -> %.2f cm/360", profile, sens, dpi, cmPer360());
    }
}
