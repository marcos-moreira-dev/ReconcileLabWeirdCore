package com.marcosmoreiradev.reconcilelab.ui;

/**
 * Fila inmutable de la línea de tiempo.
 *
 * @param tick tick lógico formateado
 * @param event nombre humano del evento
 * @param details explicación breve
 */
record EventRow(
        String tick,
        String event,
        String details) {
}
