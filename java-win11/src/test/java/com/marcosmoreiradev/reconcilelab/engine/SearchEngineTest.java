package com.marcosmoreiradev.reconcilelab.engine;

import com.marcosmoreiradev.reconcilelab.domain.ExecutionSnapshot;
import com.marcosmoreiradev.reconcilelab.domain.ProblemInstance;
import com.marcosmoreiradev.reconcilelab.domain.SearchObjective;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SearchEngineTest {

    @Test
    void encuentraVariasCombinacionesYContabilizaElArbol() {
        ProblemInstance problem = new ProblemInstance(
                "multiple",
                "test",
                List.of(20, 30, 50, 70),
                100,
                SearchObjective.ALL);

        SearchEngine engine = new SearchEngine();
        engine.reset(problem);
        engine.setStrategy(SearchStrategy.PRUNING);

        while (!engine.isComplete()) {
            engine.runBatch(100);
        }

        ExecutionSnapshot snapshot = engine.snapshot();

        assertTrue(snapshot.completed());
        assertTrue(snapshot.matches() >= 2);
        assertTrue(snapshot.solutions().size() >= 2);
        assertEquals(31, snapshot.potentialStates());
        assertEquals(
                snapshot.potentialStates(),
                snapshot.visited() + snapshot.avoidedStates());
    }

    @Test
    void laPodaConservaElNumeroDeSoluciones() {
        ProblemInstance problem = new ProblemInstance(
                "poda",
                "test",
                List.of(10, 20, 30, 40, 90),
                100,
                SearchObjective.ALL);

        SearchEngine exhaustive = run(problem, SearchStrategy.EXHAUSTIVE);
        SearchEngine pruned = run(problem, SearchStrategy.PRUNING);

        ExecutionSnapshot a = exhaustive.snapshot();
        ExecutionSnapshot b = pruned.snapshot();

        assertEquals(a.matches(), b.matches());
        assertTrue(b.visited() <= a.visited());

        assertEquals(
                a.potentialStates(),
                a.visited() + a.avoidedStates());

        assertEquals(
                b.potentialStates(),
                b.visited() + b.avoidedStates());
    }

    private SearchEngine run(
            ProblemInstance problem,
            SearchStrategy strategy) {

        SearchEngine engine = new SearchEngine();
        engine.reset(problem);
        engine.setStrategy(strategy);

        while (!engine.isComplete()) {
            engine.runBatch(1_000);
        }

        return engine;
    }
}
