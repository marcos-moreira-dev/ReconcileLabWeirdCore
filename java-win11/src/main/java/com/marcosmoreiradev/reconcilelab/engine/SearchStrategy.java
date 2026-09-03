package com.marcosmoreiradev.reconcilelab.engine;

public enum SearchStrategy {
    EXHAUSTIVE("Búsqueda exhaustiva"),
    PRUNING("Búsqueda con poda segura");

    private final String label;

    SearchStrategy(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
