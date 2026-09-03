package com.marcosmoreiradev.reconcilelab.ui;

/**
 * Reglas geométricas del panel inferior de línea de tiempo.
 *
 * <p>El valor representa la posición del divisor vertical: 0 es el borde
 * superior y 1 el inferior. La política está separada de JavaFX para poder
 * probarla sin toolkit gráfico.</p>
 */
final class TimelineDockModel {

    static final double DEFAULT_EXPANDED_DIVIDER = 0.78;
    static final double MIN_EXPANDED_DIVIDER = 0.55;
    static final double MAX_EXPANDED_DIVIDER = 0.88;

    private TimelineDockModel() {
    }

    /**
     * Limita la posición cuando el timeline está expandido.
     *
     * @param value posición propuesta
     * @return posición segura
     */
    static double normalizeExpandedDivider(double value) {
        return clamp(
                value,
                MIN_EXPANDED_DIVIDER,
                MAX_EXPANDED_DIVIDER);
    }

    /**
     * Posición que deja visible únicamente la cabecera.
     *
     * @param totalHeight alto del SplitPane
     * @param headerHeight alto objetivo de la cabecera
     * @return posición del divisor
     */
    static double collapsedDivider(
            double totalHeight,
            double headerHeight) {

        if (totalHeight <= 0) {
            return 0.95;
        }

        double header =
                Math.max(
                        28,
                        headerHeight);

        return clamp(
                1.0 - header / totalHeight,
                0.75,
                0.97);
    }

    private static double clamp(
            double value,
            double min,
            double max) {

        return Math.max(
                min,
                Math.min(
                        max,
                        value));
    }
}
