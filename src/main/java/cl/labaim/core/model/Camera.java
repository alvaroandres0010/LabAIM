package cl.labaim.core.model;

import cl.labaim.core.math.Angles;
import cl.labaim.core.math.Vec3;

/**
 * Camara en primera persona. Solo rota: nunca se traslada, porque un entrenador de
 * punteria no necesita movimiento.
 *
 * <p>El estado es {@code double} a proposito. Con {@code float} el error de redondeo
 * acumulado tras decenas de miles de updates descalibra la sensibilidad.
 */
public final class Camera {

    /** Limite de pitch. Igual que en cualquier FPS: no puedes mirar mas alla de la vertical. */
    public static final double MAX_PITCH_DEG = 89.0;

    private double yawDeg;
    private double pitchDeg;

    public Camera() {
        this(0.0, 0.0);
    }

    public Camera(double yawDeg, double pitchDeg) {
        this.yawDeg = yawDeg;
        this.pitchDeg = Angles.clamp(pitchDeg, -MAX_PITCH_DEG, MAX_PITCH_DEG);
    }

    /**
     * Aplica un delta crudo del raton.
     *
     * @param dxCounts counts horizontales; positivo = raton a la derecha
     * @param dyCounts counts verticales; positivo = raton hacia abajo (convencion de pantalla)
     * @param degreesPerCount lo que entrega {@code Sensitivity.degreesPerCount()}
     */
    public void applyMouseDelta(double dxCounts, double dyCounts, double degreesPerCount) {
        yawDeg += dxCounts * degreesPerCount;
        pitchDeg = Angles.clamp(pitchDeg - dyCounts * degreesPerCount,
                -MAX_PITCH_DEG, MAX_PITCH_DEG);
    }

    /** Direccion a la que apunta la mira. El crosshair esta siempre en el centro de pantalla. */
    public Vec3 forward() {
        return Angles.direction(yawDeg, pitchDeg);
    }

    /** Eje derecho de la camara, perpendicular a forward y horizontal. */
    public Vec3 right() {
        double yaw = Math.toRadians(yawDeg);
        return new Vec3(Math.cos(yaw), 0.0, -Math.sin(yaw));
    }

    /** Eje arriba de la camara. Se deriva para que (right, up, forward) sea ortonormal. */
    public Vec3 up() {
        return forward().cross(right());
    }

    public double yawDeg() { return yawDeg; }
    public double pitchDeg() { return pitchDeg; }

    public void setOrientation(double yawDeg, double pitchDeg) {
        this.yawDeg = yawDeg;
        this.pitchDeg = Angles.clamp(pitchDeg, -MAX_PITCH_DEG, MAX_PITCH_DEG);
    }

    public void reset() {
        setOrientation(0.0, 0.0);
    }
}
