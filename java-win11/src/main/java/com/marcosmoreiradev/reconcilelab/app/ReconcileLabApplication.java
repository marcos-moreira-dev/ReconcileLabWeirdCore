package com.marcosmoreiradev.reconcilelab.app;

import com.marcosmoreiradev.reconcilelab.runtime.RuntimeController;
import com.marcosmoreiradev.reconcilelab.ui.MainWindow;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Ciclo de vida JavaFX.
 */
public final class ReconcileLabApplication extends Application {

    private RuntimeController runtime;

    @Override
    public void start(Stage stage) {
        runtime =
                new RuntimeController();

        MainWindow window =
                new MainWindow(
                        stage,
                        runtime);

        window.show();

        Optional<Path> startupCase =
                StartupArguments.casePath(
                        getParameters().getRaw());

        startupCase.ifPresent(path ->
                Platform.runLater(
                        () -> window.loadExternalCase(path)));
    }

    @Override
    public void stop() {
        if (runtime != null) {
            runtime.close();
        }
    }
}
