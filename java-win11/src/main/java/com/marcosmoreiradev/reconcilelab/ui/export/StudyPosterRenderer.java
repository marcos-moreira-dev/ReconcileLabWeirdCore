package com.marcosmoreiradev.reconcilelab.ui.export;

import com.marcosmoreiradev.reconcilelab.domain.ExecutionSnapshot;
import com.marcosmoreiradev.reconcilelab.domain.ProblemInstance;
import com.marcosmoreiradev.reconcilelab.engine.SearchStrategy;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

/** Orquesta la creación de la imagen PNG de estudio. */
public final class StudyPosterRenderer {

    private static final long MAX_IMAGE_PIXELS =
            72_000_000L;

    private final StudyPosterHeaderPainter headerPainter =
            new StudyPosterHeaderPainter();

    private final StudyPosterGraphPainter graphPainter =
            new StudyPosterGraphPainter();

    /**
     * Exporta una lámina PNG lossless.
     *
     * <p>En trazas grandes el layout se compacta antes de crear el bitmap. Las
     * tarjetas retenidas siguen conteniendo título, movimiento, monto y estado,
     * de modo que el usuario puede abrir la imagen y acercarse sin que falte la
     * información del diagrama.</p>
     *
     * @param path destino
     * @param problem caso de conciliación
     * @param strategy estrategia utilizada
     * @param snapshot snapshot final o parcial
     * @throws IOException si el PNG no puede escribirse
     */
    public void export(
            Path path,
            ProblemInstance problem,
            SearchStrategy strategy,
            ExecutionSnapshot snapshot) throws IOException {

        if (snapshot.nodes().isEmpty()) {
            throw new IllegalStateException(
                    "Todavía no existe una traza detallada para exportar.");
        }

        StudyPosterLayout layout =
                StudyPosterLayout.from(
                        snapshot);

        int headerHeight =
                headerPainter.requiredHeight(
                        problem);

        int margin =
                30;

        int imageWidth =
                Math.max(
                        1_150,
                        layout.width()
                                + margin * 2);

        int imageHeight =
                headerHeight
                        + layout.height()
                        + margin * 2;

        validateSize(
                imageWidth,
                imageHeight);

        BufferedImage image =
                new BufferedImage(
                        imageWidth,
                        imageHeight,
                        BufferedImage.TYPE_INT_RGB);

        Graphics2D graphics =
                image.createGraphics();

        try {
            configureGraphics(
                    graphics);

            graphics.setColor(
                    Color.WHITE);

            graphics.fillRect(
                    0,
                    0,
                    imageWidth,
                    imageHeight);

            headerPainter.paint(
                    graphics,
                    problem,
                    strategy,
                    snapshot,
                    margin,
                    headerHeight,
                    imageWidth);

            int graphX =
                    margin;

            int graphY =
                    headerHeight
                            + margin;

            graphics.setColor(
                    new Color(
                            63,
                            70,
                            80));

            graphics.fillRect(
                    graphX,
                    graphY,
                    layout.width(),
                    layout.height());

            graphPainter.paint(
                    graphics,
                    snapshot,
                    layout,
                    graphX,
                    graphY);
        } finally {
            graphics.dispose();
        }

        boolean written =
                ImageIO.write(
                        image,
                        "png",
                        path.toFile());

        if (!written) {
            throw new IOException(
                    "No existe un escritor PNG disponible.");
        }
    }

    private void validateSize(
            int width,
            int height) {

        long pixels =
                (long) width
                        * height;

        if (pixels > MAX_IMAGE_PIXELS) {
            throw new IllegalStateException(
                    "La traza retenida no cabe en una lámina PNG legible "
                            + "dentro del límite seguro de memoria.");
        }
    }

    private void configureGraphics(
            Graphics2D graphics) {

        graphics.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        graphics.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        graphics.setRenderingHint(
                RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);

        graphics.setRenderingHint(
                RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_PURE);
    }
}
