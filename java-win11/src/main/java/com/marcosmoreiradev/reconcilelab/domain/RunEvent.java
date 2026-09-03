package com.marcosmoreiradev.reconcilelab.domain;

/**
 * Evento significativo conservado para la línea de tiempo.
 *
 * @param tick tick lógico asociado
 * @param type tipo de evento
 * @param nodeId nodo relacionado, cuando aplica
 * @param message explicación humana breve
 */
public record RunEvent(
        long tick,
        RunEventType type,
        long nodeId,
        String message) {
}
