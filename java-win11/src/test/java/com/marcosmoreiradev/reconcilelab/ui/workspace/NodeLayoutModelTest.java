package com.marcosmoreiradev.reconcilelab.ui.workspace;

import com.marcosmoreiradev.reconcilelab.domain.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class NodeLayoutModelTest {

    @Test
    void nodosDeLaMismaProfundidadNoSeSuperponen() {
        ExecutionSnapshot snapshot = snapshot(
                List.of(
                        node(1, 0, false, 0, 0),
                        node(2, 1, true, 1, 1),
                        node(3, 1, true, 1, 1),
                        node(4, 1, true, 1, 1),
                        node(5, 2, true, 2, 2),
                        node(6, 3, true, 2, 2)));

        NodeLayoutModel layout = new NodeLayoutModel();
        layout.update(snapshot);

        List<NodeLayoutModel.NodeBox> depthOne =
                List.of(
                        layout.box(2),
                        layout.box(3),
                        layout.box(4));

        for (int i = 0; i < depthOne.size(); i++) {
            for (int j = i + 1; j < depthOne.size(); j++) {
                assertFalse(overlaps(depthOne.get(i), depthOne.get(j)));
            }
        }
    }

    @Test
    void posicionesExistentesPermanecenEstablesAlLlegarNodosNuevos() {
        NodeLayoutModel layout = new NodeLayoutModel();

        layout.update(snapshot(
                List.of(
                        node(1, 0, false, 0, 0),
                        node(2, 1, true, 1, 1))));

        NodeLayoutModel.NodeBox before =
                layout.box(2);

        layout.update(snapshot(
                List.of(
                        node(1, 0, false, 0, 0),
                        node(2, 1, true, 1, 1),
                        node(3, 1, true, 1, 1),
                        node(4, 2, true, 2, 2))));

        assertEquals(before, layout.box(2));
        assertTrue(layout.virtualWidth() >= 1_200);
        assertTrue(layout.virtualHeight() >= 800);
    }

    private boolean overlaps(
            NodeLayoutModel.NodeBox a,
            NodeLayoutModel.NodeBox b) {

        return a.x() < b.x() + b.width()
                && a.x() + a.width() > b.x()
                && a.y() < b.y() + b.height()
                && a.y() + a.height() > b.y();
    }

    private ExecutionNode node(
            long id,
            long parentId,
            boolean hasParent,
            int index,
            int depth) {

        return new ExecutionNode(
                id,
                parentId,
                hasParent,
                index,
                1_000 + (int) id,
                depth,
                true,
                NodeStatus.COMPLETED);
    }

    private ExecutionSnapshot snapshot(
            List<ExecutionNode> nodes) {

        return new ExecutionSnapshot(
                nodes,
                List.of(),
                List.of(),
                nodes.size(),
                nodes.size(),
                0,
                0,
                0,
                0,
                127,
                0,
                0,
                false,
                false,
                10_000,
                6);
    }
}
