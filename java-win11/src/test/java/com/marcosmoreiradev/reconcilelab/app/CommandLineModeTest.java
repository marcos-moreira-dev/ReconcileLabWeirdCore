package com.marcosmoreiradev.reconcilelab.app;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandLineModeTest {

    @TempDir
    Path tempDirectory;

    @Test
    void reconoceSmokeFile() {
        Path target =
                tempDirectory.resolve("smoke.txt");

        assertTrue(
                CommandLineMode.smokeFile(
                                List.of(
                                        "--smoke-file="
                                                + target))
                        .isPresent());
    }

    @Test
    void ignoraArgumentosNormales() {
        assertFalse(
                CommandLineMode.smokeFile(
                                List.of(
                                        "--case=C:\\casos\\demo.case"))
                        .isPresent());
    }

    @Test
    void ejecutaSmokeSinJavaFx() throws Exception {
        Path target =
                tempDirectory.resolve("smoke.txt");

        assertTrue(
                CommandLineMode.tryHandle(
                        new String[] {
                                "--smoke-file="
                                        + target
                        }));

        String content =
                Files.readString(target);

        assertTrue(content.contains("product=ReconcileLab"));
        assertTrue(content.contains("vendor=Marcos Moreira Dev"));
        assertTrue(
                content.contains(
                        "package=com.marcosmoreiradev.reconcilelab"));
        assertTrue(content.contains("java="));
    }
}
