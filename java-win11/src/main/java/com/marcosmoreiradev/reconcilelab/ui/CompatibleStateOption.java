package com.marcosmoreiradev.reconcilelab.ui;

/**
 * Opción visible para navegar a un estado compatible retenido.
 *
 * @param nodeId identificador del nodo; cero representa un estado informativo
 * @param label texto mostrado en el selector
 */
record CompatibleStateOption(
        long nodeId,
        String label) {

    @Override
    public String toString() {
        return label;
    }
}
