package cl.labaim.app;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

/**
 * Punto de entrada.
 *
 * <p>Vsync va desactivado a proposito: con vsync la presentacion del frame introduce
 * hasta un frame completo de latencia variable, y eso convierte en ruido cualquier
 * medicion de tiempo de reaccion.
 */
public final class DesktopLauncher {

    public static void main(String[] args) {
        Settings settings = Settings.loadOrCreate();

        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("LabAIM");
        config.useVsync(false);
        config.setForegroundFPS(0);   // 0 = sin limite
        config.setIdleFPS(60);
        config.setBackBufferConfig(8, 8, 8, 8, 16, 0, 0); // sin antialiasing: no aporta nada aqui

        if (settings.fullscreen) {
            config.setFullscreenMode(Lwjgl3ApplicationConfiguration.getDisplayMode());
        } else {
            config.setWindowedMode(settings.windowWidth, settings.windowHeight);
        }

        new Lwjgl3Application(new LabAimGame(settings), config);
    }
}
