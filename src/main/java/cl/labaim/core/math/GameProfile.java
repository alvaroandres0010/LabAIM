package cl.labaim.core.math;

/**
 * Constante de yaw de cada juego: cuantos grados gira la camara por cada count del
 * raton, con la sensibilidad en 1.
 *
 * <p>Esta constante es lo unico que hace falta para que LabAIM tenga paridad 1:1 con
 * el juego objetivo. No depende del FOV ni de la resolucion.
 */
public enum GameProfile {

    /** Valorant: 0.07 grados por count a sensibilidad 1. */
    VALORANT(0.07, 103.0),

    /** Counter-Strike 2 / CS:GO: 0.022 grados por count a sensibilidad 1. */
    CS2(0.022, 90.0),

    /** Overwatch 2: comparte la constante de Source. */
    OVERWATCH2(0.0066, 103.0),

    /** Perfil neutro para entrenar sin referencia a ningun juego. */
    RAW(0.05, 103.0);

    private final double degreesPerCountAtSens1;
    private final double defaultHorizontalFovDeg;

    GameProfile(double degreesPerCountAtSens1, double defaultHorizontalFovDeg) {
        this.degreesPerCountAtSens1 = degreesPerCountAtSens1;
        this.defaultHorizontalFovDeg = defaultHorizontalFovDeg;
    }

    public double degreesPerCountAtSens1() {
        return degreesPerCountAtSens1;
    }

    /** FOV horizontal por defecto del juego, en 16:9. Verificalo contra tu instalacion. */
    public double defaultHorizontalFovDeg() {
        return defaultHorizontalFovDeg;
    }
}
