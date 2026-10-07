package cl.labaim.core.scenario;

import cl.labaim.core.model.Camera;
import cl.labaim.core.model.Target;

import java.util.List;

/**
 * Un escenario de entrenamiento: decide donde y cuando aparecen los objetivos.
 *
 * <p>No sabe nada de libGDX ni de renderizado. Eso permite testearlo entero sin abrir
 * una ventana, que es exactamente lo que hacen los tests de este proyecto.
 */
public interface Scenario {

    String name();

    ScenarioConfig config();

    /** Arranca la ronda y genera los objetivos iniciales. */
    void start(long nowNanos);

    /** Objetivos vivos ahora mismo. La lista devuelta no debe modificarse. */
    List<Target> activeTargets();

    /** Avanza el estado interno. Para Gridshot no hace nada; para tracking si. */
    void update(long nowNanos);

    /** Procesa un disparo y actualiza el estado del escenario. */
    ShotResult onShot(Camera cam, long nowNanos);

    /** Objetivo mas cercano a la mira, o null si no hay ninguno. Se usa para la telemetria. */
    Target nearestTarget(Camera cam);
}
