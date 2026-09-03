package com.marcosmoreiradev.reconcilelab.ui.workspace;

/**
 * Política geométrica de navegación del lienzo.
 *
 * <p>El control de zoom flota sobre el viewport. Para que nunca se sienta como
 * una pared que tapa el mundo, el paneo admite un margen extra constante en
 * píxeles de pantalla y el centrado de nodos reserva espacio a la derecha.</p>
 */
final class ViewportNavigationPolicy {

    static final double PAN_OVERSCROLL_SCREEN = 140.0;
    static final double FOCUS_RIGHT_RESERVE_SCREEN = 92.0;

    private ViewportNavigationPolicy() {
    }

    /**
     * Convierte el margen adicional de pantalla a coordenadas del mundo.
     *
     * @param scale factor de zoom
     * @return margen lógico por lado
     */
    static double overscrollWorld(double scale) {
        return PAN_OVERSCROLL_SCREEN
                / safeScale(scale);
    }

    /**
     * Ancho lógico útil para centrar una tarjeta sin esconderla bajo el zoom.
     *
     * @param viewportScreenWidth ancho físico del canvas
     * @param scale factor de zoom
     * @return ancho lógico no cubierto por el overlay
     */
    static double focusWorldWidth(
            double viewportScreenWidth,
            double scale) {

        double usable =
                Math.max(
                        1.0,
                        viewportScreenWidth
                                - FOCUS_RIGHT_RESERVE_SCREEN);

        return usable
                / safeScale(scale);
    }

    private static double safeScale(double scale) {
        return Math.max(
                0.01,
                scale);
    }
}
