package com.marcosmoreiradev.reconcilelab.ui.export;

import com.marcosmoreiradev.reconcilelab.domain.ExecutionSnapshot;
import com.marcosmoreiradev.reconcilelab.domain.ProblemInstance;
import com.marcosmoreiradev.reconcilelab.engine.SearchStrategy;
import com.marcosmoreiradev.reconcilelab.io.MoneyText;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

/** Pinta la cabecera pedagógica de la lámina de estudio. */
final class StudyPosterHeaderPainter {

    private static final int MOVEMENTS_PER_LINE = 5;

    int requiredHeight(ProblemInstance problem) {
        return 230 + movementLines(problem).size() * 20;
    }

    void paint(
            Graphics2D graphics,
            ProblemInstance problem,
            SearchStrategy strategy,
            ExecutionSnapshot snapshot,
            int margin,
            int headerHeight,
            int imageWidth) {

        List<String> lines = movementLines(problem);
        graphics.setColor(new Color(31, 54, 86));
        graphics.setFont(new Font("Segoe UI", Font.BOLD, 24));
        graphics.drawString("ReconcileLab — Lámina de estudio", margin, margin + 8);

        graphics.setColor(new Color(31, 37, 43));
        graphics.setFont(new Font("Segoe UI", Font.BOLD, 14));
        graphics.drawString("Caso: " + problem.name(), margin, margin + 42);

        graphics.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        graphics.drawString(
                "Monto a conciliar: " + MoneyText.formatCents(problem.targetCents()),
                margin,
                margin + 67);
        graphics.drawString("Método: " + strategy.label(), margin + 330, margin + 67);
        graphics.drawString(
                "Tick: " + snapshot.logicalTick()
                        + "   Visitados: " + snapshot.visited()
                        + "   Descartados: " + snapshot.pruned()
                        + "   Compatibles: " + snapshot.matches()
                        + "   Evitados: " + snapshot.avoidedStates(),
                margin,
                margin + 92);

        graphics.setFont(new Font("Segoe UI", Font.BOLD, 13));
        graphics.drawString("Entrada — movimientos candidatos:", margin, margin + 118);
        graphics.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        int y = margin + 140;
        for (String line : lines) {
            graphics.drawString(line, margin, y);
            y += 20;
        }

        graphics.setFont(new Font("Segoe UI", Font.BOLD, 13));
        graphics.drawString("Qué significa el diagrama:", margin, y + 7);
        graphics.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        graphics.drawString(
                "Cada tarjeta representa un estado parcial de la búsqueda: "
                        + "una decisión acumulada sobre incluir u omitir movimientos.",
                margin,
                y + 29);
        graphics.drawString(
                "Las rutas descartadas muestran trabajo que puede evitarse "
                        + "sin perder una combinación válida.",
                margin,
                y + 48);
        graphics.drawString(
                "La disposición se reorganiza por profundidad para mantener "
                        + "el texto legible; no es una captura literal del viewport.",
                margin,
                y + 67);

        graphics.setColor(new Color(150, 158, 166));
        graphics.drawLine(margin, headerHeight - 8, imageWidth - margin, headerHeight - 8);
    }

    private List<String> movementLines(ProblemInstance problem) {
        List<String> lines = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (int index = 0; index < problem.valuesCents().size(); index++) {
            if (!current.isEmpty()) {
                current.append("    ");
            }

            current.append("M")
                    .append(index + 1)
                    .append(" ")
                    .append(MoneyText.formatCents(problem.valuesCents().get(index)));

            if ((index + 1) % MOVEMENTS_PER_LINE == 0) {
                lines.add(current.toString());
                current.setLength(0);
            }
        }

        if (!current.isEmpty()) {
            lines.add(current.toString());
        }

        return List.copyOf(lines);
    }
}
