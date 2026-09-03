package com.marcosmoreiradev.reconcilelab.ui;

import com.marcosmoreiradev.reconcilelab.engine.SearchStrategy;
import com.marcosmoreiradev.reconcilelab.io.ResourceCaseCatalog;
import com.marcosmoreiradev.reconcilelab.runtime.ExecutionMode;
import com.marcosmoreiradev.reconcilelab.ui.component.AnimatedProgressIndicator;
import com.marcosmoreiradev.reconcilelab.ui.workspace.SearchWorkspace;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;

/**
 * Inventario explícito de controles de la ventana principal.
 *
 * <p>Separar los controles de la composición evita que {@link MainWindow}
 * termine siendo simultáneamente layout, controlador, presentador y workflow
 * de archivos.</p>
 */
final class MainWindowControls {

    final SearchWorkspace workspace = new SearchWorkspace();

    final ListView<ResourceCaseCatalog.PresetCase> examples = new ListView<>();
    final Label currentCaseTitle = new Label("Sin caso");
    final TextArea currentCaseText = readonlyArea();

    final ChoiceBox<SearchStrategy> strategy = new ChoiceBox<>();
    final ChoiceBox<ExecutionMode> mode = new ChoiceBox<>();
    final Spinner<Integer> refreshMs = new Spinner<>(20, 500, 50, 10);
    final ChoiceBox<CompatibleStateOption> compatibleStates = new ChoiceBox<>();

    final TableView<MetricRow> metrics = new TableView<>();
    final ObservableList<MetricRow> metricItems = FXCollections.observableArrayList();

    final AnimatedProgressIndicator executionProgress = new AnimatedProgressIndicator();
    final TextArea selectedRoute = readonlyArea();

    final ListView<String> solutions = new ListView<>();
    final ObservableList<String> solutionItems = FXCollections.observableArrayList();

    final TableView<EventRow> timeline = new TableView<>();
    final ObservableList<EventRow> timelineItems = FXCollections.observableArrayList();

    final Label statusLeft = new Label("Preparado");
    final Label statusTick = new Label("Tick lógico: 0");
    final Label statusMatches = new Label("Compatibles: 0");
    final Label statusRefresh = new Label("Actualización: 50 ms");

    final Button runButton = new Button("Ejecutar");
    final Button pauseButton = new Button("Pausar");
    final Button stepButton = new Button("Paso");
    final Button resetButton = new Button("Reiniciar");

    private static TextArea readonlyArea() {
        TextArea area = new TextArea();
        area.setEditable(false);
        area.setWrapText(true);
        return area;
    }
}
