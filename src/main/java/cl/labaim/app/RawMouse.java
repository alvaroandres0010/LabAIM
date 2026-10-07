package cl.labaim.app;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import org.lwjgl.glfw.GLFW;

/**
 * Activa el modo de entrada cruda de GLFW.
 *
 * <p>Sin esto, los deltas que entrega libGDX pasan antes por la aceleracion y el
 * suavizado del sistema operativo, y la calibracion de sensibilidad deja de valer:
 * un flick rapido y uno lento de la misma distancia fisica girarian angulos distintos.
 *
 * <p>Es la unica clase del proyecto que toca GLFW directamente. Si tu version de
 * libGDX cambia esta API, es el unico archivo que hay que ajustar.
 */
public final class RawMouse {

    private RawMouse() {
    }

    /**
     * Captura el cursor y pide entrada cruda.
     *
     * @return true si el sistema concedio entrada cruda
     */
    public static boolean enable() {
        Gdx.input.setCursorCatched(true);

        try {
            if (!GLFW.glfwRawMouseMotionSupported()) {
                System.err.println("[LabAIM] Este sistema no soporta raw mouse motion. "
                        + "Los deltas pasaran por la aceleracion del SO y la calibracion sera aproximada.");
                return false;
            }
            long handle = ((Lwjgl3Graphics) Gdx.graphics).getWindow().getWindowHandle();
            GLFW.glfwSetInputMode(handle, GLFW.GLFW_RAW_MOUSE_MOTION, GLFW.GLFW_TRUE);
            System.out.println("[LabAIM] Entrada cruda del raton activada.");
            return true;
        } catch (RuntimeException e) {
            System.err.println("[LabAIM] No se pudo activar la entrada cruda: " + e);
            return false;
        }
    }

    public static void disable() {
        Gdx.input.setCursorCatched(false);
    }
}
