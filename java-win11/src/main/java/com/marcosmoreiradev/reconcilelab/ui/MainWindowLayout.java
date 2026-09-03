package com.marcosmoreiradev.reconcilelab.ui;

import com.marcosmoreiradev.reconcilelab.engine.SearchStrategy;
import com.marcosmoreiradev.reconcilelab.runtime.ExecutionMode;
import com.marcosmoreiradev.reconcilelab.ui.component.UiIconFactory;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Construye la gramática espacial de ReconcileLab para Windows 11.
 *
 * <p>La estructura conserva la edición XP: menú, toolbar, franja de ejecución,
 * casos a la izquierda, workspace central, inspector a la derecha, timeline y
 * barra de estado. La modernización ocurre en CSS, no reinventando el flujo.</p>
 */
final class MainWindowLayout {

    private MainWindowLayout() {
    }

    /**
     * Construye el nodo raíz y deja configurados controles puramente visuales.
     *
     * @param ui controles compartidos de la ventana
     * @param actions callbacks de interacción
     * @return raíz JavaFX completa
     */
    static Parent build(MainWindowControls ui, Actions actions) {
        configureTables(ui);
        configureSelectors(ui);

        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-root");

        VBox top = new VBox(
                buildMenuBar(actions),
                buildToolBar(ui, actions),
                buildControlStrip(ui));

        root.setTop(top);
        root.setCenter(WorkspaceDockLayout.build(ui));
        root.setBottom(buildStatusBar(ui));
        return root;
    }

    private static MenuBar buildMenuBar(Actions actions) {
        Menu file = new Menu("Archivo");
        file.getItems().add(item("Nuevo caso...", "Ctrl+N", actions.createCase()));
        file.getItems().add(item("Abrir caso...", "Ctrl+O", actions.openCase()));
        file.getItems().add(item("Guardar caso como...", "Ctrl+Shift+S", actions.saveCase()));
        file.getItems().add(new SeparatorMenuItem());
        file.getItems().add(item("Salir", null, actions.exit()));

        Menu execution = new Menu("Ejecución");
        execution.getItems().add(item("Ejecutar / reanudar", "F5", actions.run()));
        execution.getItems().add(item("Pausar", "F6", actions.pause()));
        execution.getItems().add(item("Avanzar un paso lógico", "F7", actions.step()));
        execution.getItems().add(item("Reiniciar", "Ctrl+R", actions.reset()));

        Menu help = new Menu("Ayuda");
        help.getItems().add(item("Contenido de ayuda", "F1", actions.help()));
        help.getItems().add(item("Acerca de ReconcileLab", null, actions.about()));

        return new MenuBar(file, execution, help);
    }

    private static MenuItem item(String text, String accelerator, Runnable action) {
        MenuItem item = new MenuItem(text);
        if (accelerator != null) {
            item.setAccelerator(KeyCombination.keyCombination(accelerator));
        }
        item.setOnAction(event -> action.run());
        return item;
    }

    private static ToolBar buildToolBar(
            MainWindowControls ui,
            Actions actions) {

        Button create =
                commandButton(
                        "Nuevo",
                        UiIconFactory.newCase());

        Button open =
                commandButton(
                        "Abrir",
                        UiIconFactory.openCase());

        Button export =
                commandButton(
                        "Exportar imagen",
                        UiIconFactory.exportImage());

        configureCommandButton(ui.runButton);
        configureCommandButton(ui.pauseButton);
        configureCommandButton(ui.stepButton);
        configureCommandButton(ui.resetButton);

        ui.runButton.setGraphic(
                UiIconFactory.run());

        ui.pauseButton.setGraphic(
                UiIconFactory.pause());

        ui.stepButton.setGraphic(
                UiIconFactory.step());

        ui.resetButton.setGraphic(
                UiIconFactory.reset());

        create.setTooltip(
                new Tooltip(
                        "Crear un caso de conciliación."));

        open.setTooltip(
                new Tooltip(
                        "Abrir un archivo .case."));

        export.setTooltip(
                new Tooltip(
                        "Exportar una lámina PNG legible de la ejecución retenida."));

        ui.runButton.setTooltip(
                new Tooltip(
                        "Ejecutar o reanudar la búsqueda."));

        ui.pauseButton.setTooltip(
                new Tooltip(
                        "Pausar la ejecución."));

        ui.stepButton.setTooltip(
                new Tooltip(
                        "Avanzar un estado lógico."));

        ui.resetButton.setTooltip(
                new Tooltip(
                        "Reiniciar el caso actual."));

        ui.runButton.getStyleClass()
                .add("action-primary");

        export.getStyleClass()
                .add("action-accent");

        create.setOnAction(event ->
                actions.createCase().run());

        open.setOnAction(event ->
                actions.openCase().run());

        export.setOnAction(event ->
                actions.exportImage().run());

        ui.runButton.setOnAction(event ->
                actions.run().run());

        ui.pauseButton.setOnAction(event ->
                actions.pause().run());

        ui.stepButton.setOnAction(event ->
                actions.step().run());

        ui.resetButton.setOnAction(event ->
                actions.reset().run());

        ToolBar toolbar =
                new ToolBar(
                        create,
                        open,
                        new Separator(),
                        ui.runButton,
                        ui.pauseButton,
                        ui.stepButton,
                        ui.resetButton,
                        new Separator(),
                        export);

        toolbar.getStyleClass()
                .add("command-toolbar");

        return toolbar;
    }

    private static Button commandButton(
            String text,
            javafx.scene.Node graphic) {

        Button button =
                new Button(
                        text,
                        graphic);

        configureCommandButton(button);
        return button;
    }

    private static void configureCommandButton(Button button) {
        button.getStyleClass()
                .add("toolbar-command");

        button.setGraphicTextGap(7);
    }

    private static HBox buildControlStrip(MainWindowControls ui) {
        HBox box =
                new HBox(
                        8,
                        stripLabel("Método de búsqueda:"),
                        ui.strategy,
                        new Separator(),
                        stripLabel("Ejecución:"),
                        ui.mode,
                        new Separator(),
                        stripLabel("Actualización visual:"),
                        ui.refreshMs,
                        stripLabel("ms"),
                        new Separator(),
                        stripLabel("Estados de búsqueda compatibles:"),
                        ui.compatibleStates);

        box.setAlignment(
                Pos.CENTER_LEFT);

        box.setPadding(
                new Insets(
                        8,
                        10,
                        8,
                        10));

        box.getStyleClass()
                .add("control-strip");

        HBox.setHgrow(
                ui.compatibleStates,
                Priority.NEVER);

        return box;
    }

    private static Label stripLabel(String text) {
        Label label =
                new Label(text);

        label.getStyleClass()
                .add("control-strip-label");

        return label;
    }

    private static HBox buildStatusBar(MainWindowControls ui) {
        Region spacer1 = new Region();
        Region spacer2 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        HBox bar = new HBox(
                10,
                ui.statusLeft,
                spacer1,
                ui.statusTick,
                ui.statusMatches,
                spacer2,
                ui.statusRefresh);

        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(4, 9, 4, 9));
        bar.getStyleClass().add("status-strip");
        return bar;
    }

    private static void configureSelectors(MainWindowControls ui) {
        ui.strategy.setItems(FXCollections.observableArrayList(SearchStrategy.values()));
        ui.strategy.setValue(SearchStrategy.PRUNING);
        ui.mode.setItems(FXCollections.observableArrayList(ExecutionMode.values()));
        ui.mode.setValue(ExecutionMode.BALANCED);
        ui.refreshMs.setEditable(false);
        ui.compatibleStates.setPrefWidth(240);
    }

    private static void configureTables(MainWindowControls ui) {
        ui.metrics.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        ui.metrics.setPrefHeight(290);

        TableColumn<MetricRow, String> measure = new TableColumn<>("Medida");
        measure.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().measure()));
        TableColumn<MetricRow, String> value = new TableColumn<>("Valor");
        value.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().value()));

        ui.metrics.getColumns().add(measure);
        ui.metrics.getColumns().add(value);
        ui.metrics.setItems(ui.metricItems);

        ui.timeline.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        TableColumn<EventRow, String> tick = new TableColumn<>("Tick");
        tick.setPrefWidth(80);
        tick.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().tick()));
        TableColumn<EventRow, String> event = new TableColumn<>("Evento");
        event.setPrefWidth(130);
        event.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().event()));
        TableColumn<EventRow, String> details = new TableColumn<>("Detalles");
        details.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().details()));

        ui.timeline.getColumns().add(tick);
        ui.timeline.getColumns().add(event);
        ui.timeline.getColumns().add(details);
        ui.timeline.setItems(ui.timelineItems);
        ui.solutions.setItems(ui.solutionItems);
    }

    /** Callbacks de la vista; el layout no conoce runtime ni archivos. */
    record Actions(
            Runnable createCase,
            Runnable openCase,
            Runnable saveCase,
            Runnable exportImage,
            Runnable run,
            Runnable pause,
            Runnable step,
            Runnable reset,
            Runnable help,
            Runnable about,
            Runnable exit) {
    }
}
