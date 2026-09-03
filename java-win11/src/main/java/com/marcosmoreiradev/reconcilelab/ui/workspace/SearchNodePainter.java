package com.marcosmoreiradev.reconcilelab.ui.workspace;

import com.marcosmoreiradev.reconcilelab.domain.ExecutionNode;
import com.marcosmoreiradev.reconcilelab.domain.ExecutionSnapshot;
import com.marcosmoreiradev.reconcilelab.io.MoneyText;
import com.marcosmoreiradev.reconcilelab.ui.workspace.SearchCanvasRenderSettings.DetailLevel;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Pinta una tarjeta de estado según el nivel de detalle del zoom.
 *
 * <p>La geometría del mundo pertenece a {@link NodeLayoutModel}; esta clase
 * sólo decide cuánto texto conviene mostrar a una escala concreta.</p>
 */
final class SearchNodePainter {

    private static final Color CARD =
            Color.web("#f3f0df");

    private static final Color CARD_BORDER =
            Color.web("#20262c");

    private static final Color TEXT =
            Color.web("#161a1d");

    /**
     * Dibuja una tarjeta.
     */
    void paint(
            GraphicsContext graphics,
            ExecutionSnapshot snapshot,
            ExecutionNode node,
            NodeLayoutModel.NodeBox box,
            long selectedNodeId,
            double offsetX,
            double offsetY,
            double scale,
            DetailLevel detail) {

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

        if (detail == DetailLevel.OVERVIEW) {
            drawOverview(
                    graphics,
                    node,
                    selectedNodeId,
                    x,
                    y,
                    width,
                    height,
                    scale);

            return;
        }

        drawSurface(
                graphics,
                node,
                selectedNodeId,
                x,
                y,
                width,
                height,
                scale);

        if (detail == DetailLevel.COMPACT) {
            drawCompactText(
                    graphics,
                    node,
                    x,
                    y,
                    scale);

            return;
        }

        drawFullText(
                graphics,
                snapshot,
                node,
                x,
                y,
                scale);
    }

    private void drawOverview(
            GraphicsContext graphics,
            ExecutionNode node,
            long selectedNodeId,
            double x,
            double y,
            double width,
            double height,
            double scale) {

        graphics.setFill(
                Color.web("#e8e6d8"));

        graphics.fillRect(
                x,
                y,
                width,
                height);

        graphics.setFill(
                captionColor(node));

        graphics.fillRect(
                x,
                y,
                width,
                Math.max(
                        3,
                        18 * scale));

        graphics.setStroke(
                node.id() == selectedNodeId
                        ? Color.WHITE
                        : Color.web("#59616a"));

        graphics.setLineWidth(
                node.id() == selectedNodeId
                        ? 2.0
                        : 0.75);

        graphics.strokeRect(
                x,
                y,
                width,
                height);
    }

    private void drawSurface(
            GraphicsContext graphics,
            ExecutionNode node,
            long selectedNodeId,
            double x,
            double y,
            double width,
            double height,
            double scale) {

        graphics.setFill(CARD);

        graphics.fillRect(
                x,
                y,
                width,
                height);

        graphics.setStroke(
                node.id() == selectedNodeId
                        ? Color.WHITE
                        : CARD_BORDER);

        graphics.setLineWidth(
                node.id() == selectedNodeId
                        ? 2.0
                        : 1.0);

        graphics.strokeRect(
                x,
                y,
                width,
                height);

        graphics.setFill(
                captionColor(node));

        graphics.fillRect(
                x + 1,
                y + 1,
                Math.max(
                        0,
                        width - 2),
                20 * scale);
    }

    private void drawCompactText(
            GraphicsContext graphics,
            ExecutionNode node,
            double x,
            double y,
            double scale) {

        double fontScale =
                Math.max(
                        0.74,
                        scale);

        graphics.setFill(Color.WHITE);

        graphics.setFont(
                Font.font(
                        "Segoe UI",
                        FontWeight.BOLD,
                        9 * fontScale));

        graphics.fillText(
                "#" + node.id(),
                x + 6 * scale,
                y + 14 * scale);

        graphics.setFill(TEXT);

        graphics.setFont(
                Font.font(
                        "Segoe UI",
                        9 * fontScale));

        graphics.fillText(
                MoneyText.formatCents(
                        node.sumCents()),
                x + 6 * scale,
                y + 49 * scale);
    }

    private void drawFullText(
            GraphicsContext graphics,
            ExecutionSnapshot snapshot,
            ExecutionNode node,
            double x,
            double y,
            double scale) {

        double fontScale =
                Math.max(
                        0.80,
                        Math.min(
                                1.25,
                                scale));

        graphics.setFill(Color.WHITE);

        graphics.setFont(
                Font.font(
                        "Segoe UI",
                        FontWeight.BOLD,
                        10 * fontScale));

        graphics.fillText(
                "ESTADO DE BÚSQUEDA #"
                        + node.id(),
                x + 6 * scale,
                y + 14 * scale);

        graphics.setFill(TEXT);

        graphics.setFont(
                Font.font(
                        "Segoe UI",
                        10 * fontScale));

        graphics.fillText(
                "Movimiento: "
                        + node.index()
                        + " / "
                        + snapshot.itemCount(),
                x + 6 * scale,
                y + 38 * scale);

        graphics.fillText(
                "Acumulado: "
                        + MoneyText.formatCents(
                                node.sumCents()),
                x + 6 * scale,
                y + 54 * scale);

        graphics.fillText(
                statusLabel(node),
                x + 6 * scale,
                y + 70 * scale);
    }

    private Color captionColor(ExecutionNode node) {
        return switch (node.status()) {
            case PRUNED -> Color.web("#925959");
            case MATCH -> Color.web("#438451");
            default -> Color.web("#0f6fda");
        };
    }

    private String statusLabel(ExecutionNode node) {
        return switch (node.status()) {
            case PRUNED -> "DESCARTADO";
            case MATCH -> "COMPATIBLE";
            default -> "PROCESADO";
        };
    }

    private double toScreen(
            double world,
            double offset,
            double scale) {

        return (world - offset) * scale;
    }
}
