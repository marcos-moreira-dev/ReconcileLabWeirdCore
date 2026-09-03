package com.marcosmoreiradev.reconcilelab.ui.workspace;

import com.marcosmoreiradev.reconcilelab.domain.ExecutionSnapshot;

/**
 * Presentación textual de la frontera entre búsqueda lógica y traza retenida.
 *
 * <p>El motor puede visitar millones de estados, mientras el canvas conserva
 * sólo una cantidad acotada de tarjetas. Esta clase hace explícita esa
 * diferencia para que un canvas que dejó de crecer no parezca congelado.</p>
 */
final class TraceRetentionPresentation {

    private TraceRetentionPresentation() {
    }

    /**
     * @param snapshot fotografía del motor
     * @return {@code true} cuando ya existen estados fuera de la traza visual
     */
    static boolean isLimited(ExecutionSnapshot snapshot) {
        return snapshot != null
                && snapshot.unretainedTraceNodes() > 0;
    }

    /**
     * Texto principal del aviso flotante.
     *
     * @param snapshot fotografía del motor
     * @return título breve
     */
    static String title(ExecutionSnapshot snapshot) {
        return "Traza detallada completa: "
                + snapshot.nodes().size()
                + " estados retenidos";
    }

    /**
     * Texto vivo que cambia mientras la búsqueda sigue avanzando.
     *
     * @param snapshot fotografía del motor
     * @return detalle de estados no retenidos
     */
    static String detail(ExecutionSnapshot snapshot) {
        String phase =
                snapshot.running()
                        ? "La búsqueda continúa."
                        : "Ejecución completada.";

        return snapshot.unretainedTraceNodes()
                + " estados procesados fuera de la vista detallada. "
                + phase;
    }
}
