package com.marcosmoreiradev.reconcilelab.ui.workspace;

import com.marcosmoreiradev.reconcilelab.domain.ExecutionNode;
import com.marcosmoreiradev.reconcilelab.domain.ExecutionSnapshot;
import com.marcosmoreiradev.reconcilelab.ui.workspace.SearchCanvasRenderSettings.DetailLevel;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Renderizador del viewport del grafo.
 *
 * <p>Aplica culling y conexiones, y delega cada tarjeta a
 * {@link SearchNodePainter}. El nivel de detalle depende únicamente de
 * {@link SearchCanvasRenderSettings}.</p>
 */
final class SearchCanvasPainter {

    private static final Color BACKGROUND =
            Color.web("#3f4650");

    private static final Color EDGE =
            Color.web("#727d89");

    private final SearchNodePainter nodePainter =
            new SearchNodePainter();

    /**
     * Dibuja el viewport completo.
     */
    void paint(
            GraphicsContext graphics,
            ExecutionSnapshot snapshot,
            NodeLayoutModel layout,
            long selectedNodeId,
            double offsetX,
            double offsetY,
            double scale,
            double width,
            double height) {

        graphics.setFill(BACKGROUND);

        graphics.fillRect(
                0,
                0,
                width,
                height);

        if (snapshot == null) {
            return;
        }

        DetailLevel detail =
                SearchCanvasRenderSettings.detailLevel(
                        scale);

        drawEdges(
                graphics,
                snapshot,
                layout,
                offsetX,
                offsetY,
                scale,
                detail);

        for (ExecutionNode node : snapshot.nodes()) {
            NodeLayoutModel.NodeBox box =
                    layout.box(node.id());

            if (box == null
                    || !isVisible(
                            box,
                            offsetX,
                            offsetY,
                            scale,
                            width,
                            height)) {

                continue;
            }

            nodePainter.paint(
                    graphics,
                    snapshot,
                    node,
                    box,
                    selectedNodeId,
                    offsetX,
                    offsetY,
                    scale,
                    detail);
        }

    }

    private void drawEdges(
            GraphicsContext graphics,
            ExecutionSnapshot snapshot,
            NodeLayoutModel layout,
            double offsetX,
            double offsetY,
            double scale,
            DetailLevel detail) {

        graphics.setStroke(
                detail == DetailLevel.OVERVIEW
                        ? Color.web("#66717c")
                        : EDGE);

        graphics.setLineWidth(
                detail == DetailLevel.OVERVIEW
                        ? 0.75
                        : 1.0);

        for (ExecutionNode node : snapshot.nodes()) {
            if (!node.hasParent()) {
                continue;
            }

            NodeLayoutModel.NodeBox parent =
                    layout.box(
                            node.parentId());

            NodeLayoutModel.NodeBox child =
                    layout.box(
                            node.id());

            if (parent == null
                    || child == null) {

                continue;
            }

            double x1 =
                    toScreen(
                            parent.centerX(),
                            offsetX,
                            scale);

            double y1 =
                    toScreen(
                            parent.y() + parent.height(),
                            offsetY,
                            scale);

            double x2 =
                    toScreen(
                            child.centerX(),
                            offsetX,
                            scale);

            double y2 =
                    toScreen(
                            child.y(),
                            offsetY,
                            scale);

            double midY =
                    (y1 + y2) / 2.0;

            graphics.strokeLine(
                    x1,
                    y1,
                    x1,
                    midY);

            graphics.strokeLine(
                    x1,
                    midY,
                    x2,
                    midY);

            graphics.strokeLine(
                    x2,
                    midY,
                    x2,
                    y2);
        }
    }

    private boolean isVisible(
            NodeLayoutModel.NodeBox box,
            double offsetX,
            double offsetY,
            double scale,
            double viewportWidth,
            double viewportHeight) {

        double x =
                toScreen(
                        box.x(),
                        offsetX,
                        scale);

        double y =
                toScreen(
                        box.y(),
                        offsetY,
                        scale);

        double width =
                box.width() * scale;

        double height =
                box.height() * scale;

        return x + width >= 0
                && y + height >= 0
                && x <= viewportWidth
                && y <= viewportHeight;
    }

    private double toScreen(
            double world,
            double offset,
            double scale) {

        return (world - offset) * scale;
    }
}
