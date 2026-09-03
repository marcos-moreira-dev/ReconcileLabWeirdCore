package com.marcosmoreiradev.reconcilelab.ui.export;

import com.marcosmoreiradev.reconcilelab.domain.ExecutionNode;
import com.marcosmoreiradev.reconcilelab.domain.ExecutionSnapshot;
import com.marcosmoreiradev.reconcilelab.io.MoneyText;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Calcula una disposición compacta y legible para la lámina de estudio.
 *
 * <p>La exportación no copia las coordenadas del viewport. Para trazas grandes
 * prueba perfiles cada vez más densos, pero nunca elimina el texto de una
 * tarjeta retenida. El objetivo es reducir dimensiones sin sacrificar la
 * posibilidad de abrir el PNG y acercarse hasta leer cada estado.</p>
 */
public final class StudyPosterLayout {

    private static final long DEFAULT_GRAPH_PIXEL_BUDGET = 62_000_000L;
    private static final int MAX_DIMENSION = 16_000;

    private final Map<Long, NodeBox> boxes;
    private final int width;
    private final int height;
    private final Profile profile;

    private StudyPosterLayout(
            Map<Long, NodeBox> boxes,
            int width,
            int height,
            Profile profile) {

        this.boxes = Map.copyOf(boxes);
        this.width = width;
        this.height = height;
        this.profile = profile;
    }

    /**
     * Crea una disposición adaptativa con el presupuesto estándar.
     *
     * @param snapshot traza retenida
     * @return layout calculado
     */
    public static StudyPosterLayout from(ExecutionSnapshot snapshot) {
        return from(
                snapshot,
                DEFAULT_GRAPH_PIXEL_BUDGET);
    }

    /**
     * Crea una disposición adaptativa con presupuesto explícito.
     *
     * @param snapshot traza retenida
     * @param graphPixelBudget píxeles máximos preferidos para el bloque gris
     * @return layout calculado
     */
    static StudyPosterLayout from(
            ExecutionSnapshot snapshot,
            long graphPixelBudget) {

        if (snapshot.nodes().isEmpty()) {
            throw new IllegalArgumentException(
                    "No existe una traza para disponer.");
        }

        for (Profile profile : Profile.values()) {
            StudyPosterLayout candidate =
                    bestForProfile(
                            snapshot,
                            profile,
                            graphPixelBudget);

            if (candidate != null) {
                return candidate;
            }
        }

        throw new IllegalStateException(
                "La traza retenida sigue siendo demasiado grande para una "
                        + "lámina PNG legible incluso en modo compacto.");
    }

    /** @return caja de un nodo o {@code null} */
    public NodeBox box(long nodeId) {
        return boxes.get(nodeId);
    }

    /** @return ancho del área gráfica */
    public int width() {
        return width;
    }

    /** @return alto del área gráfica */
    public int height() {
        return height;
    }

    /** @return alto de cada tarjeta */
    int nodeHeight() {
        return profile.nodeHeight();
    }

    /** @return alto de cabecera de tarjeta */
    int nodeHeaderHeight() {
        return profile.headerHeight();
    }

    /** @return tamaño de fuente de cabecera */
    int titleFontSize() {
        return profile.titleFontSize();
    }

    /** @return tamaño de fuente del cuerpo */
    int bodyFontSize() {
        return profile.bodyFontSize();
    }

    /** @return si se aplicó un perfil espacialmente comprimido */
    boolean compacted() {
        return profile != Profile.STANDARD;
    }

    private static StudyPosterLayout bestForProfile(
            ExecutionSnapshot snapshot,
            Profile profile,
            long graphPixelBudget) {

        StudyPosterLayout best = null;
        double bestScore = Double.POSITIVE_INFINITY;

        int minColumns =
                Math.min(
                        profile.minColumns(),
                        Math.max(
                                1,
                                snapshot.nodes().size()));

        int maxColumns =
                Math.min(
                        profile.maxColumns(),
                        Math.max(
                                1,
                                snapshot.nodes().size()));

        for (int columns = minColumns; columns <= maxColumns; columns++) {
            StudyPosterLayout candidate =
                    build(
                            snapshot,
                            profile,
                            columns);

            long pixels =
                    (long) candidate.width()
                            * candidate.height();

            if (pixels > graphPixelBudget
                    || candidate.width() > MAX_DIMENSION
                    || candidate.height() > MAX_DIMENSION) {

                continue;
            }

            double aspect =
                    (double) candidate.width()
                            / candidate.height();

            double aspectPenalty =
                    Math.abs(
                            Math.log(
                                    Math.max(
                                            0.01,
                                            aspect / 0.92)));

            double dimensionPenalty =
                    Math.max(
                            candidate.width(),
                            candidate.height())
                            / 10_000.0;

            double score =
                    aspectPenalty
                            + dimensionPenalty;

            if (score < bestScore) {
                best = candidate;
                bestScore = score;
            }
        }

        return best;
    }

    private static StudyPosterLayout build(
            ExecutionSnapshot snapshot,
            Profile profile,
            int columns) {

        Map<Integer, List<ExecutionNode>> levels =
                new TreeMap<>();

        int maxNodeWidth =
                profile.minNodeWidth();

        for (ExecutionNode node : snapshot.nodes()) {
            levels.computeIfAbsent(
                            node.depth(),
                            ignored -> new ArrayList<>())
                    .add(node);

            maxNodeWidth =
                    Math.max(
                            maxNodeWidth,
                            nodeWidth(
                                    node,
                                    snapshot.itemCount(),
                                    profile));
        }

        int width =
                profile.outerPadding() * 2
                        + columns * maxNodeWidth
                        + Math.max(
                                0,
                                columns - 1)
                        * profile.gapX();

        Map<Long, NodeBox> boxes =
                new LinkedHashMap<>();

        int bandY =
                profile.topPadding();

        for (List<ExecutionNode> level : levels.values()) {
            int rows =
                    Math.max(
                            1,
                            (level.size() + columns - 1)
                                    / columns);

            for (int index = 0; index < level.size(); index++) {
                ExecutionNode node =
                        level.get(index);

                int row =
                        index / columns;

                int column =
                        index % columns;

                int x =
                        profile.outerPadding()
                                + column
                                * (maxNodeWidth + profile.gapX());

                int y =
                        bandY
                                + row
                                * (profile.nodeHeight()
                                + profile.gapY());

                boxes.put(
                        node.id(),
                        new NodeBox(
                                x,
                                y,
                                nodeWidth(
                                        node,
                                        snapshot.itemCount(),
                                        profile),
                                profile.nodeHeight()));
            }

            bandY +=
                    rows
                            * (profile.nodeHeight()
                            + profile.gapY())
                            + profile.bandGap();
        }

        int height =
                Math.max(
                        profile.minimumGraphHeight(),
                        bandY + profile.bottomPadding());

        return new StudyPosterLayout(
                boxes,
                width,
                height,
                profile);
    }

    private static int nodeWidth(
            ExecutionNode node,
            int itemCount,
            Profile profile) {

        String title =
                "ESTADO DE BÚSQUEDA #"
                        + node.id();

        String movement =
                "Movimiento: "
                        + node.index()
                        + " / "
                        + itemCount;

        String amount =
                "Acumulado: "
                        + MoneyText.formatCents(
                                node.sumCents());

        int longest =
                Math.max(
                        title.length(),
                        Math.max(
                                movement.length(),
                                amount.length()));

        double characterWidth =
                profile.bodyFontSize()
                        * 0.63;

        int estimated =
                (int) Math.ceil(
                        profile.textPadding() * 2
                                + longest * characterWidth);

        return Math.max(
                profile.minNodeWidth(),
                Math.min(
                        profile.maxNodeWidth(),
                        estimated));
    }

    /**
     * Perfiles de densidad. Todos conservan título, movimiento, monto y estado.
     */
    private enum Profile {

        STANDARD(
                190, 300, 84, 20,
                11, 11,
                34, 28, 54,
                30, 35, 20,
                520, 8, 14, 7),

        DENSE(
                160, 220, 72, 18,
                9, 9,
                18, 16, 30,
                22, 26, 18,
                440, 10, 24, 6),

        COMPACT(
                148, 196, 66, 17,
                8, 8,
                12, 12, 22,
                18, 22, 16,
                400, 12, 30, 5);

        private final int minNodeWidth;
        private final int maxNodeWidth;
        private final int nodeHeight;
        private final int headerHeight;
        private final int titleFontSize;
        private final int bodyFontSize;
        private final int gapX;
        private final int gapY;
        private final int bandGap;
        private final int outerPadding;
        private final int topPadding;
        private final int bottomPadding;
        private final int minimumGraphHeight;
        private final int minColumns;
        private final int maxColumns;
        private final int textPadding;

        Profile(
                int minNodeWidth,
                int maxNodeWidth,
                int nodeHeight,
                int headerHeight,
                int titleFontSize,
                int bodyFontSize,
                int gapX,
                int gapY,
                int bandGap,
                int outerPadding,
                int topPadding,
                int bottomPadding,
                int minimumGraphHeight,
                int minColumns,
                int maxColumns,
                int textPadding) {

            this.minNodeWidth = minNodeWidth;
            this.maxNodeWidth = maxNodeWidth;
            this.nodeHeight = nodeHeight;
            this.headerHeight = headerHeight;
            this.titleFontSize = titleFontSize;
            this.bodyFontSize = bodyFontSize;
            this.gapX = gapX;
            this.gapY = gapY;
            this.bandGap = bandGap;
            this.outerPadding = outerPadding;
            this.topPadding = topPadding;
            this.bottomPadding = bottomPadding;
            this.minimumGraphHeight = minimumGraphHeight;
            this.minColumns = minColumns;
            this.maxColumns = maxColumns;
            this.textPadding = textPadding;
        }

        int minNodeWidth() {
            return minNodeWidth;
        }

        int maxNodeWidth() {
            return maxNodeWidth;
        }

        int nodeHeight() {
            return nodeHeight;
        }

        int headerHeight() {
            return headerHeight;
        }

        int titleFontSize() {
            return titleFontSize;
        }

        int bodyFontSize() {
            return bodyFontSize;
        }

        int gapX() {
            return gapX;
        }

        int gapY() {
            return gapY;
        }

        int bandGap() {
            return bandGap;
        }

        int outerPadding() {
            return outerPadding;
        }

        int topPadding() {
            return topPadding;
        }

        int bottomPadding() {
            return bottomPadding;
        }

        int minimumGraphHeight() {
            return minimumGraphHeight;
        }

        int minColumns() {
            return minColumns;
        }

        int maxColumns() {
            return maxColumns;
        }

        int textPadding() {
            return textPadding;
        }
    }

    /** Rectángulo lógico de una tarjeta exportada. */
    public record NodeBox(
            int x,
            int y,
            int width,
            int height) {

        /** @return centro horizontal */
        public int centerX() {
            return x + width / 2;
        }
    }
}
