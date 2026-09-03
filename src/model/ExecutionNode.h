#ifndef RECONCILELAB_EXECUTION_NODE_H
#define RECONCILELAB_EXECUTION_NODE_H

/**
 * Estado final u observable de una entrada conservada en la traza.
 *
 * "Nodo" aquí significa estado lógico retenido para observación. No significa
 * hilo de CPU ni ventana nativa de Windows.
 */
enum NodeStatus
{
    NODE_QUEUED = 0,
    NODE_COMPLETED,
    NODE_PRUNED,
    NODE_MATCH
};

/**
 * Copia pequeña de un estado de búsqueda que la interfaz puede dibujar.
 *
 * El motor puede procesar muchos más estados de los que conserva en esta
 * estructura. El límite de traza impide que una búsqueda grande convierta la
 * visualización en una fuga de memoria.
 */
struct ExecutionNode
{
    /** Identificador estable dentro de la traza retenida. */
    unsigned long id;

    /** Identificador del ancestro retenido al que se dibujará la conexión. */
    unsigned long parentId;

    /** Indica si existe un padre retenido en detalle. */
    bool hasParent;

    /** Índice del próximo movimiento pendiente de decidir. */
    int index;

    /** Monto acumulado por esta ruta, en centavos. */
    int sumCents;

    /** Profundidad lógica, usada por el layout por capas. */
    int depth;

    /** Decisión tomada para llegar aquí: incluir u omitir el último monto. */
    bool tookValue;

    /** Resultado observable de este estado. */
    NodeStatus status;

    /** Inicializa un nodo neutro. */
    ExecutionNode()
        : id(0), parentId(0), hasParent(false),
          index(0), sumCents(0), depth(0), tookValue(false),
          status(NODE_QUEUED)
    {
    }
};

#endif
