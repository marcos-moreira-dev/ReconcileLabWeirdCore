package com.marcosmoreiradev.reconcilelab.ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Maquetación de los paneles operativos de la ventana principal.
 *
 * <p>El cuerpo y la línea de tiempo viven en un SplitPane vertical. El
 * timeline usa una cabecera propia para que expandir/contraer no dependa de
 * tamaños internos de {@link TitledPane}.</p>
 */
final class WorkspaceDockLayout {

    private WorkspaceDockLayout() {
    }

    static BorderPane build(MainWindowControls ui) {
        HBox body =
                buildBody(ui);

        TimelineDockPane timeline =
                new TimelineDockPane(
                        ui.timeline);

        SplitPane split =
                new SplitPane(
                        body,
                        timeline);

        split.setOrientation(
                Orientation.VERTICAL);

        split.setMinHeight(0);

        split.getStyleClass()
                .add("workspace-vertical-split");

        installTimelineBehavior(
                split,
                timeline);

        BorderPane wrapper =
                new BorderPane(
                        split);

        wrapper.setMinHeight(0);

        wrapper.setPadding(
                new Insets(
                        0,
                        0,
                        6,
                        0));

        return wrapper;
    }

    private static HBox buildBody(MainWindowControls ui) {
        VBox left =
                buildLeftPane(ui);

        ScrollPane right =
                buildRightPane(ui);

        HBox body =
                new HBox(
                        8,
                        left,
                        ui.workspace,
                        right);

        body.setPadding(
                new Insets(
                        0,
                        8,
                        0,
                        8));

        body.setMinHeight(220);

        left.setMinHeight(0);
        right.setMinHeight(0);

        ui.workspace.setMinWidth(420);
        ui.workspace.setMinHeight(0);

        HBox.setHgrow(
                ui.workspace,
                Priority.ALWAYS);

        return body;
    }

    private static VBox buildLeftPane(MainWindowControls ui) {
        ui.examples.setPrefHeight(320);
        ui.examples.setMinHeight(120);

        TitledPane examplesPane =
                titledPane(
                        "Casos de ejemplo",
                        ui.examples,
                        true);

        ui.currentCaseTitle.getStyleClass()
                .add("case-title");

        VBox currentContent =
                new VBox(
                        8,
                        ui.currentCaseTitle,
                        ui.currentCaseText);

        currentContent.setPadding(
                new Insets(8));

        currentContent.setMinHeight(0);
        ui.currentCaseText.setMinHeight(80);

        VBox.setVgrow(
                ui.currentCaseText,
                Priority.ALWAYS);

        TitledPane currentPane =
                titledPane(
                        "Caso actual",
                        currentContent,
                        true);

        VBox left =
                new VBox(
                        8,
                        examplesPane,
                        currentPane);

        left.setPadding(
                new Insets(8));

        left.setPrefWidth(265);
        left.setMinWidth(230);

        left.getStyleClass()
                .add("side-pane");

        VBox.setVgrow(
                examplesPane,
                Priority.ALWAYS);

        VBox.setVgrow(
                currentPane,
                Priority.SOMETIMES);

        return left;
    }

    private static ScrollPane buildRightPane(MainWindowControls ui) {
        VBox summaryContent =
                new VBox(
                        7,
                        ui.metrics,
                        ui.executionProgress);

        summaryContent.setPadding(
                new Insets(8));

        TitledPane summary =
                titledPane(
                        "Resumen de ejecución",
                        summaryContent,
                        true);

        TitledPane route =
                titledPane(
                        "Ruta seleccionada",
                        ui.selectedRoute,
                        true);

        TitledPane compatible =
                titledPane(
                        "Combinaciones compatibles",
                        ui.solutions,
                        true);

        VBox content =
                new VBox(
                        9,
                        summary,
                        route,
                        compatible);

        content.setPadding(
                new Insets(
                        8,
                        14,
                        8,
                        8));

        content.setFillWidth(true);
        content.setMinHeight(0);

        ScrollPane scroll =
                new ScrollPane(
                        content);

        scroll.setMinHeight(0);
        scroll.setFitToWidth(true);

        scroll.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER);

        scroll.setPrefWidth(360);
        scroll.setMinWidth(325);

        scroll.getStyleClass()
                .add("inspector-scroll");

        return scroll;
    }

    private static void installTimelineBehavior(
            SplitPane split,
            TimelineDockPane timeline) {

        double[] expandedDivider = {
                TimelineDockModel.DEFAULT_EXPANDED_DIVIDER
        };

        boolean[] applying = {
                false
        };

        split.getDividers()
                .getFirst()
                .positionProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (applying[0]
                            || !timeline.isExpanded()) {

                        return;
                    }

                    expandedDivider[0] =
                            TimelineDockModel.normalizeExpandedDivider(
                                    newValue.doubleValue());
                });

        timeline.expandedProperty()
                .addListener((observable, oldValue, expanded) -> {
                    if (!expanded) {
                        expandedDivider[0] =
                                TimelineDockModel.normalizeExpandedDivider(
                                        split.getDividers()
                                                .getFirst()
                                                .getPosition());
                    }

                    Platform.runLater(() -> {
                        applying[0] = true;

                        split.setDividerPosition(
                                0,
                                expanded
                                        ? expandedDivider[0]
                                        : TimelineDockModel.collapsedDivider(
                                                split.getHeight(),
                                                TimelineDockPane.HEADER_HEIGHT));

                        split.requestLayout();
                        applying[0] = false;
                    });
                });

        split.heightProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (timeline.isExpanded()) {
                        return;
                    }

                    Platform.runLater(() -> {
                        applying[0] = true;

                        split.setDividerPosition(
                                0,
                                TimelineDockModel.collapsedDivider(
                                        newValue.doubleValue(),
                                        TimelineDockPane.HEADER_HEIGHT));

                        applying[0] = false;
                    });
                });

        Platform.runLater(() -> {
            timeline.setExpanded(true);

            split.setDividerPosition(
                    0,
                    TimelineDockModel.DEFAULT_EXPANDED_DIVIDER);
        });
    }

    private static TitledPane titledPane(
            String title,
            javafx.scene.Node content,
            boolean expanded) {

        TitledPane pane =
                new TitledPane(
                        title,
                        content);

        pane.setExpanded(expanded);
        pane.setCollapsible(true);

        return pane;
    }
}
