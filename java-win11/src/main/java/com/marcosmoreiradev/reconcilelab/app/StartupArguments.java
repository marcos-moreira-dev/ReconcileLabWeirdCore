package com.marcosmoreiradev.reconcilelab.app;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Interpreta únicamente los argumentos de arranque que pertenecen al producto.
 *
 * <p>La asociación de archivos de Windows lanza ReconcileLab pasando el
 * `.case` como argumento. La lógica se mantiene fuera de JavaFX para poder
 * probarla sin levantar una ventana.</p>
 */
public final class StartupArguments {

    private StartupArguments() {
    }

    public static Optional<Path> casePath(List<String> rawArguments) {
        if (rawArguments == null) {
            return Optional.empty();
        }

        for (String raw : rawArguments) {
            if (raw == null || raw.isBlank()) {
                continue;
            }

            String value = raw.trim();

            if (value.startsWith("--case=")) {
                value = value.substring("--case=".length()).trim();
            } else if (value.startsWith("-")) {
                continue;
            }

            if (!value.isBlank()) {
                return Optional.of(
                        Path.of(value)
                                .toAbsolutePath()
                                .normalize());
            }
        }

        return Optional.empty();
    }
}
