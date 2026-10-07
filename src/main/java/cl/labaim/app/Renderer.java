package cl.labaim.app;

import cl.labaim.core.math.Projection;
import cl.labaim.core.model.Camera;
import cl.labaim.core.model.Target;
import cl.labaim.core.model.Viewport;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Disposable;

import java.util.List;

/**
 * Dibuja la escena. Deliberadamente pobre: circulos planos sobre fondo liso.
 *
 * <p>No hace falta nada mas. La fidelidad de un entrenador de punteria esta en la
 * relacion entre el movimiento del raton y el desplazamiento en pantalla, que la
 * resuelve {@link Projection}, no en el aspecto de los objetivos.
 */
public final class Renderer implements Disposable {

    private static final Color FONDO = new Color(0.09f, 0.10f, 0.12f, 1f);
    private static final Color OBJETIVO = new Color(0.20f, 0.85f, 0.78f, 1f);
    private static final Color OBJETIVO_CERCANO = new Color(0.45f, 0.95f, 0.88f, 1f);
    private static final Color MIRA = new Color(1f, 0.35f, 0.35f, 1f);

    private final ShapeRenderer shapes = new ShapeRenderer();
    private final OrthographicCamera uiCamera = new OrthographicCamera();

    /** Segmentos del circulo. Fijo para que el coste de dibujo no dependa del tamano. */
    private static final int SEGMENTOS = 24;

    public Renderer(int width, int height) {
        resize(width, height);
    }

    public void resize(int width, int height) {
        uiCamera.setToOrtho(false, width, height);
        uiCamera.update();
        shapes.setProjectionMatrix(uiCamera.combined);
    }

    public Color colorFondo() {
        return FONDO;
    }

    /**
     * @param cercano objetivo mas cercano a la mira; se resalta para dar feedback. Puede ser null.
     */
    public void draw(Camera cam, List<Target> targets, Viewport vp, Target cercano) {
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        for (int i = 0; i < targets.size(); i++) {
            Target t = targets.get(i);
            Projection.ScreenPoint p = Projection.project(cam, t, vp);
            if (!p.visible()) continue;
            // Descartamos lo que cae muy fuera de pantalla para no dibujar de mas.
            if (p.xPx() < -p.radiusPx() || p.xPx() > vp.widthPx() + p.radiusPx()) continue;
            if (p.yPx() < -p.radiusPx() || p.yPx() > vp.heightPx() + p.radiusPx()) continue;

            shapes.setColor(cercano != null && cercano.id() == t.id() ? OBJETIVO_CERCANO : OBJETIVO);
            shapes.circle((float) p.xPx(), (float) p.yPx(), (float) p.radiusPx(), SEGMENTOS);
        }

        dibujarMira(vp);
        shapes.end();
    }

    private void dibujarMira(Viewport vp) {
        float cx = (float) vp.centerX();
        float cy = (float) vp.centerY();
        float largo = 9f;
        float grosor = 2f;
        shapes.setColor(MIRA);
        shapes.rect(cx - largo, cy - grosor / 2f, largo * 2f, grosor);
        shapes.rect(cx - grosor / 2f, cy - largo, grosor, largo * 2f);
    }

    @Override
    public void dispose() {
        shapes.dispose();
    }
}
