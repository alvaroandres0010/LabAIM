package cl.labaim.core.math;

import cl.labaim.core.model.Camera;
import cl.labaim.core.model.Target;
import cl.labaim.core.model.Viewport;

/**
 * Proyeccion perspectiva de una direccion del mundo a coordenadas de pantalla.
 *
 * <p>Esto es lo unico que separa LabAIM de un juego de clicar circulos: el
 * desplazamiento en pantalla va con la tangente del angulo, no linealmente. Por eso
 * un flick de 10 grados cerca del centro recorre menos pixeles que uno de 10 grados
 * en el borde, igual que en cualquier FPS.
 */
public final class Projection {

    private Projection() {
    }

    /**
     * Resultado de proyectar un objetivo.
     *
     * @param xPx      coordenada X en pixeles, origen a la izquierda
     * @param yPx      coordenada Y en pixeles, origen abajo (convencion de OpenGL/libGDX)
     * @param radiusPx radio aparente en pixeles
     * @param visible  false si el objetivo esta detras de la camara
     */
    public record ScreenPoint(double xPx, double yPx, double radiusPx, boolean visible) {
        public static final ScreenPoint HIDDEN = new ScreenPoint(0, 0, 0, false);
    }

    public static ScreenPoint project(Camera cam, Target target, Viewport vp) {
        return project(cam, target.direction(), target.worldRadiusAtUnitDistance(), vp);
    }

    /**
     * @param dir    direccion unitaria en el mundo
     * @param radius radio del objetivo en unidades de mundo, a distancia 1
     */
    public static ScreenPoint project(Camera cam, Vec3 dir, double radius, Viewport vp) {
        Vec3 fwd = cam.forward();
        Vec3 right = cam.right();
        Vec3 up = cam.up();

        // Coordenadas en el espacio de la camara.
        double zc = dir.dot(fwd);   // profundidad, positiva hacia delante
        if (zc <= 1e-9) {
            return ScreenPoint.HIDDEN; // detras de la camara o exactamente de lado
        }
        double xc = dir.dot(right);
        double yc = dir.dot(up);

        double f = vp.focalLengthPx();
        double x = vp.centerX() + f * xc / zc;
        double y = vp.centerY() + f * yc / zc;
        double r = f * radius / zc;

        return new ScreenPoint(x, y, r, true);
    }
}
