package com.marcosmoreiradev.reconcilelab.domain;

/**
 * Estado lógico retenido para visualización.
 *
 * <p>Un nodo no es un thread de Java ni un control JavaFX.</p>
 */
public record ExecutionNode(
        long id,
        long parentId,
        boolean hasParent,
        int index,
        int sumCents,
        int depth,
        boolean tookValue,
        NodeStatus status) {
}
