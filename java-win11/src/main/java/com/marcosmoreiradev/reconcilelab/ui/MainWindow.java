package com.marcosmoreiradev.reconcilelab.ui;

import com.marcosmoreiradev.reconcilelab.app.AppMetadata;
import com.marcosmoreiradev.reconcilelab.app.BuildInfo;
import com.marcosmoreiradev.reconcilelab.domain.ExecutionNode;
import com.marcosmoreiradev.reconcilelab.domain.ExecutionSnapshot;
import com.marcosmoreiradev.reconcilelab.domain.ProblemInstance;
import com.marcosmoreiradev.reconcilelab.io.CaseFile;
import com.marcosmoreiradev.reconcilelab.io.MoneyText;
import com.marcosmoreiradev.reconcilelab.io.ResourceCaseCatalog;
import com.marcosmoreiradev.reconcilelab.runtime.ExecutionState;
import com.marcosmoreiradev.reconcilelab.runtime.RuntimeController;
import com.marcosmoreiradev.reconcilelab.ui.dialog.NewCaseDialog;
import com.marcosmoreiradev.reconcilelab.ui.help.HelpWindow;

import javafx.animation.AnimationTimer;
import javafx.collections.FXCollections;
import javafx.css.PseudoClass;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;

/**
 * Controlador de la ventana principal de la edición Java/Windows 11.
 *
 * <p>La clase coordina eventos y ciclo de vida. La composición vive en
 * {@link MainWindowLayout}; la conversión de snapshots a texto/tablas vive en
 * {@link SnapshotPresenter}; el dibujo del grafo vive en el paquete
 * {@code ui.workspace}. Esta separación es también un gate de mantenibilidad:
 * ningún archivo Java de producción debe superar 500 líneas.</p>
 */
public final class MainWindow {

    private static final PseudoClass ZEBRA = PseudoClass.getPseudoClass("zebra");

    private final Stage stage;
    private final RuntimeController runtime;
    private final MainWindowControls ui = new MainWindowControls();
    private final SnapshotPresenter presenter = new SnapshotPresenter(ui);
    private final WindowStateManager windowState = new WindowStateManager();
    private final AnimationTimer uiTimer;

    private ProblemInstance currentProblem;
    private ExecutionNode selectedNode;
    private long lastRefreshNanos;

    /**
     * Crea la ventana y conecta todos los adaptadores con el runtime.
     *
     * @param stage Stage principal de JavaFX
     * @param runtime controlador de ejecución
     */
    public MainWindow(Stage stage, RuntimeController runtime) {
        this.stage = Objects.requireNonNull(stage, "stage");
        this.runtime = Objects.requireNonNull(runtime, "runtime");

        configureWorkspace();
        configureSelectors();
        configureExamples();

        MainWindowLayout.Actions actions = new MainWindowLayout.Actions(
                this::createCase,
                this::openCase,
                this::saveCase,
                this::exportStudyImage,
                this::runExecution,
                this::pauseExecution,
                this::stepExecution,
                this::resetExecution,
                () -> HelpWindow.show(stage),
                this::showAbout,
                stage::close);

        Scene scene = new Scene(MainWindowLayout.build(ui, actions), 1_520, 900);
        var css = MainWindow.class.getResource("/css/reconcilelab.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }

        stage.setTitle(AppMetadata.DISPLAY_NAME);
        stage.setScene(scene);
        windowState.prepare(stage);
        installIcon();

        uiTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                refreshOnCadence(now);
            }
        };
        uiTimer.start();

        stage.setOnCloseRequest(event -> {
            windowState.save(stage);
            uiTimer.stop();
            runtime.close();
        });

        loadFirstPreset();
    }

    /** Muestra la ventana; la primera ejecución se maximiza por defecto. */
    public void show() {
        windowState.show(stage);
    }

    /**
     * Abre un caso entregado por Windows, por ejemplo mediante asociación `.case`.
     *
     * @param path archivo a cargar
     */
    public void loadExternalCase(Path path) {
        Objects.requireNonNull(path, "path");

        try {
            applyProblem(CaseFile.load(path));
            ui.examples.getSelectionModel().clearSelection();
            ui.statusLeft.setText("Caso abierto: " + path.getFileName());
        } catch (Exception ex) {
            showError("No se pudo abrir el caso.", ex);
        }
    }

    private void installIcon() {
        try (InputStream icon = MainWindow.class.getResourceAsStream(
                "/branding/reconcilelab-icon-64.png")) {
            if (icon != null) {
                stage.getIcons().add(new Image(icon));
            }
        } catch (IOException ignored) {
            // El icono no impide operar la aplicación.
        }
    }

    private void configureWorkspace() {
        ui.workspace.setSelectionListener(node -> {
            selectedNode = node;
            if (currentProblem != null) {
                presenter.present(runtime.getSnapshot(), currentProblem, selectedNode);
            }
        });
    }

    private void configureSelectors() {
        ui.strategy.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue == null || currentProblem == null) {
                        return;
                    }
                    runtime.setStrategy(newValue);
                    resetExecution();
                });

        ui.mode.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        runtime.setMode(newValue);
                    }
                });

        ui.refreshMs.valueProperty().addListener(
                (observable, oldValue, newValue) -> ui.statusRefresh.setText(
                        "Actualización: " + newValue + " ms"));

        ui.compatibleStates.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue == null || newValue.nodeId() <= 0) {
                        return;
                    }

                    ui.workspace.focusNode(
                            newValue.nodeId(),
                            false);

                    selectedNode = runtime.getSnapshot()
                            .nodes()
                            .stream()
                            .filter(node ->
                                    node.id() == newValue.nodeId())
                            .findFirst()
                            .orElse(selectedNode);

                    presenter.present(
                            runtime.getSnapshot(),
                            currentProblem,
                            selectedNode);
                });
    }

    private void configureExamples() {
        ui.examples.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(ResourceCaseCatalog.PresetCase item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    pseudoClassStateChanged(ZEBRA, false);
                    return;
                }
                setText(item.problem().name());
                pseudoClassStateChanged(ZEBRA, getIndex() % 2 == 1);
            }
        });

        ui.examples.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        applyProblem(newValue.problem());
                    }
                });

        try {
            ui.examples.setItems(FXCollections.observableArrayList(
                    new ResourceCaseCatalog().loadAll()));
        } catch (IOException ex) {
            showError("No se pudieron cargar los casos de ejemplo.", ex);
        }
    }

    private void loadFirstPreset() {
        if (!ui.examples.getItems().isEmpty()) {
            ui.examples.getSelectionModel().selectFirst();
        }
    }

    private void applyProblem(ProblemInstance problem) {
        currentProblem = problem;
        selectedNode = null;
        presenter.resetRate();

        runtime.loadProblem(problem);
        runtime.setStrategy(ui.strategy.getValue());
        runtime.setMode(ui.mode.getValue());

        ui.currentCaseTitle.setText(problem.name());
        ui.currentCaseText.setText(
                "Monto a conciliar: " + MoneyText.formatCents(problem.targetCents())
                        + "\nMovimientos candidatos: " + problem.itemCount()
                        + "\n\n" + problem.description());

        ui.compatibleStates.getItems().setAll(
                new CompatibleStateOption(0, "Sin resultados compatibles"));
        ui.compatibleStates.getSelectionModel().selectFirst();
        ui.compatibleStates.setDisable(true);
        refreshFromRuntime();
    }

    private void refreshOnCadence(long now) {
        long required = ui.refreshMs.getValue() * 1_000_000L;
        if (now - lastRefreshNanos < required) {
            return;
        }
        lastRefreshNanos = now;
        refreshFromRuntime();
    }

    private void refreshFromRuntime() {
        if (currentProblem == null) {
            return;
        }

        ExecutionSnapshot snapshot = runtime.getSnapshot();
        ExecutionState state = runtime.getState();

        ui.workspace.setSnapshot(snapshot);
        presenter.present(snapshot, currentProblem, selectedNode);

        ui.statusLeft.setText(stateLabel(state));
        ui.statusTick.setText("Tick lógico: " + snapshot.logicalTick());
        ui.statusMatches.setText("Compatibles: " + snapshot.matches());

        ui.runButton.setDisable(state == ExecutionState.RUNNING || state == ExecutionState.COMPLETED);
        ui.pauseButton.setDisable(state != ExecutionState.RUNNING);
        ui.stepButton.setDisable(state == ExecutionState.RUNNING || state == ExecutionState.COMPLETED);
        ui.resetButton.setDisable(state == ExecutionState.READY);
    }

    private String stateLabel(ExecutionState state) {
        return switch (state) {
            case READY -> "Preparado";
            case RUNNING -> "Ejecutando";
            case PAUSED -> "Pausado";
            case COMPLETED -> "Completado";
        };
    }

    private void runExecution() {
        runtime.start();
    }

    private void pauseExecution() {
        runtime.pause();
        refreshFromRuntime();
    }

    private void stepExecution() {
        runtime.stepOnce();
        refreshFromRuntime();
    }

    private void resetExecution() {
        runtime.reset();
        selectedNode = null;
        presenter.resetRate();
        refreshFromRuntime();
    }

    private void createCase() {
        NewCaseDialog dialog = new NewCaseDialog(stage);
        dialog.showAndWait().ifPresent(problem -> {
            applyProblem(problem);
            ui.examples.getSelectionModel().clearSelection();
            ui.statusLeft.setText("Caso nuevo preparado");
        });
    }

    private void openCase() {
        FileChooser chooser = caseChooser("Abrir caso de conciliación");
        File file = chooser.showOpenDialog(stage);
        if (file != null) {
            loadExternalCase(file.toPath());
        }
    }

    private void saveCase() {
        if (currentProblem == null) {
            return;
        }

        FileChooser chooser = caseChooser("Guardar caso como");
        chooser.setInitialFileName("reconcilelab.case");
        File file = chooser.showSaveDialog(stage);
        if (file == null) {
            return;
        }

        try {
            File target = ensureExtension(file, ".case");
            CaseFile.save(target.toPath(), currentProblem);
            ui.statusLeft.setText("Caso guardado");
        } catch (Exception ex) {
            showError("No se pudo guardar el caso.", ex);
        }
    }

    private FileChooser caseChooser(String title) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Casos ReconcileLab (*.case)", "*.case"));
        return chooser;
    }

    private void exportStudyImage() {
        if (currentProblem == null) {
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Exportar imagen de estudio");
        chooser.setInitialFileName("reconcilelab-estudio.png");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imagen PNG (*.png)", "*.png"));

        File file = chooser.showSaveDialog(stage);
        if (file == null) {
            return;
        }

        try {
            File target = ensureExtension(file, ".png");
            StudyImageExporter.export(
                    target.toPath(),
                    currentProblem,
                    ui.strategy.getValue(),
                    runtime.getSnapshot());
            ui.statusLeft.setText("Imagen de estudio exportada");
        } catch (Exception ex) {
            showError("No se pudo exportar la imagen.", ex);
        }
    }

    private File ensureExtension(File file, String extension) {
        String name = file.getName().toLowerCase(Locale.ROOT);
        if (name.endsWith(extension.toLowerCase(Locale.ROOT))) {
            return file;
        }
        return new File(file.getParentFile(), file.getName() + extension);
    }

    private void showAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(stage);
        alert.setTitle("Acerca de ReconcileLab");
        alert.setHeaderText(AppMetadata.DISPLAY_NAME);
        alert.setContentText(
                "Versión Java: " + BuildInfo.version()
                        + "\nEditor: " + AppMetadata.VENDOR
                        + "\nPaquete: " + AppMetadata.PACKAGE_BASE
                        + "\n\nReconcileLab ayuda a examinar un monto total y determinar qué "
                        + "movimientos candidatos pueden explicarlo. Esta edición para Windows 11 "
                        + "conserva la semántica del laboratorio C++/Windows XP con Java 21 y JavaFX.");
        alert.showAndWait();
    }

    private void showError(String message, Throwable error) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(stage);
        alert.setTitle("ReconcileLab");
        alert.setHeaderText(message);
        alert.setContentText(error.getMessage() == null
                ? error.getClass().getSimpleName()
                : error.getMessage());
        alert.showAndWait();
    }
}
