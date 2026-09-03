package com.marcosmoreiradev.reconcilelab.domain;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProblemInstanceTest {

    @Test
    void protegeLosInvariantesQueNecesitaElMotor() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ProblemInstance(
                        "sin target",
                        "",
                        List.of(100),
                        0,
                        SearchObjective.FIRST));

        assertThrows(
                IllegalArgumentException.class,
                () -> new ProblemInstance(
                        "sin candidatos",
                        "",
                        List.of(),
                        100,
                        SearchObjective.FIRST));

        assertThrows(
                IllegalArgumentException.class,
                () -> new ProblemInstance(
                        "negativo",
                        "",
                        List.of(100, -1),
                        100,
                        SearchObjective.FIRST));

        assertThrows(
                IllegalArgumentException.class,
                () -> new ProblemInstance(
                        "demasiados",
                        "",
                        Collections.nCopies(
                                ProblemInstance.MAX_CANDIDATES + 1,
                                100),
                        100,
                        SearchObjective.ALL));
    }

    @Test
    void copiaLaListaDeCandidatos() {
        var source = new java.util.ArrayList<>(List.of(100, 200));

        ProblemInstance problem =
                new ProblemInstance(
                        "estable",
                        "",
                        source,
                        300,
                        SearchObjective.ALL);

        source.set(0, 999);

        assertEquals(List.of(100, 200), problem.valuesCents());
        assertThrows(
                UnsupportedOperationException.class,
                () -> problem.valuesCents().add(400));
    }
}
