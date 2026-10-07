package cl.labaim.app;

import cl.labaim.core.math.GameProfile;
import cl.labaim.core.math.Sensitivity;
import cl.labaim.core.scenario.ScenarioConfig;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Configuracion del entrenador, persistida en {@code labaim.properties} junto al jar.
 *
 * <p>Si el archivo no existe se crea con los valores por defecto. Editarlo a mano es
 * la forma prevista de cambiar sensibilidad y escenario mientras no haya menu.
 */
public final class Settings {

    public static final Path FILE = Path.of("labaim.properties");

    public GameProfile profile = GameProfile.VALORANT;
    public double sens = 0.643;
    public double dpi = 800;
    public double horizontalFovDeg = 103.0;

    /**
     * Factor de correccion entre los counts del raton y lo que reporta libGDX.
     * Debe quedarse en 1.0. Si la prueba de la regla (ver README) no cuadra, es casi
     * seguro escalado de pantalla de Windows, y este valor lo compensa.
     */
    public double countScale = 1.0;

    public int windowWidth = 1920;
    public int windowHeight = 1080;
    public boolean fullscreen = false;

    public int gridCols = 5;
    public int gridRows = 5;
    public double spacingDeg = 4.5;
    public int simultaneousTargets = 3;
    public double targetRadiusDeg = 1.1;
    public double durationSeconds = 60.0;

    public Sensitivity sensitivity() {
        return new Sensitivity(profile, sens, dpi);
    }

    public ScenarioConfig scenarioConfig(long seed) {
        return new ScenarioConfig(gridCols, gridRows, spacingDeg,
                simultaneousTargets, targetRadiusDeg, durationSeconds, seed);
    }

    public static Settings loadOrCreate() {
        Settings s = new Settings();
        if (!Files.exists(FILE)) {
            s.save();
            return s;
        }
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(FILE)) {
            p.load(in);
        } catch (IOException e) {
            System.err.println("No se pudo leer " + FILE + ", se usan los valores por defecto: " + e);
            return s;
        }

        s.profile = GameProfile.valueOf(p.getProperty("perfil", s.profile.name()).toUpperCase());
        s.sens = dbl(p, "sens", s.sens);
        s.dpi = dbl(p, "dpi", s.dpi);
        s.horizontalFovDeg = dbl(p, "fovHorizontal", s.horizontalFovDeg);
        s.countScale = dbl(p, "escalaCounts", s.countScale);
        s.windowWidth = (int) dbl(p, "anchoVentana", s.windowWidth);
        s.windowHeight = (int) dbl(p, "altoVentana", s.windowHeight);
        s.fullscreen = Boolean.parseBoolean(p.getProperty("pantallaCompleta", "false"));
        s.gridCols = (int) dbl(p, "columnas", s.gridCols);
        s.gridRows = (int) dbl(p, "filas", s.gridRows);
        s.spacingDeg = dbl(p, "separacionGrados", s.spacingDeg);
        s.simultaneousTargets = (int) dbl(p, "objetivosSimultaneos", s.simultaneousTargets);
        s.targetRadiusDeg = dbl(p, "radioObjetivoGrados", s.targetRadiusDeg);
        s.durationSeconds = dbl(p, "duracionSegundos", s.durationSeconds);
        return s;
    }

    public void save() {
        Properties p = new Properties();
        p.setProperty("perfil", profile.name());
        p.setProperty("sens", String.valueOf(sens));
        p.setProperty("dpi", String.valueOf(dpi));
        p.setProperty("fovHorizontal", String.valueOf(horizontalFovDeg));
        p.setProperty("escalaCounts", String.valueOf(countScale));
        p.setProperty("anchoVentana", String.valueOf(windowWidth));
        p.setProperty("altoVentana", String.valueOf(windowHeight));
        p.setProperty("pantallaCompleta", String.valueOf(fullscreen));
        p.setProperty("columnas", String.valueOf(gridCols));
        p.setProperty("filas", String.valueOf(gridRows));
        p.setProperty("separacionGrados", String.valueOf(spacingDeg));
        p.setProperty("objetivosSimultaneos", String.valueOf(simultaneousTargets));
        p.setProperty("radioObjetivoGrados", String.valueOf(targetRadiusDeg));
        p.setProperty("duracionSegundos", String.valueOf(durationSeconds));
        try (OutputStream out = Files.newOutputStream(FILE)) {
            p.store(out, "LabAIM - configuracion. Tras editar sens o dpi, revalida con la prueba de la regla.");
        } catch (IOException e) {
            System.err.println("No se pudo guardar " + FILE + ": " + e);
        }
    }

    private static double dbl(Properties p, String key, double def) {
        String v = p.getProperty(key);
        if (v == null) return def;
        try {
            return Double.parseDouble(v.trim());
        } catch (NumberFormatException e) {
            System.err.println("Valor invalido para " + key + ": '" + v + "', se usa " + def);
            return def;
        }
    }
}
