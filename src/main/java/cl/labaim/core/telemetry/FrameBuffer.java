package cl.labaim.core.telemetry;

import java.util.Arrays;

/**
 * Almacen de muestras por frame en arreglos de primitivos.
 *
 * <p>Un {@code ArrayList<FrameSample>} haria una asignacion por frame y el GC podria
 * entrar justo a mitad de un flick, arruinando esa muestra. Con arreglos primitivos
 * que crecen por duplicacion no se asigna nada durante el bucle salvo en los pocos
 * redimensionados, y esos se evitan reservando capacidad al inicio.
 *
 * <p>Es el mismo patron de {@code realloc} de C, con la ventaja de que aqui no hay
 * que acordarse de liberar.
 */
public final class FrameBuffer {

    private long[] tNanos;
    private double[] yawDeg;
    private double[] pitchDeg;
    private int[] nearestTargetId;
    private int size;

    public FrameBuffer(int initialCapacity) {
        int cap = Math.max(16, initialCapacity);
        tNanos = new long[cap];
        yawDeg = new double[cap];
        pitchDeg = new double[cap];
        nearestTargetId = new int[cap];
        size = 0;
    }

    /** Capacidad suficiente para una sesion completa, sin redimensionar en pleno juego. */
    public static FrameBuffer forSession(double durationSeconds, int expectedFps) {
        return new FrameBuffer((int) Math.ceil(durationSeconds * expectedFps * 1.2));
    }

    public void add(long t, double yaw, double pitch, int nearestId) {
        if (size == tNanos.length) {
            grow();
        }
        tNanos[size] = t;
        yawDeg[size] = yaw;
        pitchDeg[size] = pitch;
        nearestTargetId[size] = nearestId;
        size++;
    }

    private void grow() {
        int cap = tNanos.length * 2;
        tNanos = Arrays.copyOf(tNanos, cap);
        yawDeg = Arrays.copyOf(yawDeg, cap);
        pitchDeg = Arrays.copyOf(pitchDeg, cap);
        nearestTargetId = Arrays.copyOf(nearestTargetId, cap);
    }

    public int size() { return size; }
    public long tNanos(int i) { return tNanos[i]; }
    public double yawDeg(int i) { return yawDeg[i]; }
    public double pitchDeg(int i) { return pitchDeg[i]; }
    public int nearestTargetId(int i) { return nearestTargetId[i]; }

    public void clear() { size = 0; }
}
