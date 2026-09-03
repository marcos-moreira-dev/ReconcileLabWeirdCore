package com.marcosmoreiradev.reconcilelab.runtime;

import com.marcosmoreiradev.reconcilelab.domain.ProblemInstance;
import com.marcosmoreiradev.reconcilelab.domain.SearchObjective;
import com.marcosmoreiradev.reconcilelab.engine.SearchStrategy;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RuntimeControllerTest {

    @Test
    void pasoPausaReanudaYCompleta() throws Exception {
        ProblemInstance problem = new ProblemInstance(
                "runtime",
                "test",
                List.of(800, 1_000, 1_200, 1_400, 1_600, 1_800),
                3_800,
                SearchObjective.ALL);

        try (RuntimeController runtime = new RuntimeController()) {
            runtime.loadProblem(problem);
            runtime.setStrategy(SearchStrategy.PRUNING);
            runtime.setMode(ExecutionMode.BALANCED);

            assertEquals(ExecutionState.READY, runtime.getState());

            assertTrue(runtime.stepOnce());
            assertEquals(ExecutionState.PAUSED, runtime.getState());
            assertEquals(1, runtime.getSnapshot().logicalTick());

            assertTrue(runtime.start());
            waitUntil(
                    () -> runtime.getSnapshot().logicalTick() >= 3,
                    Duration.ofSeconds(2));

            runtime.pause();
            assertEquals(ExecutionState.PAUSED, runtime.getState());

            long pausedTick = runtime.getSnapshot().logicalTick();
            Thread.sleep(80);
            assertEquals(pausedTick, runtime.getSnapshot().logicalTick());

            assertTrue(runtime.start());

            waitUntil(
                    () -> runtime.getState() == ExecutionState.COMPLETED,
                    Duration.ofSeconds(5));

            assertTrue(runtime.getSnapshot().completed());
            assertTrue(runtime.getSnapshot().matches() >= 2);
        }
    }

    @Test
    void resetVuelveAReadyYTickCero() {
        ProblemInstance problem = new ProblemInstance(
                "reset",
                "test",
                List.of(20, 30, 50),
                50,
                SearchObjective.ALL);

        try (RuntimeController runtime = new RuntimeController()) {
            runtime.loadProblem(problem);
            runtime.stepOnce();

            assertEquals(1, runtime.getSnapshot().logicalTick());

            runtime.reset();

            assertEquals(ExecutionState.READY, runtime.getState());
            assertEquals(0, runtime.getSnapshot().logicalTick());
        }
    }

    private static void waitUntil(
            CheckedCondition condition,
            Duration timeout) throws Exception {

        Instant deadline = Instant.now().plus(timeout);

        while (Instant.now().isBefore(deadline)) {
            if (condition.evaluate()) {
                return;
            }

            Thread.sleep(20);
        }

        fail("La condición no se cumplió dentro de " + timeout);
    }

    @FunctionalInterface
    private interface CheckedCondition {
        boolean evaluate() throws Exception;
    }
}
