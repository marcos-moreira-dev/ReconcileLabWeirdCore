package com.marcosmoreiradev.reconcilelab.ui;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Panel inferior de línea de tiempo con cabecera propia.
 *
 * <p>No utiliza el estado interno de {@code TitledPane}; contenido y geometría
 * se controlan explícitamente para que contraer y volver a expandir siempre
 * restaure una zona útil dentro del SplitPane.</p>
 */
final class TimelineDockPane extends VBox {

    static final double HEADER_HEIGHT = 36;

    private final BooleanProperty expanded =
            new SimpleBooleanProperty(true);

    private final Label arrow =
            new Label("▼");

    private final Node content;

    TimelineDockPane(Node content) {
        super(0);

        this.content = content;

        getStyleClass()
                .add("timeline-dock");

        HBox header =
                buildHeader();

        getChildren()
                .addAll(
                        header,
                        content);

        VBox.setVgrow(
                content,
                Priority.ALWAYS);

        setMinHeight(
                HEADER_HEIGHT);

        expanded.addListener(
                (observable, oldValue, newValue) ->
                        applyExpandedState());

        applyExpandedState();
    }

    BooleanProperty expandedProperty() {
        return expanded;
    }

    boolean isExpanded() {
        return expanded.get();
    }

    void setExpanded(boolean value) {
        expanded.set(value);
    }

    private HBox buildHeader() {
        arrow.getStyleClass()
                .add("timeline-dock-arrow");

        Label title =
                new Label(
                        "Línea de tiempo de ejecución");

        title.getStyleClass()
                .add("timeline-dock-title");

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS);

        HBox header =
                new HBox(
                        7,
                        arrow,
                        title,
                        spacer);

        header.setAlignment(
                Pos.CENTER_LEFT);

        header.setMinHeight(
                HEADER_HEIGHT);

        header.setPrefHeight(
                HEADER_HEIGHT);

        header.setFocusTraversable(true);

        Tooltip.install(
                header,
                new Tooltip(
                        "Mostrar u ocultar la línea de tiempo."));

        header.setOnMouseClicked(event ->
                toggle());

        header.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER
                    || event.getCode() == KeyCode.SPACE) {

                toggle();
                event.consume();
            }
        });

        header.getStyleClass()
                .add("timeline-dock-header");

        return header;
    }

    private void toggle() {
        setExpanded(
                !isExpanded());
    }

    private void applyExpandedState() {
        boolean show =
                isExpanded();

        content.setManaged(show);
        content.setVisible(show);

        arrow.setText(
                show
                        ? "▼"
                        : "▶");
    }
}
