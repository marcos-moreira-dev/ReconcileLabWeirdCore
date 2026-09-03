/**
 * Implementación del motor de búsqueda.
 *
 * Conviene leer este archivo junto a SearchEngine.h: el encabezado explica
 * responsabilidades e invariantes; aquí se ve cómo esas ideas se convierten
 * en operaciones concretas sobre una pila LIFO.
 */
#include "SearchEngine.h"
#include <sstream>

SearchEngine::SearchEngine()
    : m_strategy(STRATEGY_PRUNING),
      m_nextNodeId(1),
      m_traceNodeLimit(2500),
      m_solutionLimit(100),
      m_eventLimit(250),
      m_tick(0), m_visited(0), m_pruned(0), m_matches(0),
      m_unretainedTraceNodes(0), m_hiddenSolutions(0),
      m_potentialStates(0), m_avoidedStates(0),
      m_started(false), m_complete(true), m_completionEventRecorded(false)
{
}

void SearchEngine::Reset(const ProblemInstance& problem)
{
    m_problem = problem;

    m_suffixSum.assign(problem.valuesCents.size() + 1, 0);
    for (int i = (int)problem.valuesCents.size() - 1; i >= 0; --i)
        m_suffixSum[i] = m_suffixSum[i + 1] + problem.valuesCents[i];

    m_stack.clear();
    m_nodes.clear();
    m_solutions.clear();
    m_events.clear();

    SearchTask root;
    root.index = 0;
    root.sumCents = 0;
    root.depth = 0;
    root.hasParentTrace = false;
    root.tookValue = false;
    root.selectionMask = 0;
    m_stack.push_back(root);

    m_nextNodeId = 1;
    m_tick = 0;
    m_visited = 0;
    m_pruned = 0;
    m_matches = 0;
    m_unretainedTraceNodes = 0;
    m_hiddenSolutions = 0;
    m_potentialStates = FullSubtreeSize((int)problem.valuesCents.size());
    m_avoidedStates = 0;

    m_started = false;
    m_complete = false;
    m_completionEventRecorded = false;

    AddEvent(RUN_EVENT_READY, 0, "Caso preparado para conciliacion.");
}

void SearchEngine::SetStrategy(SearchStrategy strategy)
{
    m_strategy = strategy;
}

void SearchEngine::SetTraceNodeLimit(unsigned long limit)
{
    m_traceNodeLimit = limit;
}

bool SearchEngine::ShouldPrune(const SearchTask& task) const
{
    if (m_strategy != STRATEGY_PRUNING)
        return false;

    // Regla 1: con montos positivos, una suma que ya superó el objetivo
    // jamás puede volver a bajar al agregar candidatos posteriores.
    if (task.sumCents > m_problem.targetCents)
        return true;

    // Regla 2: si ni sumando TODOS los candidatos restantes se alcanza el
    // objetivo, ningún descendiente de esta ruta puede ser compatible.
    if (task.index >= 0 && task.index < (int)m_suffixSum.size())
    {
        if (task.sumCents + m_suffixSum[task.index] < m_problem.targetCents)
            return true;
    }

    return false;
}

unsigned long SearchEngine::AddTraceNode(const SearchTask& task, NodeStatus status, bool& created)
{
    created = false;

    if (m_nodes.size() >= m_traceNodeLimit)
    {
        ++m_unretainedTraceNodes;
        return task.parentTraceId;
    }

    ExecutionNode node;
    node.id = m_nextNodeId++;
    node.parentId = task.parentTraceId;
    node.hasParent = task.hasParentTrace;
    node.index = task.index;
    node.sumCents = task.sumCents;
    node.depth = task.depth;
    node.tookValue = task.tookValue;
    node.status = status;

    m_nodes.push_back(node);
    created = true;
    return node.id;
}

void SearchEngine::PushChildren(const SearchTask& task,
                                unsigned long parentTraceId,
                                bool hasParentTrace)
{
    if (task.index >= (int)m_problem.valuesCents.size())
        return;

    // `m_stack` es LIFO. Insertamos OMITIR primero e INCLUIR después para
    // que la siguiente extracción explore INCLUIR y el recorrido sea estable.
    SearchTask skip;
    skip.index = task.index + 1;
    skip.sumCents = task.sumCents;
    skip.depth = task.depth + 1;
    skip.parentTraceId = parentTraceId;
    skip.hasParentTrace = hasParentTrace;
    skip.tookValue = false;
    skip.selectionMask = task.selectionMask;
    m_stack.push_back(skip);

    SearchTask take;
    take.index = task.index + 1;
    take.sumCents = task.sumCents + m_problem.valuesCents[task.index];
    take.depth = task.depth + 1;
    take.parentTraceId = parentTraceId;
    take.hasParentTrace = hasParentTrace;
    take.tookValue = true;
    take.selectionMask = task.selectionMask | (1UL << task.index);
    m_stack.push_back(take);
}

unsigned long SearchEngine::FullSubtreeSize(int remainingDecisions) const
{
    if (remainingDecisions <= 0)
        return 1UL;

    // V1 admite como máximo 30 candidatos. El árbol completo tiene como
    // máximo 2^31-1 nodos, todavía representable en unsigned long de 32 bits.
    return (1UL << (remainingDecisions + 1)) - 1UL;
}

void SearchEngine::RecordSolution(const SearchTask& task)
{
    ++m_matches;

    if (m_solutions.size() >= m_solutionLimit)
    {
        ++m_hiddenSolutions;
        return;
    }

    CompatibleSolution solution;
    solution.selectionMask = task.selectionMask;
    solution.totalCents = task.sumCents;
    solution.foundAtTick = m_tick;
    m_solutions.push_back(solution);
}

void SearchEngine::AddEvent(RunEventType type,
                            unsigned long nodeId,
                            const std::string& message)
{
    RunEvent event;
    event.tick = m_tick;
    event.type = type;
    event.nodeId = nodeId;
    event.message = message;

    if (m_events.size() >= m_eventLimit)
        m_events.erase(m_events.begin());

    m_events.push_back(event);
}

void SearchEngine::MaybeRecordPruneEvent(unsigned long nodeId)
{
    // Conservamos las primeras podas y luego muestreamos periódicamente.
    // La timeline no debe crecer en proporción a cada ruta descartada.
    if (m_pruned <= 40 || (m_pruned % 250UL) == 0)
    {
        std::ostringstream msg;
        msg << "Ruta candidata descartada. Total descartadas: " << m_pruned << ".";
        AddEvent(RUN_EVENT_PRUNED, nodeId, msg.str());
    }
}

bool SearchEngine::RunBatch(unsigned long budget)
{
    if (m_complete)
        return true;

    if (!m_started)
    {
        m_started = true;
        AddEvent(RUN_EVENT_STARTED, 0, "Busqueda de conciliacion iniciada.");
    }

    unsigned long used = 0;

    while (!m_stack.empty() && used < budget)
    {
        SearchTask task = m_stack.back();
        m_stack.pop_back();

        ++m_tick;
        ++m_visited;
        ++used;

        if (task.sumCents == m_problem.targetCents)
        {
            bool created = false;
            unsigned long nodeId = AddTraceNode(task, NODE_MATCH, created);

            RecordSolution(task);

            // Con montos positivos, una ruta que ya alcanzó exactamente el
            // objetivo no necesita decidir candidatos posteriores: cualquier
            // inclusión adicional aumentaría la suma. Por eso contabilizamos
            // el subárbol restante como trabajo evitado.
            int remaining = (int)m_problem.valuesCents.size() - task.index;
            unsigned long matchSubtree = FullSubtreeSize(remaining);
            if (matchSubtree > 0)
                m_avoidedStates += matchSubtree - 1UL;

            // Igual que con las podas, mostramos las primeras coincidencias
            // y luego muestreamos. Un caso con miles de soluciones no debe
            // convertir la timeline en un cuello de botella.
            if (m_matches <= 40 || (m_matches % 100UL) == 0)
            {
                std::ostringstream msg;
                msg << "Combinacion compatible encontrada. Total compatibles: "
                    << m_matches << ".";
                AddEvent(RUN_EVENT_MATCH, created ? nodeId : 0, msg.str());
            }

            if (m_problem.objective == OBJECTIVE_FIRST)
            {
                m_stack.clear();
                m_complete = true;
                break;
            }

            // La coincidencia actual ya es suficiente; sus descendientes no
            // pueden aportar otra combinación válida sin elevar la suma.
            continue;
        }

        if (ShouldPrune(task))
        {
            ++m_pruned;

            int remaining = (int)m_problem.valuesCents.size() - task.index;
            unsigned long prunedSubtree = FullSubtreeSize(remaining);
            if (prunedSubtree > 0)
                m_avoidedStates += prunedSubtree - 1UL;

            bool created = false;
            unsigned long nodeId = AddTraceNode(task, NODE_PRUNED, created);
            MaybeRecordPruneEvent(created ? nodeId : 0);
            continue;
        }

        if (task.index >= (int)m_problem.valuesCents.size())
        {
            bool created = false;
            AddTraceNode(task, NODE_COMPLETED, created);
            continue;
        }

        bool created = false;
        unsigned long visualId = AddTraceNode(task, NODE_COMPLETED, created);

        // Si esta tarea ya no cabe en la traza, sus descendientes siguen
        // enlazándose al ancestro retenido más cercano. Así la vista conserva
        // continuidad sin obligar al motor a materializar todos los estados.
        unsigned long parentForChildren = created ? visualId : task.parentTraceId;
        bool hasParentForChildren = created || task.hasParentTrace;

        PushChildren(task, parentForChildren, hasParentForChildren);
    }

    if (m_stack.empty())
        m_complete = true;

    if (m_complete && !m_completionEventRecorded)
    {
        m_completionEventRecorded = true;

        std::ostringstream msg;
        msg << "Busqueda de conciliacion completada con " << m_matches
            << " combinacion compatible";
        if (m_matches != 1) msg << "es";
        msg << ".";
        AddEvent(RUN_EVENT_COMPLETED, 0, msg.str());
    }

    return m_complete;
}

void SearchEngine::Step()
{
    RunBatch(1);
}

ExecutionSnapshot SearchEngine::Snapshot() const
{
    ExecutionSnapshot s;
    s.nodes = m_nodes;
    s.solutions = m_solutions;
    s.events = m_events;

    s.logicalTick = m_tick;
    s.visited = m_visited;
    s.pruned = m_pruned;
    s.matches = m_matches;
    s.unretainedTraceNodes = m_unretainedTraceNodes;
    s.hiddenSolutions = m_hiddenSolutions;
    s.potentialStates = m_potentialStates;
    s.avoidedStates = m_avoidedStates;

    s.pendingTasks = (unsigned long)m_stack.size();
    s.running = !m_complete;
    s.completed = m_complete;
    s.targetCents = m_problem.targetCents;
    s.itemCount = (int)m_problem.valuesCents.size();
    return s;
}

bool SearchEngine::IsComplete() const
{
    return m_complete;
}
