package com.marcosmoreiradev.reconcilelab.ui.component;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;

/**
 * Iconografía vectorial pequeña de la interfaz.
 *
 * <p>Los iconos se construyen con primitivas JavaFX para mantener nitidez a
 * cualquier DPI, evitar dependencias/licencias externas y conservar una
 * identidad visual sobria con pequeños acentos de color.</p>
 */
public final class UiIconFactory {

    private static final Color BLUE =
            Color.web("#3478b8");

    private static final Color GREEN =
            Color.web("#2d9a57");

    private static final Color AMBER =
            Color.web("#d28a22");

    private static final Color ORANGE =
            Color.web("#d66b35");

    private static final Color PURPLE =
            Color.web("#7b62b3");

    private static final Color NAVY =
            Color.web("#3d566d");

    private static final Color PAPER =
            Color.web("#f8fbfd");

    private UiIconFactory() {
    }

    /**
     * Hoja nueva con insignia verde.
     */
    public static Node newCase() {
        Rectangle page =
                rectangle(
                        3,
                        2,
                        12,
                        15,
                        PAPER,
                        NAVY);

        Polygon fold =
                new Polygon(
                        11.0, 2.0,
                        15.0, 6.0,
                        11.0, 6.0);

        fold.setFill(
                Color.web("#c9d9e7"));

        Circle badge =
                new Circle(
                        14,
                        14,
                        4.2,
                        GREEN);

        Line horizontal =
                line(
                        11.8,
                        14,
                        16.2,
                        14,
                        Color.WHITE,
                        1.4);

        Line vertical =
                line(
                        14,
                        11.8,
                        14,
                        16.2,
                        Color.WHITE,
                        1.4);

        return icon(
                page,
                fold,
                badge,
                horizontal,
                vertical);
    }

    /**
     * Carpeta abierta.
     */
    public static Node openCase() {
        Polygon folderBack =
                new Polygon(
                        2.0, 6.0,
                        7.0, 6.0,
                        9.0, 4.0,
                        16.0, 4.0,
                        16.0, 14.0,
                        2.0, 14.0);

        folderBack.setFill(
                Color.web("#e0a03b"));

        folderBack.setStroke(
                Color.web("#a86d1e"));

        Polygon folderFront =
                new Polygon(
                        2.0, 8.0,
                        17.0, 8.0,
                        14.5, 15.5,
                        2.0, 15.5);

        folderFront.setFill(
                Color.web("#f2bd55"));

        folderFront.setStroke(
                Color.web("#a86d1e"));

        return icon(
                folderBack,
                folderFront);
    }

    /**
     * Triángulo de ejecución.
     */
    public static Node run() {
        Polygon triangle =
                new Polygon(
                        4.0, 2.5,
                        16.0, 9.0,
                        4.0, 15.5);

        triangle.setFill(GREEN);
        triangle.setStroke(
                Color.web("#237944"));

        return icon(triangle);
    }

    /**
     * Pausa con acento ámbar.
     */
    public static Node pause() {
        Rectangle left =
                rectangle(
                        4,
                        3,
                        4,
                        12,
                        AMBER,
                        Color.web("#a56715"));

        Rectangle right =
                rectangle(
                        11,
                        3,
                        4,
                        12,
                        AMBER,
                        Color.web("#a56715"));

        return icon(
                left,
                right);
    }

    /**
     * Paso lógico: reproducción hasta una barra.
     */
    public static Node step() {
        Polygon triangle =
                new Polygon(
                        3.0, 3.0,
                        12.0, 9.0,
                        3.0, 15.0);

        triangle.setFill(BLUE);

        Rectangle stop =
                rectangle(
                        13,
                        3,
                        3,
                        12,
                        NAVY,
                        NAVY);

        return icon(
                triangle,
                stop);
    }

    /**
     * Flecha circular de reinicio.
     */
    public static Node reset() {
        Arc arc =
                new Arc(
                        9,
                        9,
                        6,
                        6,
                        35,
                        280);

        arc.setType(ArcType.OPEN);
        arc.setFill(
                Color.TRANSPARENT);

        arc.setStroke(ORANGE);
        arc.setStrokeWidth(2.2);
        arc.setStrokeLineCap(
                StrokeLineCap.ROUND);

        Polygon head =
                new Polygon(
                        3.2, 5.0,
                        3.0, 10.0,
                        7.2, 7.8);

        head.setFill(ORANGE);

        return icon(
                arc,
                head);
    }

    /**
     * Exportación a archivo.
     */
    public static Node exportImage() {
        Rectangle page =
                rectangle(
                        3,
                        2,
                        12,
                        15,
                        PAPER,
                        NAVY);

        Rectangle picture =
                rectangle(
                        5,
                        5,
                        8,
                        5,
                        Color.web("#d9e8f3"),
                        BLUE);

        Circle sun =
                new Circle(
                        7,
                        7,
                        1.2,
                        AMBER);

        Polygon mountain =
                new Polygon(
                        5.5, 10.0,
                        8.0, 7.7,
                        10.0, 9.2,
                        12.5, 6.8,
                        13.0, 10.0);

        mountain.setFill(PURPLE);

        return icon(
                page,
                picture,
                sun,
                mountain);
    }

    /**
     * Lupa con signo positivo.
     */
    public static Node zoomIn() {
        return zoom(true);
    }

    /**
     * Lupa con signo negativo.
     */
    public static Node zoomOut() {
        return zoom(false);
    }

    private static Node zoom(boolean plus) {
        Circle lens =
                new Circle(
                        7.5,
                        7.5,
                        5,
                        Color.web("#eef5fb"));

        lens.setStroke(BLUE);
        lens.setStrokeWidth(1.8);

        Line handle =
                line(
                        11.3,
                        11.3,
                        16,
                        16,
                        NAVY,
                        2.1);

        Line horizontal =
                line(
                        5.2,
                        7.5,
                        9.8,
                        7.5,
                        BLUE,
                        1.5);

        Group group =
                icon(
                        lens,
                        handle,
                        horizontal);

        if (plus) {
            Line vertical =
                    line(
                            7.5,
                            5.2,
                            7.5,
                            9.8,
                            BLUE,
                            1.5);

            group.getChildren()
                    .add(vertical);
        }

        return group;
    }

    private static Group icon(Node... nodes) {
        Group group =
                new Group(nodes);

        group.setManaged(true);
        return group;
    }

    private static Rectangle rectangle(
            double x,
            double y,
            double width,
            double height,
            Color fill,
            Color stroke) {

        Rectangle rectangle =
                new Rectangle(
                        x,
                        y,
                        width,
                        height);

        rectangle.setArcWidth(1.5);
        rectangle.setArcHeight(1.5);
        rectangle.setFill(fill);
        rectangle.setStroke(stroke);
        rectangle.setStrokeWidth(1);

        return rectangle;
    }

    private static Line line(
            double startX,
            double startY,
            double endX,
            double endY,
            Color color,
            double width) {

        Line line =
                new Line(
                        startX,
                        startY,
                        endX,
                        endY);

        line.setStroke(color);
        line.setStrokeWidth(width);
        line.setStrokeLineCap(
                StrokeLineCap.ROUND);

        return line;
    }
}
