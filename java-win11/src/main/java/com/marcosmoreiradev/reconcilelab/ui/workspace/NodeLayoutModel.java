package com.marcosmoreiradev.reconcilelab.ui.workspace;

import com.marcosmoreiradev.reconcilelab.domain.ExecutionNode;
import com.marcosmoreiradev.reconcilelab.domain.ExecutionSnapshot;
import com.marcosmoreiradev.reconcilelab.io.MoneyText;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Layout incremental por profundidad.
 *
 * <p>Conserva las posiciones existentes para que el grafo no "salte" cada vez
 * que llega un snapshot nuevo. Cada profundidad mantiene un siguiente X propio,
 * por lo que dos tarjetas de la misma fila nunca se superponen.</p>
 */
final class NodeLayoutModel {

    record NodeBox(double x, double y, double width, double height) {
        double centerX() {
            return x + width / 2.0;
        }

        double centerY() {
            return y + height / 2.0;
        }

        boolean contains(double px, double py) {
            return px >= x && px <= x + width
                    && py >= y && py <= y + height;
        }
    }

    private static final double NODE_MIN_W = 190;
    private static final double NODE_MAX_W = 300;
    private static final double NODE_H = 84;
    private static final double GAP_X = 34;
    private static final double GAP_Y = 46;

    private final Map<Long, NodeBox> boxes = new LinkedHashMap<>();
    private final Map<Integer, Double> nextXByDepth = new HashMap<>();

    private long lastTick = -1;
    private int lastNodeCount;

    private double virtualWidth = 1_200;
    private double virtualHeight = 800;

    void update(ExecutionSnapshot snapshot) {
        boolean reset = snapshot.logicalTick() < lastTick
                || snapshot.nodes().size() < lastNodeCount;

        if (reset) {
            clear();
        }

        for (ExecutionNode node : snapshot.nodes()) {
            if (boxes.containsKey(node.id())) {
                continue;
            }

            double x = nextXByDepth.getOrDefault(node.depth(), 30.0);

            if (!nextXByDepth.containsKey(node.depth()) && node.hasParent()) {
                NodeBox parent = boxes.get(node.parentId());
                if (parent != null) {
                    x = Math.max(30.0, parent.x());
                }
            }

            double width = nodeWidth(node, snapshot.itemCount());
            double y = 30 + node.depth() * (NODE_H + GAP_Y);

            boxes.put(node.id(), new NodeBox(x, y, width, NODE_H));
            nextXByDepth.put(node.depth(), x + width + GAP_X);

            virtualWidth = Math.max(virtualWidth, x + width + 80);
            virtualHeight = Math.max(virtualHeight, y + NODE_H + 80);
        }

        lastTick = snapshot.logicalTick();
        lastNodeCount = snapshot.nodes().size();
    }

    void clear() {
        boxes.clear();
        nextXByDepth.clear();
        virtualWidth = 1_200;
        virtualHeight = 800;
        lastTick = -1;
        lastNodeCount = 0;
    }

    NodeBox box(long nodeId) {
        return boxes.get(nodeId);
    }

    Map<Long, NodeBox> boxes() {
        return boxes;
    }

    double virtualWidth() {
        return virtualWidth;
    }

    double virtualHeight() {
        return virtualHeight;
    }

    private double nodeWidth(ExecutionNode node, int itemCount) {
        String title = "ESTADO DE BÚSQUEDA #" + node.id();
        String movement = "Movimiento: " + node.index() + " / " + itemCount;
        String amount = "Acumulado: " + MoneyText.formatCents(node.sumCents());

        int longest = Math.max(
                title.length(),
                Math.max(movement.length(), amount.length()));

        double width = 26 + longest * 7.6;
        return Math.max(NODE_MIN_W, Math.min(NODE_MAX_W, width));
    }
}
