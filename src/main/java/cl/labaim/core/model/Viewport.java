package cl.labaim.core.model;

/**
 * Parametros de proyeccion de la pantalla.
 *
 * <p>El FOV importa tanto como la sensibilidad: si no coincide con el del juego
 * objetivo, los flicks largos no transfieren aunque la sensibilidad angular sea
 * correcta. El FOV no cambia cuanto gira la camara, cambia donde aparece en pantalla
 * lo que estas mirando.
 *
 * @param widthPx   ancho en pixeles
 * @param heightPx  alto en pixeles
 * @param vFovRad   campo de vision VERTICAL en radianes
 */
public record Viewport(int widthPx, int heightPx, double vFovRad) {

    public Viewport {
        if (widthPx <= 0 || heightPx <= 0) {
            throw new IllegalArgumentException("Resolucion invalida: " + widthPx + "x" + heightPx);
        }
        if (vFovRad <= 0 || vFovRad >= Math.PI) {
            throw new IllegalArgumentException("vFov fuera de rango: " + vFovRad);
        }
    }

    /**
     * Construye el viewport desde un FOV horizontal, que es como lo expresan los juegos.
     * Valorant usa 103 grados horizontales en 16:9.
     */
    public static Viewport fromHorizontalFov(int widthPx, int heightPx, double hFovDeg) {
        double hFov = Math.toRadians(hFovDeg);
        double vFov = 2.0 * Math.atan(Math.tan(hFov / 2.0) * heightPx / (double) widthPx);
        return new Viewport(widthPx, heightPx, vFov);
    }

    /** Distancia focal en pixeles. Es la constante que convierte angulos en pixeles. */
    public double focalLengthPx() {
        return (heightPx / 2.0) / Math.tan(vFovRad / 2.0);
    }

    public double centerX() { return widthPx / 2.0; }
    public double centerY() { return heightPx / 2.0; }

    public double horizontalFovDeg() {
        return Math.toDegrees(2.0 * Math.atan(Math.tan(vFovRad / 2.0) * widthPx / (double) heightPx));
    }
}
