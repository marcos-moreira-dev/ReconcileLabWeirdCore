package com.marcosmoreiradev.reconcilelab.ui.help;

import com.marcosmoreiradev.reconcilelab.app.AppMetadata;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Ventana de ayuda del producto.
 *
 * <p>La portada pertenece a la edición Java, mientras las páginas de dominio
 * continúan reutilizando el manual canónico compartido con la edición XP.</p>
 */
public final class HelpWindow {

    private HelpWindow() {
    }

    public static void show(Window owner) {
        URL help =
                HelpWindow.class.getResource(
                        "/java-help/index.html");

        if (help == null) {
            help =
                    HelpWindow.class.getResource(
                            "/help/index.html");
        }

        if (help == null) {
            throw new IllegalStateException(
                    "No se encontró la ayuda integrada.");
        }

        WebView webView =
                new WebView();

        URL javaHome =
                HelpWindow.class.getResource(
                        "/java-help/index.html");

        webView.getEngine()
                .locationProperty()
                .addListener(
                        (obs, oldLocation, newLocation) -> {
                            if (javaHome == null
                                    || newLocation == null) {
                                return;
                            }

                            if (newLocation.endsWith(
                                    "/help/index.html")) {

                                webView.getEngine()
                                        .load(
                                                javaHome.toExternalForm());
                            }
                        });

        webView.getEngine()
                .load(help.toExternalForm());

        Stage stage =
                new Stage();

        stage.initOwner(owner);
        stage.initModality(Modality.NONE);

        stage.setTitle(
                "Ayuda de "
                        + AppMetadata.PRODUCT_NAME);

        stage.setScene(
                new Scene(
                        webView,
                        900,
                        700));

        stage.setMinWidth(720);
        stage.setMinHeight(520);

        try (InputStream icon =
                     HelpWindow.class.getResourceAsStream(
                             "/branding/reconcilelab-icon-64.png")) {

            if (icon != null) {
                stage.getIcons()
                        .add(new Image(icon));
            }
        } catch (IOException ignored) {
            // La ayuda sigue siendo utilizable aunque falle sólo su icono.
        }

        stage.show();
    }
}
