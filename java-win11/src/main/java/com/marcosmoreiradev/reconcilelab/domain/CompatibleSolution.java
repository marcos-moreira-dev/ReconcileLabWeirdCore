package com.marcosmoreiradev.reconcilelab.domain;

/**
 * Combinación de movimientos que alcanza exactamente el monto objetivo.
 *
 * @param selectionMask máscara de movimientos incluidos
 * @param totalCents total conciliado en centavos
 * @param foundAtTick tick lógico en que fue descubierta
 */
public record CompatibleSolution(
        long selectionMask,
        int totalCents,
        long foundAtTick) {
}
