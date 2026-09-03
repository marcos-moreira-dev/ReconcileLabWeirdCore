package com.marcosmoreiradev.reconcilelab.io;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MoneyTextTest {

    @Test
    void aceptaPuntoYComaDecimal() {
        assertEquals(28_740, MoneyText.parseCents("287.40"));
        assertEquals(28_740, MoneyText.parseCents("287,40"));
    }

    @Test
    void rechazaNegativosYMasDeDosDecimales() {
        assertThrows(IllegalArgumentException.class,
                () -> MoneyText.parseCents("-1.00"));

        assertThrows(IllegalArgumentException.class,
                () -> MoneyText.parseCents("12.345"));
    }
}
