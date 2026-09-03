package com.marcosmoreiradev.reconcilelab.app;

/**
 * Identidad canónica de la edición Java.
 *
 * <p>Centraliza los literales que también consumen la ventana principal,
 * diagnósticos y documentación. El packaging de Windows replica estos valores
 * en jpackage.</p>
 */
public final class AppMetadata {

    public static final String PRODUCT_NAME = "ReconcileLab";
    public static final String DISPLAY_NAME = "ReconcileLab — Conciliación y trazabilidad";
    public static final String EDITION = "Java Edition para Windows 11";
    public static final String VENDOR = "Marcos Moreira Dev";
    public static final String PACKAGE_BASE = "com.marcosmoreiradev.reconcilelab";
    public static final String MAVEN_GROUP_ID = "com.marcosmoreiradev";

    private AppMetadata() {
    }
}
