package com.marcosmoreiradev.reconcilelab.runtime;

public enum ExecutionMode {
    STUDY("Estudio"),
    BALANCED("Equilibrado"),
    MAXIMUM("Máximo");

    private final String label;

    ExecutionMode(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
