package com.marcosmoreiradev.reconcilelab.app;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Modos de línea de comandos que pueden ejecutarse antes de levantar JavaFX.
 *
 * <p>Permite que una app-image producida por {@code jpackage} demuestre que
 * launcher, runtime embebido y JAR principal arrancan correctamente sin abrir
 * una ventana durante un gate automatizado.</p>
 */
public final class CommandLineMode {

    private static final String SMOKE_PREFIX = "--smoke-file=";

    private CommandLineMode() {
    }

    /**
     * Ejecuta un modo no gráfico si los argumentos lo solicitan.
     *
     * @param args argumentos entregados al launcher
     * @return {@code true} si el proceso debe terminar sin iniciar JavaFX
     */
    public static boolean tryHandle(String[] args) {
        Optional<Path> smokeFile =
                smokeFile(
                        args == null
                                ? List.of()
                                : List.of(args));

        if (smokeFile.isEmpty()) {
            return false;
        }

        writeSmokeEvidence(smokeFile.get());
        return true;
    }

    /**
     * Obtiene el destino del smoke test.
     *
     * @param rawArguments argumentos crudos
     * @return destino normalizado cuando existe {@code --smoke-file=}
     */
    static Optional<Path> smokeFile(List<String> rawArguments) {
        if (rawArguments == null) {
            return Optional.empty();
        }

        for (String raw : rawArguments) {
            if (raw == null || raw.isBlank()) {
                continue;
            }

            String value = raw.trim();

            if (!value.startsWith(SMOKE_PREFIX)) {
                continue;
            }

            String pathText =
                    value.substring(
                                    SMOKE_PREFIX.length())
                            .trim();

            if (pathText.isBlank()) {
                continue;
            }

            return Optional.of(
                    Path.of(pathText)
                            .toAbsolutePath()
                            .normalize());
        }

        return Optional.empty();
    }

    private static void writeSmokeEvidence(Path output) {
        try {
            Path parent = output.getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }

            String content =
                    "product="
                            + AppMetadata.PRODUCT_NAME
                            + System.lineSeparator()
                            + "vendor="
                            + AppMetadata.VENDOR
                            + System.lineSeparator()
                            + "package="
                            + AppMetadata.PACKAGE_BASE
                            + System.lineSeparator()
                            + "version="
                            + BuildInfo.version()
                            + System.lineSeparator()
                            + "java="
                            + System.getProperty("java.version")
                            + System.lineSeparator()
                            + "os="
                            + System.getProperty("os.name")
                            + System.lineSeparator();

            Files.writeString(
                    output,
                    content,
                    StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "No se pudo escribir la evidencia de smoke test: "
                            + output,
                    exception);
        }
    }
}
