package cl.labaim.core.math;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Estos tests son la validacion externa del proyecto: los numeros esperados salen de
 * capturas reales de Aimlabs, no de nuestra propia formula. Si alguno falla, LabAIM
 * dejo de estar calibrado con el juego objetivo.
 */
class SensitivityTest {

    @Test
    @DisplayName("Valorant sens=1 @ 800 DPI da 16.33 cm/360 (valor que muestra Aimlabs)")
    void valorantSens1() {
        assertEquals(16.33, new Sensitivity(GameProfile.VALORANT, 1.0, 800).cmPer360(), 0.005);
    }

    @Test
    @DisplayName("Valorant sens=0.6434 @ 800 DPI da 25.38 cm/360")
    void valorantSensBaja() {
        assertEquals(25.38, new Sensitivity(GameProfile.VALORANT, 0.6434, 800).cmPer360(), 0.01);
    }

    @Test
    @DisplayName("CS2 sens=1 @ 800 DPI da 51.95 cm/360")
    void cs2Sens1() {
        assertEquals(51.95, new Sensitivity(GameProfile.CS2, 1.0, 800).cmPer360(), 0.01);
    }

    @Test
    @DisplayName("grados por count es exactamente constante_yaw * sens")
    void gradosPorCount() {
        assertEquals(0.07, new Sensitivity(GameProfile.VALORANT, 1.0, 800).degreesPerCount(), 1e-12);
        assertEquals(0.035, new Sensitivity(GameProfile.VALORANT, 0.5, 800).degreesPerCount(), 1e-12);
    }

    @Test
    @DisplayName("el DPI no cambia los grados por count, solo el cm/360")
    void dpiNoAfectaAngulo() {
        var a = new Sensitivity(GameProfile.VALORANT, 1.0, 400);
        var b = new Sensitivity(GameProfile.VALORANT, 1.0, 1600);
        assertEquals(a.degreesPerCount(), b.degreesPerCount(), 1e-12);
        assertEquals(a.cmPer360(), b.cmPer360() * 4.0, 1e-9);
    }

    @Test
    @DisplayName("sensForCmPer360 es la inversa exacta de cmPer360")
    void inversa() {
        for (double sens : new double[]{0.1, 0.5, 1.0, 2.5, 10.0}) {
            double cm = new Sensitivity(GameProfile.VALORANT, sens, 800).cmPer360();
            assertEquals(sens, Sensitivity.sensForCmPer360(GameProfile.VALORANT, cm, 800), 1e-9);
        }
    }

    @Test
    void rechazaValoresInvalidos() {
        assertThrows(IllegalArgumentException.class,
                () -> new Sensitivity(GameProfile.VALORANT, 0, 800));
        assertThrows(IllegalArgumentException.class,
                () -> new Sensitivity(GameProfile.VALORANT, 1, -400));
    }
}
