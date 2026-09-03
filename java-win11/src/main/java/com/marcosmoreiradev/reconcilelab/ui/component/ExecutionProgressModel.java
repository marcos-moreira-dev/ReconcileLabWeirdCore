package com.marcosmoreiradev.reconcilelab.ui.component;

import com.marcosmoreiradev.reconcilelab.domain.ExecutionSnapshot;

/**
 * Traduce métricas del motor a los dos porcentajes que la interfaz presenta.
 *
 * <p>Es deliberadamente independiente de JavaFX. El porcentaje de espacio
 * contabilizado y el progreso de ejecución no significan lo mismo cuando el
 * objetivo permite terminar temprano.</p>
 */
public record ExecutionProgressModel(
        long accountedStates,
        double accountedRatio,
        double executionRatio) {

    public static ExecutionProgressModel from(
            ExecutionSnapshot snapshot) {

        long potential =
                snapshot.potentialStates();

        long accounted =
                Math.min(
                        potential,
                        snapshot.visited()
                                + snapshot.avoidedStates());

        double ratio =
                potential == 0
                        ? 0.0
                        : (double) accounted
                        / potential;

        double execution =
                snapshot.completed()
                        ? 1.0
                        : ratio;

        return new ExecutionProgressModel(
                accounted,
                clamp(ratio),
                clamp(execution));
    }

    private static double clamp(double value) {
        return Math.max(
                0.0,
                Math.min(1.0, value));
    }
}
