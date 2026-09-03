package com.marcosmoreiradev.reconcilelab.ui.component;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.Locale;

/**
 * Indicador reutilizable de progreso para superficies JavaFX.
 *
 * <p>Texto y barra dependen de una única propiedad observable. La animación es
 * puramente de presentación: nunca modifica el runtime ni el algoritmo.</p>
 */
public final class AnimatedProgressIndicator extends VBox {

    private final ProgressBar bar = new ProgressBar(0);
    private final Label label = new Label("Progreso de ejecución: 0.0%");
    private final DoubleProperty displayed = new SimpleDoubleProperty(0);

    private Timeline animation;

    public AnimatedProgressIndicator() {
        super(5);

        getStyleClass().add("animated-progress");
        setAlignment(Pos.CENTER_LEFT);

        bar.setMaxWidth(Double.MAX_VALUE);
        bar.progressProperty().bind(displayed);

        displayed.addListener((obs, oldValue, newValue) ->
                label.setText(String.format(
                        Locale.ROOT,
                        "Progreso de ejecución: %.1f%%",
                        newValue.doubleValue() * 100.0)));

        getChildren().addAll(label, bar);
    }

    /**
     * Publica un objetivo entre 0 y 1.
     *
     * <p>Los saltos hacia abajo se aplican de inmediato porque normalmente
     * significan un caso nuevo o un reinicio. Los avances se interpolan.</p>
     */
    public void setTarget(double value) {
        double target = clamp(value);

        if (animation != null) {
            animation.stop();
        }

        if (target <= displayed.get()) {
            displayed.set(target);
            return;
        }

        animation = new Timeline(
                new KeyFrame(
                        Duration.ZERO,
                        new KeyValue(displayed, displayed.get())),
                new KeyFrame(
                        Duration.millis(150),
                        new KeyValue(displayed, target)));

        animation.play();
    }

    public double displayedProgress() {
        return displayed.get();
    }

    private double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
