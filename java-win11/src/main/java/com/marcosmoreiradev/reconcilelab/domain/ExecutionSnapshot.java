package com.marcosmoreiradev.reconcilelab.domain;

import java.util.List;

/**
 * Fotografía inmutable del estado observable del motor.
 */
public record ExecutionSnapshot(
        List<ExecutionNode> nodes,
        List<CompatibleSolution> solutions,
        List<RunEvent> events,
        long logicalTick,
        long visited,
        long pruned,
        long matches,
        long unretainedTraceNodes,
        long hiddenSolutions,
        long potentialStates,
        long avoidedStates,
        long pendingTasks,
        boolean running,
        boolean completed,
        int targetCents,
        int itemCount) {

    public ExecutionSnapshot {
        nodes = List.copyOf(nodes);
        solutions = List.copyOf(solutions);
        events = List.copyOf(events);
    }
}
