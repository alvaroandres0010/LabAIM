package cl.labaim.app;

import cl.labaim.core.math.Sensitivity;
import cl.labaim.core.session.SessionStats;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.Disposable;

import java.util.Locale;

/**
 * Texto en pantalla: tiempo, aciertos, precision y la calibracion activa.
 *
 * <p>Reutiliza un unico {@link StringBuilder} y evita {@code String.format} en el
 * bucle. El formateo de texto por frame es la fuente real de basura para el GC en un
 * juego sencillo como este, mucho mas que los vectores de la proyeccion.
 */
public final class Hud implements Disposable {

    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();
    private final OrthographicCamera camera = new OrthographicCamera();
    private final StringBuilder sb = new StringBuilder(96);

    private int width;
    private int height;

    public Hud(int width, int height) {
        font.getData().setScale(1.4f);
        font.setColor(Color.WHITE);
        resize(width, height);
    }

    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
        camera.setToOrtho(false, width, height);
        camera.update();
        batch.setProjectionMatrix(camera.combined);
    }

    public void drawRunning(SessionStats stats, double remainingSeconds) {
        batch.begin();
        sb.setLength(0);
        sb.append("Tiempo ").append((int) Math.ceil(remainingSeconds)).append("s");
        font.draw(batch, sb, 24, height - 24);

        sb.setLength(0);
        sb.append("Aciertos ").append(stats.hits()).append('/').append(stats.shots());
        if (stats.shots() > 0) {
            sb.append("   ").append(Math.round(stats.accuracy() * 100)).append('%');
        }
        font.draw(batch, sb, 24, height - 56);
        batch.end();
    }

    public void drawIdle(String titulo, String instruccion, Sensitivity sens, double fovDeg) {
        batch.begin();
        font.draw(batch, titulo, 24, height - 24);
        font.draw(batch, instruccion, 24, height - 64);
        font.draw(batch, String.format(Locale.ROOT,
                        "%s  sens %.3f  %.0f DPI  =  %.2f cm/360   |   FOV %.0f",
                        sens.profile(), sens.sens(), sens.dpi(), sens.cmPer360(), fovDeg),
                24, 48);
        batch.end();
    }

    public void drawResults(SessionStats stats, String rutaCsv) {
        batch.begin();
        float y = height - 24;
        font.draw(batch, "Ronda terminada", 24, y);
        y -= 44;
        font.draw(batch, String.format(Locale.ROOT, "Aciertos:   %d de %d  (%.1f%%)",
                stats.hits(), stats.shots(), stats.accuracy() * 100), 24, y);
        y -= 32;
        font.draw(batch, String.format(Locale.ROOT, "Aciertos/s: %.2f", stats.hitsPerSecond()), 24, y);
        y -= 32;
        font.draw(batch, String.format(Locale.ROOT, "Tiempo medio por objetivo: %.0f ms",
                stats.avgTimeToHitMs()), 24, y);
        y -= 32;
        font.draw(batch, String.format(Locale.ROOT, "Error medio al impactar: %.3f grados",
                stats.avgErrorDeg()), 24, y);
        y -= 48;
        font.draw(batch, "Telemetria guardada en: " + rutaCsv, 24, y);
        y -= 32;
        font.draw(batch, "R para repetir   ESC para salir", 24, y);
        batch.end();
    }

    public int width() { return width; }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }
}
