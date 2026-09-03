package com.marcosmoreiradev.reconcilelab.app;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StartupArgumentsTest {

    @Test
    void aceptaRutaPasadaPorAsociacionDeWindows() {
        var result =
                StartupArguments.casePath(
                        List.of("C:\\Casos\\cliente.case"));

        assertTrue(result.isPresent());
        assertTrue(result.get().endsWith(
                Path.of("Casos", "cliente.case")));
    }

    @Test
    void aceptaFormaExplicitaCase() {
        var result =
                StartupArguments.casePath(
                        List.of("--case=ejemplo.case"));

        assertTrue(result.isPresent());
        assertEquals(
                "ejemplo.case",
                result.get().getFileName().toString());
    }

    @Test
    void ignoraOpcionesDesconocidas() {
        assertTrue(
                StartupArguments.casePath(
                        List.of("--verbose", "-x"))
                        .isEmpty());
    }
}
