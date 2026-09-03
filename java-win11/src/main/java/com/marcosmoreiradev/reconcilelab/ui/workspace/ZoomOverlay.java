package com.marcosmoreiradev.reconcilelab.ui.workspace;

import com.marcosmoreiradev.reconcilelab.ui.component.UiIconFactory;

import javafx.beans.property.DoubleProperty;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;

/**
 * Control flotante de zoom del workspace.
 *
 * <p>Es un HUD del viewport, no una columna del layout ni parte del mundo
 * desplazable. Por tanto permanece fijo mientras el usuario panea el grafo.</p>
 */
final class ZoomOverlay extends VBox {

    static final double MIN_ZOOM = 25;
    static final double MAX_ZOOM = 200;

    private final Slider slider =
            new Slider(
                    MIN_ZOOM,
                    MAX_ZOOM,
                    100);

    private final Label percent =
            new Label("100%");

    ZoomOverlay() {
        super(7);

        Button plus =
                new Button();

        Button minus =
                new Button();

        plus.setGraphic(
                UiIconFactory.zoomIn());

        minus.setGraphic(
                UiIconFactory.zoomOut());

        plus.getStyleClass()
                .add("zoom-button");

        minus.getStyleClass()
                .add("zoom-button");

        plus.setTooltip(
                new Tooltip(
                        "Acercar 10%."));

        minus.setTooltip(
                new Tooltip(
                        "Alejar 10%."));

        slider.setOrientation(
                Orientation.VERTICAL);

        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(25);
        slider.setMinorTickCount(0);
        slider.setBlockIncrement(10);
        slider.setPrefHeight(175);

        percent.getStyleClass()
                .add("zoom-percent");

        plus.setOnAction(event ->
                setValue(
                        getValue() + 10));

        minus.setOnAction(event ->
                setValue(
                        getValue() - 10));

        slider.valueProperty()
                .addListener((observable, oldValue, newValue) ->
                        percent.setText(
                                Math.round(
                                        newValue.doubleValue())
                                        + "%"));

        getChildren()
                .addAll(
                        plus,
                        slider,
                        minus,
                        percent);

        setAlignment(
                Pos.CENTER);

        setMaxWidth(
                USE_PREF_SIZE);

        setMaxHeight(
                USE_PREF_SIZE);

        getStyleClass()
                .add("zoom-overlay");
    }

    double getValue() {
        return slider.getValue();
    }

    void setValue(double value) {
        slider.setValue(
                clamp(value));
    }

    DoubleProperty valueProperty() {
        return slider.valueProperty();
    }

    private double clamp(double value) {
        return Math.max(
                MIN_ZOOM,
                Math.min(
                        MAX_ZOOM,
                        value));
    }
}
