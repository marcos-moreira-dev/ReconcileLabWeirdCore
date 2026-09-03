package com.marcosmoreiradev.reconcilelab.ui.workspace;

import com.marcosmoreiradev.reconcilelab.domain.ExecutionSnapshot;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TraceRetentionPresentationTest {

    @Test
    void distingueBusquedaCompletaDeTrazaVisualAcotada() {
        ExecutionSnapshot snapshot =
                snapshot(
                        2_500,
                        10_000,
                        true,
                        false);

        assertTrue(
                TraceRetentionPresentation.isLimited(snapshot));

        assertTrue(
                TraceRetentionPresentation.title(snapshot)
                        .contains("2500"));

        assertTrue(
                TraceRetentionPresentation.detail(snapshot)
                        .contains("10000"));

        assertTrue(
                TraceRetentionPresentation.detail(snapshot)
                        .contains("continúa"));
    }

    @Test
    void noMuestraAvisoMientrasTodaLaTrazaCabe() {
        ExecutionSnapshot snapshot =
                snapshot(
                        20,
                        0,
                        false,
                        true);

        assertFalse(
                TraceRetentionPresentation.isLimited(snapshot));
    }

    private ExecutionSnapshot snapshot(
            int retained,
            long outside,
            boolean running,
            boolean completed) {

        List<com.marcosmoreiradev.reconcilelab.domain.ExecutionNode> nodes =
                java.util.stream.IntStream.range(0, retained)
                        .mapToObj(index ->
                                new com.marcosmoreiradev.reconcilelab.domain.ExecutionNode(
                                        index + 1L,
                                        0,
                                        false,
                                        0,
                                        0,
                                        0,
                                        false,
                                        com.marcosmoreiradev.reconcilelab.domain.NodeStatus.COMPLETED))
                        .toList();

        return new ExecutionSnapshot(
                nodes,
                List.of(),
                List.of(),
                retained + outside,
                retained + outside,
                0,
                0,
                outside,
                0,
                retained + outside,
                0,
                running ? 1 : 0,
                running,
                completed,
                0,
                0);
    }
}
