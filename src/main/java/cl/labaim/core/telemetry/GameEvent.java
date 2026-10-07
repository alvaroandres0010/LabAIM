package cl.labaim.core.telemetry;

/**
 * Evento discreto con marca de tiempo.
 *
 * @param tNanos          instante, de System.nanoTime()
 * @param type            tipo de evento
 * @param targetId        objetivo implicado, o 0
 * @param targetYawDeg    posicion del objetivo
 * @param targetPitchDeg  posicion del objetivo
 * @param camYawDeg       donde apuntaba la mira
 * @param camPitchDeg     donde apuntaba la mira
 * @param angularErrorDeg angulo entre mira y centro del objetivo; NaN si no aplica
 */
public record GameEvent(long tNanos, EventType type, int targetId,
                        double targetYawDeg, double targetPitchDeg,
                        double camYawDeg, double camPitchDeg,
                        double angularErrorDeg) {

    static final String CSV_HEADER =
            "t_ns,tipo,target_id,target_yaw,target_pitch,cam_yaw,cam_pitch,error_angular_deg";

    String toCsvRow() {
        return tNanos + "," + type + "," + targetId + ","
                + fmt(targetYawDeg) + "," + fmt(targetPitchDeg) + ","
                + fmt(camYawDeg) + "," + fmt(camPitchDeg) + ","
                + fmt(angularErrorDeg);
    }

    private static String fmt(double v) {
        return Double.isNaN(v) ? "" : String.format(java.util.Locale.ROOT, "%.6f", v);
    }
}
