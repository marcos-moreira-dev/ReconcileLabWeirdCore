package com.marcosmoreiradev.reconcilelab.app;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LayerDependencySourceTest {

    private static final Path SOURCE_ROOT =
            Path.of(
                    "src",
                    "main",
                    "java",
                    "com",
                    "marcosmoreiradev",
                    "reconcilelab");

    @Test
    void dominioMotorYRuntimeNoImportanJavaFx() throws Exception {
        for (String layer : List.of(
                "domain",
                "engine",
                "runtime")) {

            Path directory =
                    SOURCE_ROOT.resolve(layer);

            assertTrue(
                    Files.isDirectory(directory),
                    "No existe la capa: "
                            + directory);

            assertLayerHasNoJavaFxImports(directory);
        }
    }

    private void assertLayerHasNoJavaFxImports(
            Path directory) throws IOException {

        try (var files = Files.walk(directory)) {
            for (Path file : files
                    .filter(path ->
                            path.toString()
                                    .endsWith(".java"))
                    .toList()) {

                List<String> lines =
                        Files.readAllLines(file);

                boolean importsJavaFx =
                        lines.stream()
                                .map(String::strip)
                                .anyMatch(line ->
                                        line.startsWith(
                                                "import javafx."));

                assertTrue(
                        !importsJavaFx,
                        "Dependencia JavaFX no permitida en "
                                + file);
            }
        }
    }
}
