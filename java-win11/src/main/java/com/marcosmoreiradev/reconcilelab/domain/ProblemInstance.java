package com.marcosmoreiradev.reconcilelab.domain;

import java.util.List;
import java.util.Objects;

/**
 * Entrada completa de una conciliación por monto.
 *
 * <p>Los importes se guardan como centavos enteros. Se conserva así la misma
 * decisión de dominio de la edición C++: el dinero no pasa por punto flotante.</p>
 *
 * <p>La propia entidad protege los invariantes que necesita el motor. De esta
 * forma no existe un camino programático que pueda crear silenciosamente un
 * caso con montos negativos o más de 30 candidatos y luego romper la máscara
 * de selección.</p>
 */
public record ProblemInstance(
        String name,
        String description,
        List<Integer> valuesCents,
        int targetCents,
        SearchObjective objective) {

    public static final int MAX_CANDIDATES = 30;

    public ProblemInstance {
        name = Objects.requireNonNullElse(name, "");
        description = Objects.requireNonNullElse(description, "");
        valuesCents = List.copyOf(
                Objects.requireNonNull(valuesCents, "valuesCents"));
        objective = Objects.requireNonNull(objective, "objective");

        if (targetCents <= 0) {
            throw new IllegalArgumentException(
                    "El monto a conciliar debe ser mayor que cero.");
        }

        if (valuesCents.isEmpty()) {
            throw new IllegalArgumentException(
                    "El caso no contiene movimientos candidatos.");
        }

        if (valuesCents.size() > MAX_CANDIDATES) {
            throw new IllegalArgumentException(
                    "Esta versión admite como máximo "
                            + MAX_CANDIDATES
                            + " movimientos candidatos por caso.");
        }

        long total = 0;

        for (Integer value : valuesCents) {
            if (value == null || value <= 0) {
                throw new IllegalArgumentException(
                        "Los movimientos candidatos deben ser mayores que cero.");
            }

            total += value;

            if (total > Integer.MAX_VALUE) {
                throw new IllegalArgumentException(
                        "La suma de los movimientos candidatos es demasiado grande.");
            }
        }
    }

    public int itemCount() {
        return valuesCents.size();
    }
}
