package com.marcosmoreiradev.reconcilelab.domain;

/** Tipos de eventos visibles en la línea de tiempo. */
public enum RunEventType {
    /** Caso preparado. */
    READY,
    /** Ejecución iniciada o reanudada. */
    STARTED,
    /** Rama descartada. */
    PRUNED,
    /** Combinación compatible encontrada. */
    MATCH,
    /** Objetivo de ejecución completado. */
    COMPLETED
}
