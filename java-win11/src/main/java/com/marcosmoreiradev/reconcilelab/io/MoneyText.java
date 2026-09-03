package com.marcosmoreiradev.reconcilelab.io;

import java.util.Locale;

public final class MoneyText {

    private MoneyText() {
    }

    public static int parseCents(String raw) {
        String text = raw == null ? "" : raw.trim().replace(',', '.');

        if (text.startsWith("-")) {
            throw new IllegalArgumentException("Los montos negativos no están permitidos.");
        }

        if (!text.matches("\\d+(\\.\\d{1,2})?")) {
            throw new IllegalArgumentException("Monto no válido: " + raw);
        }

        String[] parts = text.split("\\.", -1);
        long whole = Long.parseLong(parts[0]);
        int decimals = 0;

        if (parts.length == 2) {
            decimals = Integer.parseInt(
                    parts[1].length() == 1 ? parts[1] + "0" : parts[1]);
        }

        long cents = whole * 100L + decimals;
        if (cents > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Monto demasiado grande.");
        }

        return (int) cents;
    }

    public static String formatCents(int cents) {
        return String.format(
                Locale.ROOT,
                "$%d.%02d",
                cents / 100,
                Math.abs(cents % 100));
    }
}
