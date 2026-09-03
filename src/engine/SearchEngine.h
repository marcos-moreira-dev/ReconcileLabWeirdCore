#ifndef RECONCILELAB_SEARCH_ENGINE_H
#define RECONCILELAB_SEARCH_ENGINE_H

#include "../model/ProblemInstance.h"
#include "../model/ExecutionSnapshot.h"
#include <vector>

/**
 * Estrategias de búsqueda expuestas por V1.
 *
 * Ambas recorren el mismo árbol binario de decisiones INCLUIR/OMITIR.
 * STRATEGY_EXHAUSTIVE no corta rutas antes de tiempo.
 * STRATEGY_PRUNING aplica reglas demostrablemente seguras para montos
 * positivos. No inventamos un tercer "backtracking sin poda" si en este
 * problema sería esencialmente la misma exploración exhaustiva en profundidad.
 */
enum SearchStrategy
{
    STRATEGY_EXHAUSTIVE = 0,
    STRATEGY_PRUNING = 1
};

/**
 * Unidad interna de trabajo pendiente.
 *
 * Un SearchTask representa una pregunta parcial:
 * "ya decidí los movimientos [0, index); ¿qué hago con el resto?".
 *
 * No es una tarjeta gráfica. Tampoco es un hilo. La GUI sólo llega a conocer
 * aquellos estados que el motor decide retener como ExecutionNode.
 */
struct SearchTask
{
    /** Próximo movimiento que todavía no fue decidido. */
    int index;

    /** Monto acumulado por las decisiones anteriores. */
    int sumCents;

    /** Profundidad lógica dentro del árbol de decisiones. */
    int depth;

    /** Ancestro que sí existe en la traza visual retenida. */
    unsigned long parentTraceId;

    /** Indica si parentTraceId representa realmente un nodo retenido. */
    bool hasParentTrace;

    /** Decisión tomada para llegar a esta tarea. */
    bool tookValue;

    /** Bits de movimientos incluidos en la ruta parcial. */
    unsigned long selectionMask;

    /** Construye la tarea raíz vacía. */
    SearchTask()
        : index(0), sumCents(0), depth(0),
          parentTraceId(0), hasParentTrace(false), tookValue(false),
          selectionMask(0)
    {
    }
};

/**
 * Motor puro de conciliación.
 *
 * Responsabilidades:
 * - mantener la frontera de búsqueda;
 * - expandir decisiones INCLUIR/OMITIR;
 * - aplicar poda cuando corresponde;
 * - contar trabajo visitado y evitado;
 * - conservar una muestra acotada de nodos, eventos y soluciones.
 *
 * No conoce wxWidgets, ventanas, timers ni coordenadas. Esa separación permite
 * ejecutar pruebas del algoritmo de forma independiente de Windows XP.
 */
class SearchEngine
{
public:
    /** Construye un motor vacío con poda como estrategia predeterminada. */
    SearchEngine();

    /**
     * Limpia cualquier ejecución anterior y prepara un problema nuevo.
     *
     * También precalcula `m_suffixSum`, usado por una de las reglas de poda.
     */
    void Reset(const ProblemInstance& problem);

    /** Cambia la estrategia que se aplicará a los siguientes estados. */
    void SetStrategy(SearchStrategy strategy);

    /**
     * Limita cuántos estados se retienen para dibujo detallado.
     *
     * Alcanzar este límite no detiene el algoritmo; sólo reduce el detalle
     * visual almacenado.
     */
    void SetTraceNodeLimit(unsigned long limit);

    /**
     * Procesa como máximo `budget` tareas lógicas.
     *
     * @return true cuando el objetivo actual ya está completo.
     */
    bool RunBatch(unsigned long budget);

    /** Atajo pedagógico equivalente a `RunBatch(1)`. */
    void Step();

    /**
     * Construye una copia desacoplada del estado observable.
     *
     * La copia permite que RuntimeController libere su bloqueo antes de que la
     * GUI empiece a dibujar.
     */
    ExecutionSnapshot Snapshot() const;

    /** Indica si ya no existe trabajo requerido por el objetivo actual. */
    bool IsComplete() const;

private:
    /**
     * Decide si una tarea puede descartarse sin perder una solución válida.
     *
     * Las reglas son correctas sólo porque V1 exige montos positivos.
     */
    bool ShouldPrune(const SearchTask& task) const;

    /**
     * Convierte una tarea lógica en nodo de traza si queda presupuesto visual.
     *
     * @param created sale true sólo si se materializó un ExecutionNode.
     * @return id del nodo creado o el ancestro retenido más cercano.
     */
    unsigned long AddTraceNode(
        const SearchTask& task,
        NodeStatus status,
        bool& created);

    /**
     * Genera los dos descendientes del próximo movimiento.
     *
     * Se apila OMITIR primero e INCLUIR después porque `m_stack` es LIFO; así
     * INCLUIR será el siguiente estado explorado.
     */
    void PushChildren(
        const SearchTask& task,
        unsigned long parentTraceId,
        bool hasParentTrace);

    /**
     * Cuenta nodos de ocurrencia de un subárbol binario completo.
     *
     * Con d decisiones restantes: 2^(d+1)-1 nodos.
     */
    unsigned long FullSubtreeSize(int remainingDecisions) const;

    /** Registra una solución respetando el límite de almacenamiento visible. */
    void RecordSolution(const SearchTask& task);

    /** Agrega un hito a la línea de tiempo acotada. */
    void AddEvent(
        RunEventType type,
        unsigned long nodeId,
        const std::string& message);

    /**
     * Muestrea eventos de poda.
     *
     * Registrar absolutamente todas las podas haría que observar el algoritmo
     * se volviera parte importante de su costo.
     */
    void MaybeRecordPruneEvent(unsigned long nodeId);

private:
    /** Problema actualmente cargado. */
    ProblemInstance m_problem;

    /** Estrategia usada por ShouldPrune. */
    SearchStrategy m_strategy;

    /**
     * m_suffixSum[i] = suma de todos los candidatos desde i hasta el final.
     * Permite saber en O(1) si ni usando todo lo restante se alcanza el target.
     */
    std::vector<int> m_suffixSum;

    /** Frontera LIFO de búsqueda en profundidad. */
    std::vector<SearchTask> m_stack;

    /** Traza visual retenida, deliberadamente acotada. */
    std::vector<ExecutionNode> m_nodes;

    /** Soluciones retenidas para presentación. */
    std::vector<CompatibleSolution> m_solutions;

    /** Eventos significativos retenidos para timeline. */
    std::vector<RunEvent> m_events;

    /** Identificador del próximo nodo de traza. */
    unsigned long m_nextNodeId;

    /** Límites de instrumentación/presentación. */
    unsigned long m_traceNodeLimit;
    unsigned long m_solutionLimit;
    unsigned long m_eventLimit;

    /** Contadores lógicos de la ejecución. */
    unsigned long m_tick;
    unsigned long m_visited;
    unsigned long m_pruned;
    unsigned long m_matches;
    unsigned long m_unretainedTraceNodes;
    unsigned long m_hiddenSolutions;
    unsigned long m_potentialStates;
    unsigned long m_avoidedStates;

    /** Banderas de ciclo de vida del motor. */
    bool m_started;
    bool m_complete;
    bool m_completionEventRecorded;
};

#endif
