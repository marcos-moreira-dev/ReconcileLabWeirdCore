package com.marcosmoreiradev.reconcilelab.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimelineDockModelTest {

    @Test
    void limitaElTimelineExpandidoAUnaFranjaUtil() {
        assertEquals(
                TimelineDockModel.MIN_EXPANDED_DIVIDER,
                TimelineDockModel.normalizeExpandedDivider(0.2));

        assertEquals(
                TimelineDockModel.MAX_EXPANDED_DIVIDER,
                TimelineDockModel.normalizeExpandedDivider(0.99));

        assertEquals(
                0.78,
                TimelineDockModel.normalizeExpandedDivider(0.78));
    }

    @Test
    void colapsadoDejaVisibleUnaCabeceraReal() {
        double divider =
                TimelineDockModel.collapsedDivider(
                        900,
                        36);

        assertTrue(divider > 0.90);
        assertTrue(divider < 0.97);
    }
}
