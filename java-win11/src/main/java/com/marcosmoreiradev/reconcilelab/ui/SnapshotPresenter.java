package com.marcosmoreiradev.reconcilelab.ui;

import com.marcosmoreiradev.reconcilelab.domain.CompatibleSolution;
import com.marcosmoreiradev.reconcilelab.domain.ExecutionNode;
import com.marcosmoreiradev.reconcilelab.domain.ExecutionSnapshot;
import com.marcosmoreiradev.reconcilelab.domain.NodeStatus;
import com.marcosmoreiradev.reconcilelab.domain.ProblemInstance;
import com.marcosmoreiradev.reconcilelab.domain.RunEventType;
import com.marcosmoreiradev.reconcilelab.io.MoneyText;
import com.marcosmoreiradev.reconcilelab.ui.component.ExecutionProgressModel;
import com.marcosmoreiradev.reconcilelab.ui.component.ObservedRateMeter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Convierte snapshots del runtime en información visible.
 *
 * <p>Centraliza métricas, timeline, soluciones y reconstrucción de ruta. La
 * ventana principal conserva así coordinación y eventos, no lógica de
 * presentación repetitiva.</p>
 */
final class SnapshotPresenter {

    private final MainWindowControls ui;
    private final ObservedRateMeter rateMeter = new ObservedRateMeter();
    private boolean publishingCompatibleStates;

    SnapshotPresenter(MainWindowControls ui) {
        this.ui = ui;
    }

    /** Reinicia métricas que dependen del tiempo real. */
    void resetRate() {
        rateMeter.reset();
    }

    /**
     * Publica todo lo derivable de un snapshot.
     *
     * @param snapshot fotografía actual del motor
     * @param problem caso cargado
     * @param selectedNode nodo seleccionado o {@code null}
     */
    void present(
            ExecutionSnapshot snapshot,
            ProblemInstance problem,
            ExecutionNode selectedNode) {

        presentMetrics(snapshot);
        presentTimeline(snapshot);
        presentSolutions(snapshot, problem);
        presentCompatibleStates(snapshot);
        presentSelectedRoute(snapshot, problem, selectedNode);
    }

    private void presentMetrics(ExecutionSnapshot snapshot) {
        rateMeter.sample(snapshot);
        ExecutionProgressModel progress = ExecutionProgressModel.from(snapshot);

        List<MetricRow> rows = List.of(
                metric("Tick lógico", snapshot.logicalTick()),
                metric("Visitados", snapshot.visited()),
                metric("Descartados", snapshot.pruned()),
                metric("Compatibles", snapshot.matches()),
                metric("Pendientes", snapshot.pendingTasks()),
                new MetricRow(
                        "Tasa observada",
                        String.format(Locale.ROOT, "%.0f estados/s", rateMeter.statesPerSecond())),
                metric("Evitados", snapshot.avoidedStates()),
                metric("Potenciales", snapshot.potentialStates()),
                new MetricRow(
                        "Espacio contabilizado",
                        String.format(Locale.ROOT, "%.1f%%", progress.accountedRatio() * 100)),
                metric("Traza retenida", snapshot.nodes().size()),
                metric("Fuera de traza", snapshot.unretainedTraceNodes()));

        ui.metricItems.setAll(rows);
        ui.executionProgress.setTarget(progress.executionRatio());
    }

    private MetricRow metric(String label, long value) {
        return new MetricRow(label, Long.toString(value));
    }

    private void presentTimeline(ExecutionSnapshot snapshot) {
        List<EventRow> rows = snapshot.events().stream()
                .map(event -> new EventRow(
                        Long.toString(event.tick()),
                        eventLabel(event.type()),
                        event.message()))
                .toList();

        ui.timelineItems.setAll(rows);
    }

    private void presentSolutions(ExecutionSnapshot snapshot, ProblemInstance problem) {
        List<String> labels = new ArrayList<>();

        for (int index = 0; index < snapshot.solutions().size(); index++) {
            CompatibleSolution solution = snapshot.solutions().get(index);
            labels.add(describeSolution(solution, index + 1, problem));
        }

        if (snapshot.hiddenSolutions() > 0) {
            labels.add("… " + snapshot.hiddenSolutions()
                    + " combinaciones adicionales no se retuvieron en la lista.");
        }

        ui.solutionItems.setAll(labels);
    }

    private void presentCompatibleStates(ExecutionSnapshot snapshot) {
        if (publishingCompatibleStates) {
            return;
        }

        publishingCompatibleStates = true;

        try {
            CompatibleStateOption selected =
                    ui.compatibleStates
                            .getSelectionModel()
                            .getSelectedItem();

            long previous =
                    selected == null
                            ? 0
                            : selected.nodeId();

            List<CompatibleStateOption> choices =
                    snapshot.nodes()
                            .stream()
                            .filter(node ->
                                    node.status() == NodeStatus.MATCH)
                            .map(node ->
                                    new CompatibleStateOption(
                                            node.id(),
                                            "Estado #"
                                                    + node.id()
                                                    + " — "
                                                    + MoneyText.formatCents(
                                                            node.sumCents())))
                            .toList();

            if (choices.isEmpty()) {
                presentEmptyCompatibleState(snapshot);
                return;
            }

            ui.compatibleStates.setDisable(false);

            if (!ui.compatibleStates
                    .getItems()
                    .equals(choices)) {

                ui.compatibleStates
                        .getItems()
                        .setAll(choices);
            }

            CompatibleStateOption target =
                    restoreCompatibleChoice(
                            choices,
                            previous);

            CompatibleStateOption current =
                    ui.compatibleStates
                            .getSelectionModel()
                            .getSelectedItem();

            if (current == null
                    || current.nodeId() != target.nodeId()) {

                ui.compatibleStates
                        .getSelectionModel()
                        .select(target);
            }
        } finally {
            publishingCompatibleStates = false;
        }
    }

    private void presentEmptyCompatibleState(
            ExecutionSnapshot snapshot) {

        String label =
                snapshot.matches() > 0
                        ? "Compatibles fuera de la vista detallada"
                        : "Sin resultados compatibles";

        CompatibleStateOption placeholder =
                new CompatibleStateOption(
                        0,
                        label);

        if (ui.compatibleStates
                .getItems()
                .size() != 1
                || !placeholder.equals(
                        ui.compatibleStates
                                .getItems()
                                .getFirst())) {

            ui.compatibleStates
                    .getItems()
                    .setAll(placeholder);
        }

        ui.compatibleStates
                .getSelectionModel()
                .selectFirst();

        ui.compatibleStates.setDisable(true);
    }

    private CompatibleStateOption restoreCompatibleChoice(
            List<CompatibleStateOption> choices,
            long previousNodeId) {

        return choices.stream()
                .filter(choice ->
                        choice.nodeId() == previousNodeId)
                .findFirst()
                .orElse(
                        choices.getFirst());
    }

    private void presentSelectedRoute(
            ExecutionSnapshot snapshot,
            ProblemInstance problem,
            ExecutionNode selectedNode) {

        if (selectedNode == null) {
            ui.selectedRoute.setText("Haz clic en una tarjeta del lienzo para inspeccionarla.");
            return;
        }

        List<ExecutionNode> chain = retainedChain(snapshot, selectedNode);
        StringBuilder text = new StringBuilder();

        text.append("Estado #")
                .append(selectedNode.id())
                .append("\nPosición del movimiento: ")
                .append(selectedNode.index())
                .append(" / ")
                .append(problem.itemCount())
                .append("\nMonto acumulado: ")
                .append(MoneyText.formatCents(selectedNode.sumCents()))
                .append("\nResultado: ")
                .append(statusLabel(selectedNode))
                .append("\n\nRuta retenida:");

        appendRoute(text, chain, problem);
        ui.selectedRoute.setText(text.toString());
    }

    private List<ExecutionNode> retainedChain(
            ExecutionSnapshot snapshot,
            ExecutionNode selectedNode) {

        Map<Long, ExecutionNode> byId = new HashMap<>();
        for (ExecutionNode node : snapshot.nodes()) {
            byId.put(node.id(), node);
        }

        List<ExecutionNode> chain = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        ExecutionNode cursor = selectedNode;

        while (cursor != null && seen.add(cursor.id())) {
            chain.add(cursor);
            if (!cursor.hasParent()) {
                break;
            }
            cursor = byId.get(cursor.parentId());
        }

        Collections.reverse(chain);
        return chain;
    }

    private void appendRoute(
            StringBuilder text,
            List<ExecutionNode> chain,
            ProblemInstance problem) {

        if (chain.size() <= 1) {
            text.append("\nInicio del caso.");
            return;
        }

        for (int index = 1; index < chain.size(); index++) {
            ExecutionNode node = chain.get(index);
            int movementIndex = node.index() - 1;

            text.append("\nM").append(node.index()).append(" ");

            if (movementIndex >= 0 && movementIndex < problem.valuesCents().size()) {
                text.append(MoneyText.formatCents(problem.valuesCents().get(movementIndex)));
            } else {
                text.append("(movimiento no retenido)");
            }

            text.append(" — ").append(node.tookValue() ? "incluido" : "omitido");
        }

        if (chain.getFirst().hasParent()) {
            text.append("\n\nLa parte inicial de esta ruta quedó fuera "
                    + "de la traza detallada retenida.");
        }
    }

    private String describeSolution(
            CompatibleSolution solution,
            int number,
            ProblemInstance problem) {

        List<String> parts = new ArrayList<>();

        for (int index = 0; index < problem.valuesCents().size(); index++) {
            if ((solution.selectionMask() & (1L << index)) != 0) {
                parts.add("M" + (index + 1) + " "
                        + MoneyText.formatCents(problem.valuesCents().get(index)));
            }
        }

        return "#" + number + "  " + String.join(" + ", parts)
                + " = " + MoneyText.formatCents(solution.totalCents());
    }

    private String statusLabel(ExecutionNode node) {
        return switch (node.status()) {
            case MATCH -> "Compatible";
            case PRUNED -> "Descartado";
            default -> "Procesado";
        };
    }

    private String eventLabel(RunEventType type) {
        return switch (type) {
            case READY -> "Preparado";
            case STARTED -> "Iniciada";
            case PRUNED -> "Descartado";
            case MATCH -> "Compatible";
            case COMPLETED -> "Completada";
        };
    }
}
