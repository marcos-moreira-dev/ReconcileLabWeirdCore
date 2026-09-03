package com.marcosmoreiradev.reconcilelab.engine;

import com.marcosmoreiradev.reconcilelab.domain.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Motor puro de conciliación por monto.
 *
 * <p>Porta la semántica de SearchEngine de la edición C++/wxWidgets, no su
 * sintaxis. Mantiene una frontera LIFO, genera decisiones INCLUIR/OMITIR,
 * aplica poda segura sobre montos positivos y conserva una traza acotada.</p>
 */
public final class SearchEngine {

    private record SearchTask(
            int index,
            int sumCents,
            int depth,
            long parentTraceId,
            boolean hasParentTrace,
            boolean tookValue,
            long selectionMask) {
    }

    private record TraceResult(long nodeId, boolean created) {
    }

    private ProblemInstance problem;
    private SearchStrategy strategy = SearchStrategy.PRUNING;

    private int[] suffixSum = new int[0];
    private final Deque<SearchTask> stack = new ArrayDeque<>();
    private final List<ExecutionNode> nodes = new ArrayList<>();
    private final List<CompatibleSolution> solutions = new ArrayList<>();
    private final List<RunEvent> events = new ArrayList<>();

    private long nextNodeId = 1;
    private int traceNodeLimit = 2_500;
    private int solutionLimit = 100;
    private int eventLimit = 250;

    private long tick;
    private long visited;
    private long pruned;
    private long matches;
    private long unretainedTraceNodes;
    private long hiddenSolutions;
    private long potentialStates;
    private long avoidedStates;

    private boolean started;
    private boolean complete = true;
    private boolean completionEventRecorded;

    public void reset(ProblemInstance problem) {
        this.problem = problem;

        suffixSum = new int[problem.itemCount() + 1];
        for (int i = problem.itemCount() - 1; i >= 0; i--) {
            suffixSum[i] = suffixSum[i + 1] + problem.valuesCents().get(i);
        }

        stack.clear();
        nodes.clear();
        solutions.clear();
        events.clear();

        stack.addLast(new SearchTask(0, 0, 0, 0, false, false, 0));

        nextNodeId = 1;
        tick = 0;
        visited = 0;
        pruned = 0;
        matches = 0;
        unretainedTraceNodes = 0;
        hiddenSolutions = 0;
        potentialStates = fullSubtreeSize(problem.itemCount());
        avoidedStates = 0;
        started = false;
        complete = false;
        completionEventRecorded = false;

        addEvent(RunEventType.READY, 0, "Caso preparado para conciliación.");
    }

    public void setStrategy(SearchStrategy strategy) {
        this.strategy = strategy;
    }

    public SearchStrategy getStrategy() {
        return strategy;
    }

    public void setTraceNodeLimit(int traceNodeLimit) {
        this.traceNodeLimit = Math.max(0, traceNodeLimit);
    }

    public boolean runBatch(long budget) {
        requireProblem();

        if (complete) {
            return true;
        }

        if (!started) {
            started = true;
            addEvent(RunEventType.STARTED, 0, "Búsqueda de conciliación iniciada.");
        }

        long used = 0;

        while (!stack.isEmpty() && used < budget) {
            SearchTask task = stack.removeLast();

            tick++;
            visited++;
            used++;

            if (task.sumCents() == problem.targetCents()) {
                TraceResult trace = addTraceNode(task, NodeStatus.MATCH);
                recordSolution(task);

                int remaining = problem.itemCount() - task.index();
                long subtree = fullSubtreeSize(remaining);
                if (subtree > 0) {
                    avoidedStates += subtree - 1;
                }

                if (matches <= 40 || matches % 100 == 0) {
                    addEvent(
                            RunEventType.MATCH,
                            trace.created() ? trace.nodeId() : 0,
                            "Combinación compatible encontrada. Total compatibles: "
                                    + matches + ".");
                }

                if (problem.objective() == SearchObjective.FIRST) {
                    stack.clear();
                    complete = true;
                    break;
                }

                continue;
            }

            if (shouldPrune(task)) {
                pruned++;

                int remaining = problem.itemCount() - task.index();
                long subtree = fullSubtreeSize(remaining);
                if (subtree > 0) {
                    avoidedStates += subtree - 1;
                }

                TraceResult trace = addTraceNode(task, NodeStatus.PRUNED);
                maybeRecordPruneEvent(trace.created() ? trace.nodeId() : 0);
                continue;
            }

            if (task.index() >= problem.itemCount()) {
                addTraceNode(task, NodeStatus.COMPLETED);
                continue;
            }

            TraceResult trace = addTraceNode(task, NodeStatus.COMPLETED);

            long parentForChildren =
                    trace.created() ? trace.nodeId() : task.parentTraceId();

            boolean hasParentForChildren =
                    trace.created() || task.hasParentTrace();

            pushChildren(task, parentForChildren, hasParentForChildren);
        }

        if (stack.isEmpty()) {
            complete = true;
        }

        if (complete && !completionEventRecorded) {
            completionEventRecorded = true;
            addEvent(
                    RunEventType.COMPLETED,
                    0,
                    "Búsqueda de conciliación completada con "
                            + matches
                            + (matches == 1
                            ? " combinación compatible."
                            : " combinaciones compatibles."));
        }

        return complete;
    }

    public void step() {
        runBatch(1);
    }

    public ExecutionSnapshot snapshot() {
        requireProblem();

        return new ExecutionSnapshot(
                nodes,
                solutions,
                events,
                tick,
                visited,
                pruned,
                matches,
                unretainedTraceNodes,
                hiddenSolutions,
                potentialStates,
                avoidedStates,
                stack.size(),
                !complete,
                complete,
                problem.targetCents(),
                problem.itemCount());
    }

    public boolean isComplete() {
        return complete;
    }

    private boolean shouldPrune(SearchTask task) {
        if (strategy != SearchStrategy.PRUNING) {
            return false;
        }

        if (task.sumCents() > problem.targetCents()) {
            return true;
        }

        if (task.index() >= 0 && task.index() < suffixSum.length) {
            return task.sumCents() + suffixSum[task.index()]
                    < problem.targetCents();
        }

        return false;
    }

    private TraceResult addTraceNode(SearchTask task, NodeStatus status) {
        if (nodes.size() >= traceNodeLimit) {
            unretainedTraceNodes++;
            return new TraceResult(task.parentTraceId(), false);
        }

        long id = nextNodeId++;

        nodes.add(new ExecutionNode(
                id,
                task.parentTraceId(),
                task.hasParentTrace(),
                task.index(),
                task.sumCents(),
                task.depth(),
                task.tookValue(),
                status));

        return new TraceResult(id, true);
    }

    private void pushChildren(
            SearchTask task,
            long parentTraceId,
            boolean hasParentTrace) {

        if (task.index() >= problem.itemCount()) {
            return;
        }

        stack.addLast(new SearchTask(
                task.index() + 1,
                task.sumCents(),
                task.depth() + 1,
                parentTraceId,
                hasParentTrace,
                false,
                task.selectionMask()));

        long takeMask =
                task.selectionMask() | (1L << task.index());

        stack.addLast(new SearchTask(
                task.index() + 1,
                task.sumCents() + problem.valuesCents().get(task.index()),
                task.depth() + 1,
                parentTraceId,
                hasParentTrace,
                true,
                takeMask));
    }

    private long fullSubtreeSize(int remainingDecisions) {
        if (remainingDecisions <= 0) {
            return 1;
        }

        return (1L << (remainingDecisions + 1)) - 1L;
    }

    private void recordSolution(SearchTask task) {
        matches++;

        if (solutions.size() >= solutionLimit) {
            hiddenSolutions++;
            return;
        }

        solutions.add(new CompatibleSolution(
                task.selectionMask(),
                task.sumCents(),
                tick));
    }

    private void addEvent(
            RunEventType type,
            long nodeId,
            String message) {

        if (events.size() >= eventLimit) {
            events.removeFirst();
        }

        events.add(new RunEvent(tick, type, nodeId, message));
    }

    private void maybeRecordPruneEvent(long nodeId) {
        if (pruned <= 40 || pruned % 250 == 0) {
            addEvent(
                    RunEventType.PRUNED,
                    nodeId,
                    "Ruta candidata descartada. Total descartadas: "
                            + pruned + ".");
        }
    }

    private void requireProblem() {
        if (problem == null) {
            throw new IllegalStateException("No hay un problema cargado.");
        }
    }
}
