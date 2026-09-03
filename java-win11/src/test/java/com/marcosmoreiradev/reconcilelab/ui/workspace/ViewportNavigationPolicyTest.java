package com.marcosmoreiradev.reconcilelab.ui.workspace;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ViewportNavigationPolicyTest {

    @Test
    void overscrollConservaMargenFisicoAlCambiarZoom() {
        assertEquals(
                140.0,
                ViewportNavigationPolicy.overscrollWorld(1.0));

        assertEquals(
                280.0,
                ViewportNavigationPolicy.overscrollWorld(0.5));
    }

    @Test
    void centradoReservaEspacioParaElControlFlotante() {
        double width =
                ViewportNavigationPolicy.focusWorldWidth(
                        1_000,
                        1.0);

        assertTrue(width < 1_000);
        assertEquals(908.0, width);
    }
}
