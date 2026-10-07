package cl.labaim.core.session;

/**
 * Resumen de una ronda. Son los numeros que se muestran en pantalla; el analisis fino
 * sale despues de los CSV, no de aqui.
 */
public record SessionStats(int shots, int hits, int misses, double accuracy,
                           double avgTimeToHitMs, double avgErrorDeg, double elapsedSeconds) {

    public static final SessionStats EMPTY =
            new SessionStats(0, 0, 0, 0.0, 0.0, 0.0, 0.0);

    /** Aciertos por segundo. Es la medida mas honesta de rendimiento en clicking. */
    public double hitsPerSecond() {
        return elapsedSeconds > 0 ? hits / elapsedSeconds : 0.0;
    }
}
