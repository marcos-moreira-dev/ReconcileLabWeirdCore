package com.marcosmoreiradev.reconcilelab.ui;

/**
 * Fila inmutable de la tabla de métricas.
 *
 * @param measure nombre humano de la métrica
 * @param value valor ya formateado para presentación
 */
record MetricRow(
        String measure,
        String value) {
}
