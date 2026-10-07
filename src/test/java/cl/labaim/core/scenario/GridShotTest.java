package cl.labaim.core.scenario;

import cl.labaim.core.model.Camera;
import cl.labaim.core.model.Target;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GridShotTest {

    private GridShot gs;

    @BeforeEach
    void setUp() {
        gs = new GridShot(ScenarioConfig.gridshot(42L));
        gs.start(0L);
    }

    private static Set<String> celdasOcupadas(GridShot g) {
        Set<String> s = new HashSet<>();
        for (Target t : g.activeTargets()) s.add(t.yawDeg() + "/" + t.pitchDeg());
        return s;
    }

    private static Camera apuntandoA(Target t) {
        return new Camera(t.yawDeg(), t.pitchDeg());
    }

    @Test
    @DisplayName("arranca con tantos objetivos como dice la config, en celdas distintas")
    void arranque() {
        assertEquals(3, gs.activeTargets().size());
        assertEquals(3, celdasOcupadas(gs).size());
    }

    @Test
    @DisplayName("500 impactos seguidos mantienen el invariante de la rejilla")
    void invarianteTrasMuchosImpactos() {
        long t = 0;
        for (int i = 0; i < 500; i++) {
            Target objetivo = gs.activeTargets().get(i % gs.activeTargets().size());
            t += 200_000_000L;
            ShotResult r = gs.onShot(apuntandoA(objetivo), t);

            assertTrue(r.hit(), "apuntar al centro siempre debe acertar (iteracion " + i + ")");
            assertEquals(3, gs.activeTargets().size(), "iteracion " + i);
            assertEquals(3, celdasOcupadas(gs).size(),
                    "dos objetivos ocuparon la misma celda en la iteracion " + i);
        }
    }

    @Test
    @DisplayName("un fallo no altera los objetivos vivos")
    void falloNoCambiaNada() {
        Target antes = gs.activeTargets().get(0);
        ShotResult r = gs.onShot(new Camera(antes.yawDeg() + 30, antes.pitchDeg()), 1000L);
        assertFalse(r.hit());
        assertEquals(3, gs.activeTargets().size());
    }

    @Test
    @DisplayName("el objetivo impactado desaparece y aparece uno nuevo con id mayor")
    void reemplazo() {
        Target objetivo = gs.activeTargets().get(0);
        int idPrevio = objetivo.id();
        gs.onShot(apuntandoA(objetivo), 1000L);

        assertFalse(gs.activeTargets().contains(objetivo));
        assertTrue(gs.activeTargets().stream().anyMatch(t -> t.id() > idPrevio));
    }

    @Test
    @DisplayName("onShot resuelve contra el objetivo mas cercano a la mira")
    void resuelveContraElMasCercano() {
        Target a = gs.activeTargets().get(0);
        ShotResult r = gs.onShot(apuntandoA(a), 1000L);
        assertEquals(a.id(), r.target().id());
    }

    @Test
    @DisplayName("un fallo cerca de un vecino puede acertarle: es comportamiento correcto")
    void clipeAlVecino() {
        // Las celdas estan a 4.5 grados y el radio es 1.1. Fallar por 4.0 grados deja la
        // mira a 0.5 grados del vecino, dentro de su radio. Debe contar como acierto.
        GridShot denso = new GridShot(new ScenarioConfig(3, 1, 4.5, 3, 1.1, 60.0, 1L));
        denso.start(0L);
        Target centro = denso.activeTargets().stream()
                .filter(t -> t.yawDeg() == 0.0).findFirst().orElseThrow();
        ShotResult r = denso.onShot(new Camera(centro.yawDeg() + 4.0, centro.pitchDeg()), 1000L);
        assertTrue(r.hit());
        assertNotEquals(centro.id(), r.target().id(), "deberia haber acertado al vecino");
    }

    @Test
    @DisplayName("la misma semilla produce exactamente la misma secuencia")
    void determinismo() {
        GridShot a = new GridShot(ScenarioConfig.gridshot(7L));
        GridShot b = new GridShot(ScenarioConfig.gridshot(7L));
        a.start(0L);
        b.start(0L);
        for (int i = 0; i < 100; i++) {
            Target ta = a.activeTargets().get(0);
            Target tb = b.activeTargets().get(0);
            assertEquals(ta.yawDeg(), tb.yawDeg(), 1e-12, "divergen en el paso " + i);
            assertEquals(ta.pitchDeg(), tb.pitchDeg(), 1e-12);
            a.onShot(apuntandoA(ta), i * 1000L);
            b.onShot(apuntandoA(tb), i * 1000L);
        }
    }

    @Test
    @DisplayName("la rejilla queda centrada en el origen")
    void rejillaCentrada() {
        GridShot lleno = new GridShot(new ScenarioConfig(5, 5, 4.5, 25, 1.1, 60.0, 1L));
        lleno.start(0L);
        double sumaYaw = lleno.activeTargets().stream().mapToDouble(Target::yawDeg).sum();
        double sumaPitch = lleno.activeTargets().stream().mapToDouble(Target::pitchDeg).sum();
        assertEquals(0.0, sumaYaw, 1e-9);
        assertEquals(0.0, sumaPitch, 1e-9);
        assertEquals(18.0, lleno.gridWidthDeg(), 1e-9);
    }

    @Test
    void configRechazaValoresImposibles() {
        assertThrows(IllegalArgumentException.class,
                () -> new ScenarioConfig(2, 2, 4.5, 99, 1.1, 60, 1L));
        assertThrows(IllegalArgumentException.class,
                () -> new ScenarioConfig(5, 5, -1, 3, 1.1, 60, 1L));
    }
}
