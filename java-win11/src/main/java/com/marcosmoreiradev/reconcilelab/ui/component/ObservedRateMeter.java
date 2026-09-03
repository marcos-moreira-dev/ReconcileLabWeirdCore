package com.marcosmoreiradev.reconcilelab.ui.component;

import com.marcosmoreiradev.reconcilelab.domain.ExecutionSnapshot;

/**
 * Calcula una tasa observada de estados lógicos procesados por segundo.
 *
 * <p>La tasa es diagnóstica y deliberadamente independiente de la complejidad
 * asintótica. Se calcula entre snapshots de la interfaz y nunca condiciona el
 * comportamiento del motor.</p>
 */
public final class ObservedRateMeter {

    private static final long MIN_SAMPLE_NANOS = 250_000_000L;

    private long lastTick;
    private long lastNanos;
    private double statesPerSecond;

    /**
     * Incorpora un snapshot a la ventana de medición.
     *
     * @param snapshot snapshot actual
     */
    public void sample(ExecutionSnapshot snapshot) {
        long now = System.nanoTime();

        if (snapshot.logicalTick() < lastTick) {
            reset();
        }

        if (lastNanos == 0) {
            lastTick = snapshot.logicalTick();
            lastNanos = now;
            return;
        }

        long elapsed = now - lastNanos;

        if (elapsed < MIN_SAMPLE_NANOS) {
            return;
        }

        long delta = snapshot.logicalTick() - lastTick;

        statesPerSecond = elapsed <= 0
                ? 0
                : delta * 1_000_000_000.0 / elapsed;

        lastTick = snapshot.logicalTick();
        lastNanos = now;
    }

    /**
     * Devuelve la tasa observada más reciente.
     *
     * @return estados lógicos por segundo
     */
    public double statesPerSecond() {
        return statesPerSecond;
    }

    /**
     * Reinicia la ventana de medición al cargar o reiniciar un caso.
     */
    public void reset() {
        lastTick = 0;
        lastNanos = 0;
        statesPerSecond = 0;
    }
}
