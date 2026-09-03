package com.marcosmoreiradev.reconcilelab.ui.component;

import com.marcosmoreiradev.reconcilelab.domain.ExecutionSnapshot;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExecutionProgressModelTest {

    @Test
    void completadoPuedeSerCienAunqueElEspacioSeaParcial() {
        ExecutionSnapshot snapshot =
                snapshot(
                        5,
                        6,
                        127,
                        true);

        ExecutionProgressModel model =
                ExecutionProgressModel.from(snapshot);

        assertEquals(11, model.accountedStates());
        assertEquals(11.0 / 127.0, model.accountedRatio(), 1e-9);
        assertEquals(1.0, model.executionRatio(), 1e-9);
    }

    @Test
    void mientrasCorreUsaElEspacioComoReferenciaVisual() {
        ExecutionSnapshot snapshot =
                snapshot(
                        10,
                        20,
                        100,
                        false);

        ExecutionProgressModel model =
                ExecutionProgressModel.from(snapshot);

        assertEquals(0.30, model.accountedRatio(), 1e-9);
        assertEquals(0.30, model.executionRatio(), 1e-9);
    }

    private ExecutionSnapshot snapshot(
            long visited,
            long avoided,
            long potential,
            boolean completed) {

        return new ExecutionSnapshot(
                List.of(),
                List.of(),
                List.of(),
                visited,
                visited,
                0,
                0,
                0,
                0,
                potential,
                avoided,
                completed ? 0 : 1,
                !completed,
                completed,
                10_000,
                6);
    }
}
