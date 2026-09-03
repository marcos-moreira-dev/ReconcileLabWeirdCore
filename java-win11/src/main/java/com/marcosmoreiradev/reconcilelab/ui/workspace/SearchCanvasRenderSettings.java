package com.marcosmoreiradev.reconcilelab.ui.workspace;

/**
 * Configuración pequeña del nivel de detalle del lienzo.
 *
 * <p>El zoom puede llegar a 25% para inspeccionar la forma global del árbol,
 * pero a esa escala intentar pintar todo el texto sólo produce ruido visual.
 * Estas dos fronteras mantienen una única política fácil de ajustar.</p>
 */
final class SearchCanvasRenderSettings {

    static final double COMPACT_FROM_SCALE = 0.55;
    static final double FULL_DETAIL_FROM_SCALE = 0.80;

    private SearchCanvasRenderSettings() {
    }

    /**
     * Clasifica una escala del viewport en un nivel visual.
     *
     * @param scale factor 1.0 = 100%
     * @return nivel de detalle
     */
    static DetailLevel detailLevel(double scale) {
        if (scale < COMPACT_FROM_SCALE) {
            return DetailLevel.OVERVIEW;
        }

        if (scale < FULL_DETAIL_FROM_SCALE) {
            return DetailLevel.COMPACT;
        }

        return DetailLevel.FULL;
    }

    /**
     * Niveles visuales del canvas.
     */
    enum DetailLevel {
        /** Sólo geometría y estado por color. */
        OVERVIEW,
        /** Identidad y acumulado, sin texto secundario. */
        COMPACT,
        /** Tarjeta pedagógica completa. */
        FULL
    }
}
