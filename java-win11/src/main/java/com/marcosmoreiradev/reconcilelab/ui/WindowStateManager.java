package com.marcosmoreiradev.reconcilelab.ui;

import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.prefs.Preferences;

/**
 * Conserva el estado básico de la ventana principal.
 *
 * <p>En la primera ejecución la aplicación abre maximizada dentro del área
 * útil de Windows 11. En ejecuciones posteriores respeta si el operador cerró
 * la ventana maximizada o restaurada.</p>
 */
final class WindowStateManager {

    private static final String KEY_INITIALIZED = "initialized";
    private static final String KEY_MAXIMIZED = "maximized";
    private static final String KEY_WIDTH = "width";
    private static final String KEY_HEIGHT = "height";
    private static final String KEY_X = "x";
    private static final String KEY_Y = "y";

    private static final double DEFAULT_WIDTH = 1_520;
    private static final double DEFAULT_HEIGHT = 900;

    private final Preferences preferences = Preferences.userNodeForPackage(WindowStateManager.class);

    /**
     * Prepara dimensiones restaurables antes de mostrar el Stage.
     *
     * @param stage ventana principal
     */
    void prepare(Stage stage) {
        stage.setMinWidth(1_100);
        stage.setMinHeight(700);

        if (!preferences.getBoolean(KEY_INITIALIZED, false)) {
            stage.setWidth(DEFAULT_WIDTH);
            stage.setHeight(DEFAULT_HEIGHT);
            return;
        }

        double width = preferences.getDouble(KEY_WIDTH, DEFAULT_WIDTH);
        double height = preferences.getDouble(KEY_HEIGHT, DEFAULT_HEIGHT);
        double x = preferences.getDouble(KEY_X, Double.NaN);
        double y = preferences.getDouble(KEY_Y, Double.NaN);

        if (isVisible(x, y, width, height)) {
            stage.setX(x);
            stage.setY(y);
            stage.setWidth(width);
            stage.setHeight(height);
        } else {
            stage.setWidth(DEFAULT_WIDTH);
            stage.setHeight(DEFAULT_HEIGHT);
        }
    }

    /**
     * Muestra la ventana y aplica la maximización después de crear el peer nativo.
     *
     * @param stage ventana principal
     */
    void show(Stage stage) {
        stage.show();
        boolean maximize = preferences.getBoolean(KEY_MAXIMIZED, true);
        Platform.runLater(() -> stage.setMaximized(maximize));
    }

    /**
     * Persiste el estado antes del cierre.
     *
     * @param stage ventana principal
     */
    void save(Stage stage) {
        preferences.putBoolean(KEY_INITIALIZED, true);
        preferences.putBoolean(KEY_MAXIMIZED, stage.isMaximized());

        if (!stage.isMaximized()) {
            preferences.putDouble(KEY_WIDTH, stage.getWidth());
            preferences.putDouble(KEY_HEIGHT, stage.getHeight());
            preferences.putDouble(KEY_X, stage.getX());
            preferences.putDouble(KEY_Y, stage.getY());
        }
    }

    private boolean isVisible(double x, double y, double width, double height) {
        if (!Double.isFinite(x) || !Double.isFinite(y) || width < 600 || height < 450) {
            return false;
        }

        Rectangle2D candidate = new Rectangle2D(x, y, width, height);
        return Screen.getScreens().stream()
                .map(Screen::getVisualBounds)
                .anyMatch(bounds -> bounds.intersects(candidate));
    }
}
