package com.marcosmoreiradev.reconcilelab.ui.workspace;

import com.marcosmoreiradev.reconcilelab.domain.ExecutionNode;
import com.marcosmoreiradev.reconcilelab.domain.ExecutionSnapshot;

import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ScrollBar;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Workspace matemático con canvas físico y mundo virtual.
 *
 * <p>El zoom vive como HUD fijo sobre el viewport. Pan, scroll y focus operan
 * sobre coordenadas lógicas y permiten overscroll para que el overlay nunca
 * bloquee el acceso visual al borde derecho del grafo.</p>
 */
public final class SearchWorkspace extends GridPane {

    private final StackPane viewport = new StackPane();
    private final Canvas canvas = new Canvas();
    private final ScrollBar horizontal = new ScrollBar();
    private final ScrollBar vertical = new ScrollBar();
    private final ZoomOverlay zoom = new ZoomOverlay();
    private final NodeLayoutModel layout = new NodeLayoutModel();
    private final SearchCanvasPainter painter = new SearchCanvasPainter();

    private ExecutionSnapshot snapshot;
    private long selectedNodeId;
    private Consumer<ExecutionNode> selectionListener = node -> { };

    private double panStartX;
    private double panStartY;
    private double panStartH;
    private double panStartV;
    private boolean panning;

    /** Construye viewport, scrollbars y zoom flotante. */
    public SearchWorkspace() {
        getStyleClass().add("search-workspace");
        viewport.getStyleClass().add("search-viewport");
        viewport.getChildren().addAll(
                canvas,
                zoom);

        StackPane.setAlignment(zoom, Pos.CENTER_RIGHT);
        StackPane.setMargin(zoom, new Insets(0, 14, 0, 0));

        horizontal.setOrientation(Orientation.HORIZONTAL);
        vertical.setOrientation(Orientation.VERTICAL);

        configureGrid();
        add(viewport, 0, 0);
        add(vertical, 1, 0);
        add(horizontal, 0, 1);

        GridPane.setHgrow(viewport, Priority.ALWAYS);
        GridPane.setVgrow(viewport, Priority.ALWAYS);

        canvas.widthProperty().bind(viewport.widthProperty());
        canvas.heightProperty().bind(viewport.heightProperty());

        installResizeListeners();
        installScrollListeners();
        installMouseNavigation();
    }

    /**
     * Registra el observador de selección.
     *
     * @param listener consumidor del nodo; recibe {@code null} al seleccionar vacío
     */
    public void setSelectionListener(Consumer<ExecutionNode> listener) {
        selectionListener = listener == null ? node -> { } : listener;
    }

    /**
     * Publica un snapshot para layout y dibujo.
     *
     * @param snapshot snapshot actual
     */
    public void setSnapshot(ExecutionSnapshot snapshot) {
        this.snapshot = snapshot;
        layout.update(snapshot);

        if (selectedNodeId != 0
                && snapshot.nodes().stream().noneMatch(node -> node.id() == selectedNodeId)) {
            selectedNodeId = 0;
        }

        updateScrollRanges();
        redraw();
    }

    /** @return porcentaje de zoom visible */
    public double getZoomPercent() {
        return zoom.getValue();
    }

    /**
     * Centra y selecciona un nodo retenido.
     *
     * @param nodeId identificador lógico
     */
    public void focusNode(long nodeId) {
        focusNode(nodeId, true);
    }

    /**
     * Centra un nodo y opcionalmente notifica su selección.
     *
     * @param nodeId identificador lógico
     * @param notifySelection si debe publicarse al inspector
     */
    public void focusNode(long nodeId, boolean notifySelection) {
        if (snapshot == null) {
            return;
        }

        NodeLayoutModel.NodeBox box = layout.box(nodeId);
        if (box == null) {
            return;
        }

        ExecutionNode node = snapshot.nodes().stream()
                .filter(candidate -> candidate.id() == nodeId)
                .findFirst()
                .orElse(null);

        if (node == null) {
            return;
        }

        boolean changed = selectedNodeId != nodeId;
        selectedNodeId = nodeId;

        double scale = zoom.getValue() / 100.0;
        double visibleWorldWidth = ViewportNavigationPolicy.focusWorldWidth(canvas.getWidth(), scale);
        double visibleWorldHeight = canvas.getHeight() / scale;

        horizontal.setValue(clamp(
                box.centerX() - visibleWorldWidth / 2.0,
                horizontal.getMin(),
                horizontal.getMax()));

        vertical.setValue(clamp(
                box.centerY() - visibleWorldHeight / 2.0,
                vertical.getMin(),
                vertical.getMax()));

        redraw();

        if (notifySelection && changed) {
            selectionListener.accept(node);
        }
    }

    private void configureGrid() {
        ColumnConstraints world = new ColumnConstraints();
        world.setHgrow(Priority.ALWAYS);
        getColumnConstraints().add(world);
        getColumnConstraints().add(fixedColumn(18));

        RowConstraints worldRow = new RowConstraints();
        worldRow.setVgrow(Priority.ALWAYS);
        getRowConstraints().add(worldRow);
        getRowConstraints().add(fixedRow(18));
    }

    private ColumnConstraints fixedColumn(double width) {
        ColumnConstraints constraints = new ColumnConstraints();
        constraints.setMinWidth(width);
        constraints.setPrefWidth(width);
        constraints.setMaxWidth(width);
        return constraints;
    }

    private RowConstraints fixedRow(double height) {
        RowConstraints constraints = new RowConstraints();
        constraints.setMinHeight(height);
        constraints.setPrefHeight(height);
        constraints.setMaxHeight(height);
        return constraints;
    }

    private void installResizeListeners() {
        canvas.widthProperty().addListener((observable, oldValue, newValue) -> {
            updateScrollRanges();
            redraw();
        });

        canvas.heightProperty().addListener((observable, oldValue, newValue) -> {
            updateScrollRanges();
            redraw();
        });
    }

    private void installScrollListeners() {
        ChangeListener<Number> repaint = (observable, oldValue, newValue) -> redraw();

        horizontal.valueProperty().addListener(repaint);
        vertical.valueProperty().addListener(repaint);

        zoom.valueProperty().addListener((observable, oldValue, newValue) -> {
            updateScrollRanges();
            redraw();
        });
    }

    private void installMouseNavigation() {
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, this::handleMousePressed);
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::handleMouseDragged);
        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, this::handleMouseReleased);

        canvas.setOnScroll(event -> {
            double scale = zoom.getValue() / 100.0;

            if (event.isControlDown()) {
                zoom.setValue(zoom.getValue() + Math.signum(event.getDeltaY()) * 10);
            } else if (event.isShiftDown()) {
                horizontal.setValue(clamp(
                        horizontal.getValue() - event.getDeltaY() / scale,
                        horizontal.getMin(),
                        horizontal.getMax()));
            } else {
                vertical.setValue(clamp(
                        vertical.getValue() - event.getDeltaY() / scale,
                        vertical.getMin(),
                        vertical.getMax()));
            }

            event.consume();
        });
    }

    private void handleMousePressed(MouseEvent event) {
        if (event.getButton() == MouseButton.MIDDLE
                || event.getButton() == MouseButton.SECONDARY) {
            panning = true;
            panStartX = event.getScreenX();
            panStartY = event.getScreenY();
            panStartH = horizontal.getValue();
            panStartV = vertical.getValue();
            canvas.setCursor(Cursor.CLOSED_HAND);
            event.consume();
            return;
        }

        if (event.getButton() == MouseButton.PRIMARY) {
            selectAt(event.getX(), event.getY());
        }
    }

    private void handleMouseDragged(MouseEvent event) {
        if (!panning) {
            return;
        }

        double scale = zoom.getValue() / 100.0;

        horizontal.setValue(clamp(
                panStartH - (event.getScreenX() - panStartX) / scale,
                horizontal.getMin(),
                horizontal.getMax()));

        vertical.setValue(clamp(
                panStartV - (event.getScreenY() - panStartY) / scale,
                vertical.getMin(),
                vertical.getMax()));

        event.consume();
    }

    private void handleMouseReleased(MouseEvent event) {
        if (!panning) {
            return;
        }

        panning = false;
        canvas.setCursor(Cursor.DEFAULT);
        event.consume();
    }

    private void selectAt(double screenX, double screenY) {
        if (snapshot == null) {
            return;
        }

        double scale = zoom.getValue() / 100.0;
        double worldX = horizontal.getValue() + screenX / scale;
        double worldY = vertical.getValue() + screenY / scale;
        long found = findNodeAt(worldX, worldY);

        selectedNodeId = found;
        redraw();

        if (found == 0) {
            selectionListener.accept(null);
            return;
        }

        long selectedId = found;
        snapshot.nodes().stream()
                .filter(node -> node.id() == selectedId)
                .findFirst()
                .ifPresent(selectionListener);
    }

    private long findNodeAt(double worldX, double worldY) {
        for (Map.Entry<Long, NodeLayoutModel.NodeBox> entry : layout.boxes().entrySet()) {
            if (entry.getValue().contains(worldX, worldY)) {
                return entry.getKey();
            }
        }

        return 0;
    }

    private void updateScrollRanges() {
        double scale = zoom.getValue() / 100.0;
        double visibleWidth = canvas.getWidth() / scale;
        double visibleHeight = canvas.getHeight() / scale;
        double extra = ViewportNavigationPolicy.overscrollWorld(scale);

        horizontal.setMin(-extra);
        horizontal.setMax(Math.max(
                -extra,
                layout.virtualWidth() - visibleWidth + extra));
        horizontal.setVisibleAmount(Math.max(1, visibleWidth));

        vertical.setMin(-extra);
        vertical.setMax(Math.max(
                -extra,
                layout.virtualHeight() - visibleHeight + extra));
        vertical.setVisibleAmount(Math.max(1, visibleHeight));

        horizontal.setValue(clamp(
                horizontal.getValue(),
                horizontal.getMin(),
                horizontal.getMax()));

        vertical.setValue(clamp(
                vertical.getValue(),
                vertical.getMin(),
                vertical.getMax()));
    }

    private void redraw() {
        painter.paint(
                canvas.getGraphicsContext2D(),
                snapshot,
                layout,
                selectedNodeId,
                horizontal.getValue(),
                vertical.getValue(),
                zoom.getValue() / 100.0,
                canvas.getWidth(),
                canvas.getHeight());
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
