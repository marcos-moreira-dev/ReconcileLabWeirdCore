package com.marcosmoreiradev.reconcilelab.ui.export;

import com.marcosmoreiradev.reconcilelab.domain.ExecutionNode;
import com.marcosmoreiradev.reconcilelab.domain.ExecutionSnapshot;
import com.marcosmoreiradev.reconcilelab.io.MoneyText;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;

/** Pinta conexiones y tarjetas del póster. */
final class StudyPosterGraphPainter {

    void paint(
            Graphics2D graphics,
            ExecutionSnapshot snapshot,
            StudyPosterLayout layout,
            int originX,
            int originY) {

        drawEdges(
                graphics,
                snapshot,
                layout,
                originX,
                originY);

        drawNodes(
                graphics,
                snapshot,
                layout,
                originX,
                originY);

        if (snapshot.unretainedTraceNodes() > 0) {
            graphics.setFont(
                    new Font(
                            "Segoe UI",
                            Font.PLAIN,
                            12));

            graphics.setColor(Color.WHITE);

            graphics.drawString(
                    snapshot.unretainedTraceNodes()
                            + " estados adicionales fueron procesados fuera de la vista detallada.",
                    originX + 18,
                    originY + 20);
        }
    }

    private void drawEdges(
            Graphics2D graphics,
            ExecutionSnapshot snapshot,
            StudyPosterLayout layout,
            int originX,
            int originY) {

        graphics.setColor(
                new Color(
                        115,
                        125,
                        136));

        graphics.setStroke(
                new BasicStroke(
                        1.0f));

        for (ExecutionNode node : snapshot.nodes()) {
            if (!node.hasParent()) {
                continue;
            }

            StudyPosterLayout.NodeBox parent =
                    layout.box(
                            node.parentId());

            StudyPosterLayout.NodeBox child =
                    layout.box(
                            node.id());

            if (parent == null
                    || child == null) {

                continue;
            }

            int x1 =
                    originX
                            + parent.centerX();

            int y1 =
                    originY
                            + parent.y()
                            + parent.height();

            int x2 =
                    originX
                            + child.centerX();

            int y2 =
                    originY
                            + child.y();

            int midY =
                    (y1 + y2) / 2;

            graphics.draw(
                    new Line2D.Double(
                            x1,
                            y1,
                            x1,
                            midY));

            graphics.draw(
                    new Line2D.Double(
                            x1,
                            midY,
                            x2,
                            midY));

            graphics.draw(
                    new Line2D.Double(
                            x2,
                            midY,
                            x2,
                            y2));
        }
    }

    private void drawNodes(
            Graphics2D graphics,
            ExecutionSnapshot snapshot,
            StudyPosterLayout layout,
            int originX,
            int originY) {

        for (ExecutionNode node : snapshot.nodes()) {
            StudyPosterLayout.NodeBox box =
                    layout.box(
                            node.id());

            if (box == null) {
                continue;
            }

            int x =
                    originX
                            + box.x();

            int y =
                    originY
                            + box.y();

            graphics.setColor(
                    new Color(
                            243,
                            240,
                            223));

            graphics.fillRect(
                    x,
                    y,
                    box.width(),
                    box.height());

            graphics.setColor(
                    new Color(
                            32,
                            38,
                            44));

            graphics.drawRect(
                    x,
                    y,
                    box.width(),
                    box.height());

            int headerHeight =
                    layout.nodeHeaderHeight();

            graphics.setColor(
                    captionColor(node));

            graphics.fillRect(
                    x + 1,
                    y + 1,
                    box.width() - 2,
                    headerHeight);

            graphics.setColor(Color.WHITE);

            graphics.setFont(
                    new Font(
                            "Segoe UI",
                            Font.BOLD,
                            layout.titleFontSize()));

            graphics.drawString(
                    "ESTADO DE BÚSQUEDA #"
                            + node.id(),
                    x + 7,
                    y + headerHeight - 5);

            graphics.setColor(
                    new Color(
                            22,
                            26,
                            29));

            graphics.setFont(
                    new Font(
                            "Segoe UI",
                            Font.PLAIN,
                            layout.bodyFontSize()));

            int available =
                    Math.max(
                            38,
                            box.height()
                                    - headerHeight
                                    - 8);

            int lineStep =
                    Math.max(
                            12,
                            available / 3);

            int firstY =
                    y
                            + headerHeight
                            + lineStep;

            graphics.drawString(
                    "Movimiento: "
                            + node.index()
                            + " / "
                            + snapshot.itemCount(),
                    x + 7,
                    firstY);

            graphics.drawString(
                    "Acumulado: "
                            + MoneyText.formatCents(
                                    node.sumCents()),
                    x + 7,
                    firstY + lineStep);

            graphics.drawString(
                    statusLabel(node),
                    x + 7,
                    Math.min(
                            y + box.height() - 5,
                            firstY + lineStep * 2));
        }
    }

    private Color captionColor(ExecutionNode node) {
        return switch (node.status()) {
            case PRUNED ->
                    new Color(
                            146,
                            89,
                            89);

            case MATCH ->
                    new Color(
                            67,
                            132,
                            81);

            default ->
                    new Color(
                            15,
                            111,
                            218);
        };
    }

    private String statusLabel(ExecutionNode node) {
        return switch (node.status()) {
            case PRUNED -> "DESCARTADO";
            case MATCH -> "COMPATIBLE";
            default -> "PROCESADO";
        };
    }
}
