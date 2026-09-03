#ifndef RECONCILELAB_EXECUTION_SNAPSHOT_H
#define RECONCILELAB_EXECUTION_SNAPSHOT_H

#include "ExecutionNode.h"
#include "CompatibleSolution.h"
#include "RunEvent.h"
#include <vector>

/**
 * Fotografía desacoplada del estado observable del motor.
 *
 * RuntimeController crea esta copia bajo bloqueo y después libera el mutex.
 * La GUI puede recorrerla y dibujarla sin mantener bloqueado al hilo de
 * búsqueda. Esta separación es la pieza central para que "paso lógico" y
 * "frame visual" no sean la misma cosa.
 */
struct ExecutionSnapshot
{
    /** Estados retenidos para representación detallada. */
    std::vector<ExecutionNode> nodes;

    /** Soluciones retenidas para mostrar al usuario. */
    std::vector<CompatibleSolution> solutions;

    /** Hitos retenidos para la línea de tiempo. */
    std::vector<RunEvent> events;

    /** Cantidad de tareas lógicas ya procesadas. */
    unsigned long logicalTick;

    /** Estados visitados realmente por el algoritmo. */
    unsigned long visited;

    /** Rutas descartadas mediante poda segura. */
    unsigned long pruned;

    /** Combinaciones compatibles encontradas. */
    unsigned long matches;

    /** Estados procesados que ya no cabían en la traza detallada. */
    unsigned long unretainedTraceNodes;

    /** Soluciones reales no retenidas por el límite de presentación. */
    unsigned long hiddenSolutions;

    /** Tamaño potencial del árbol completo TAKE/SKIP. */
    unsigned long potentialStates;

    /** Estados cuyo procesamiento pudo evitarse justificadamente. */
    unsigned long avoidedStates;

    /** Tareas que todavía esperan en la frontera de búsqueda. */
    unsigned long pendingTasks;

    /** Indicadores simples conservados por compatibilidad de presentación. */
    bool running;
    bool completed;

    /** Datos mínimos del problema que necesita el canvas. */
    int targetCents;
    int itemCount;

    /** Construye una instantánea vacía y consistente. */
    ExecutionSnapshot()
        : logicalTick(0), visited(0), pruned(0), matches(0),
          unretainedTraceNodes(0), hiddenSolutions(0),
          potentialStates(0), avoidedStates(0),
          pendingTasks(0), running(false), completed(false),
          targetCents(0), itemCount(0)
    {
    }
};

#endif
