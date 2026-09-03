package com.marcosmoreiradev.reconcilelab.app;

import javafx.application.Application;

/**
 * Main class separada de Application.
 *
 * <p>Facilita ejecución desde JAR y packaging con jpackage.</p>
 */
public final class Launcher {

    private Launcher() {
    }

    /**
     * Punto de entrada del proceso.
     *
     * @param args argumentos de Windows/jpackage
     */
    public static void main(String[] args) {
        if (CommandLineMode.tryHandle(args)) {
            return;
        }

        Application.launch(
                ReconcileLabApplication.class,
                args);
    }
}
