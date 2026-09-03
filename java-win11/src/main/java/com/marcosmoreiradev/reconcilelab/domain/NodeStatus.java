package com.marcosmoreiradev.reconcilelab.domain;

/** Estado de presentación de un nodo retenido. */
public enum NodeStatus {
    /** Pendiente de procesamiento. */
    QUEUED,
    /** Procesado sin ser solución ni poda. */
    COMPLETED,
    /** Descartado por una regla segura. */
    PRUNED,
    /** Estado compatible con el monto objetivo. */
    MATCH
}
