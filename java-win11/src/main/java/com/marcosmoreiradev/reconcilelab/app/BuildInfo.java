package com.marcosmoreiradev.reconcilelab.app;

/**
 * Información de identidad de la edición Java.
 *
 * <p>La versión visible se obtiene del MANIFEST.MF cuando la aplicación se
 * ejecuta desde el JAR empaquetado. Durante desarrollo se usa un fallback
 * explícito para evitar valores nulos en Acerca de y diagnósticos.</p>
 */
public final class BuildInfo {

    public static final String FALLBACK_VERSION = "0.7.5-dev";

    private BuildInfo() {
    }

    /**
     * Devuelve la versión empaquetada o el identificador de desarrollo.
     */
    public static String version() {
        String value = BuildInfo.class
                .getPackage()
                .getImplementationVersion();

        return value == null || value.isBlank()
                ? FALLBACK_VERSION
                : value;
    }
}
