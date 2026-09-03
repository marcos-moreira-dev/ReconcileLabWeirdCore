package com.marcosmoreiradev.reconcilelab.io;

import com.marcosmoreiradev.reconcilelab.domain.ExecutionSnapshot;
import com.marcosmoreiradev.reconcilelab.domain.ProblemInstance;
import com.marcosmoreiradev.reconcilelab.engine.SearchEngine;
import com.marcosmoreiradev.reconcilelab.engine.SearchStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.InputStream;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CaseFileTest {

    @Test
    void todosLosPresetsCompartidosSePuedenLeer() throws Exception {
        for (String file : ResourceCaseCatalog.PRESET_FILES) {
            String resource = "/examples/" + file;

            try (InputStream input =
                         CaseFileTest.class.getResourceAsStream(resource)) {

                assertNotNull(input, resource);
                ProblemInstance problem = CaseFile.load(input);
                assertTrue(problem.targetCents() > 0);
                assertFalse(problem.valuesCents().isEmpty());
            }
        }
    }

    @Test
    void ejemplosClaveConservanSuSemantica() throws Exception {
        ProblemInstance multiple = load("02-multiple-solutions.case");
        ProblemInstance none = load("03-no-solution.case");
        ProblemInstance horizontal = load("07-horizontal-compact.case");

        assertTrue(run(multiple).matches() > 1);
        assertEquals(0, run(none).matches());

        ExecutionSnapshot horizontalSnapshot = run(horizontal);
        assertTrue(horizontalSnapshot.visited() >= 80);
        assertTrue(horizontalSnapshot.matches() >= 2);
    }


    @Test
    void losPresetsConservanLosConteosObservadosEnLaEdicionCpp() throws Exception {
        Object[][] expected = {
                {"01-small-exact.case", 1L, 5L},
                {"02-multiple-solutions.case", 2L, 51L},
                {"03-no-solution.case", 0L, 47L},
                {"04-pruning-showcase.case", 1L, 1_581L},
                {"05-medium-reconciliation.case", 1L, 844L},
                {"06-stress.case", 2_907L, 1_200_965L},
                {"07-horizontal-compact.case", 3L, 89L}
        };

        for (Object[] row : expected) {
            ExecutionSnapshot snapshot = run(load((String) row[0]));
            assertEquals((long) row[1], snapshot.matches(), row[0] + " matches");
            assertEquals((long) row[2], snapshot.visited(), row[0] + " visited");
        }
    }

    @Test
    void guardarYCargarConservaDatos(@TempDir Path tempDir) throws Exception {
        ProblemInstance original = load("01-small-exact.case");
        Path target = tempDir.resolve("roundtrip.case");

        CaseFile.save(target, original);
        ProblemInstance loaded = CaseFile.load(target);

        assertEquals(original.targetCents(), loaded.targetCents());
        assertEquals(original.valuesCents(), loaded.valuesCents());
        assertEquals(original.objective(), loaded.objective());
    }

    private ProblemInstance load(String file) throws Exception {
        try (InputStream input =
                     CaseFileTest.class.getResourceAsStream("/examples/" + file)) {
            assertNotNull(input);
            return CaseFile.load(input);
        }
    }

    private ExecutionSnapshot run(ProblemInstance problem) {
        SearchEngine engine = new SearchEngine();
        engine.reset(problem);
        engine.setStrategy(SearchStrategy.PRUNING);

        while (!engine.isComplete()) {
            engine.runBatch(1_000);
        }

        return engine.snapshot();
    }
}
