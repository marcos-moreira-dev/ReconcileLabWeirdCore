package com.marcosmoreiradev.reconcilelab.ui.dialog;

import com.marcosmoreiradev.reconcilelab.domain.ProblemInstance;
import com.marcosmoreiradev.reconcilelab.domain.SearchObjective;
import com.marcosmoreiradev.reconcilelab.io.CaseFile;
import com.marcosmoreiradev.reconcilelab.io.MoneyText;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.util.ArrayList;
import java.util.List;

/**
 * Crea un caso de conciliación sin obligar al usuario a editar un `.case`.
 *
 * <p>La validación es exactamente la misma que se aplica al cargar/guardar
 * archivos. El diálogo sólo se cierra con Aceptar cuando puede producir un
 * {@link ProblemInstance} válido.</p>
 */
public final class NewCaseDialog extends Dialog<ProblemInstance> {

    private final TextField name =
            new TextField("Caso de conciliación personalizado");

    private final TextField target =
            new TextField("0.00");

    private final TextArea values =
            new TextArea();

    private final ChoiceBox<ObjectiveChoice> objective =
            new ChoiceBox<>();

    private ProblemInstance validatedProblem;

    public NewCaseDialog(Window owner) {
        initOwner(owner);
        setTitle("Nuevo caso de conciliación");
        setHeaderText("Define el monto total y los movimientos candidatos.");

        getDialogPane().getButtonTypes().addAll(
                ButtonType.OK,
                ButtonType.CANCEL);

        name.setPromptText("Nombre del caso");
        target.setPromptText("Ejemplo: 428.50");

        values.setPromptText(
                "32.00\n75.50\n120.00");
        values.setPrefRowCount(10);
        values.setWrapText(false);

        objective.getItems().setAll(
                new ObjectiveChoice(
                        SearchObjective.FIRST,
                        "Detenerse en la primera combinación compatible"),
                new ObjectiveChoice(
                        SearchObjective.ALL,
                        "Encontrar todas las combinaciones compatibles"),
                new ObjectiveChoice(
                        SearchObjective.COUNT,
                        "Contar las combinaciones compatibles"));

        objective.getSelectionModel().selectFirst();

        Label valuesHelp = new Label(
                "Ingresa un monto por línea. Se acepta punto o coma decimal.");
        valuesHelp.setWrapText(true);
        valuesHelp.getStyleClass().add("field-help");

        Label note = new Label(
                "Una combinación compatible explica el monto numéricamente. "
                        + "Por sí sola no demuestra que esos movimientos formaron "
                        + "históricamente el depósito o abono original.");
        note.setWrapText(true);
        note.getStyleClass().add("domain-note");

        GridPane fields = new GridPane();
        fields.setHgap(10);
        fields.setVgap(8);

        fields.add(new Label("Nombre del caso:"), 0, 0);
        fields.add(name, 1, 0);

        fields.add(new Label("Monto a conciliar:"), 0, 1);
        fields.add(target, 1, 1);

        fields.add(new Label("Objetivo de la búsqueda:"), 0, 2);
        fields.add(objective, 1, 2);

        GridPane.setHgrow(name, Priority.ALWAYS);
        GridPane.setHgrow(target, Priority.ALWAYS);
        GridPane.setHgrow(objective, Priority.ALWAYS);

        VBox movements = new VBox(
                5,
                new Label("Movimientos candidatos"),
                valuesHelp,
                values);

        VBox.setVgrow(values, Priority.ALWAYS);

        VBox content = new VBox(
                12,
                fields,
                movements,
                note);

        content.setPadding(new Insets(4));
        content.setPrefWidth(540);
        content.setPrefHeight(500);

        getDialogPane().setContent(content);

        var css =
                NewCaseDialog.class.getResource(
                        "/css/reconcilelab.css");

        if (css != null) {
            getDialogPane()
                    .getStylesheets()
                    .add(css.toExternalForm());
        }

        Button ok =
                (Button) getDialogPane()
                        .lookupButton(ButtonType.OK);

        ok.addEventFilter(
                ActionEvent.ACTION,
                event -> {
                    try {
                        validatedProblem =
                                buildProblem();
                    } catch (IllegalArgumentException ex) {
                        event.consume();
                        showValidationError(ex.getMessage());
                    }
                });

        setResultConverter(button ->
                button == ButtonType.OK
                        ? validatedProblem
                        : null);
    }

    private ProblemInstance buildProblem() {
        String caseName =
                name.getText() == null
                        ? ""
                        : name.getText().trim();

        if (caseName.isBlank()) {
            caseName =
                    "Caso de conciliación personalizado";
        }

        int targetCents =
                MoneyText.parseCents(
                        target.getText());

        List<Integer> candidateValues =
                parseCandidateValues();

        ObjectiveChoice selected =
                objective.getValue();

        SearchObjective searchObjective =
                selected == null
                        ? SearchObjective.FIRST
                        : selected.objective();

        ProblemInstance problem =
                new ProblemInstance(
                        caseName,
                        "Creado manualmente en ReconcileLab.",
                        candidateValues,
                        targetCents,
                        searchObjective);

        CaseFile.validate(problem);
        return problem;
    }

    private List<Integer> parseCandidateValues() {
        List<Integer> result =
                new ArrayList<>();

        String raw =
                values.getText() == null
                        ? ""
                        : values.getText();

        String[] lines =
                raw.split("\\R", -1);

        for (int i = 0; i < lines.length; i++) {
            String line =
                    lines[i].trim();

            if (line.isBlank()) {
                continue;
            }

            try {
                result.add(
                        MoneyText.parseCents(line));
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException(
                        "El movimiento candidato de la línea "
                                + (i + 1)
                                + " no es válido. "
                                + ex.getMessage(),
                        ex);
            }
        }

        return List.copyOf(result);
    }

    private void showValidationError(String message) {
        Alert alert =
                new Alert(
                        Alert.AlertType.WARNING);

        alert.initOwner(
                getOwner());

        alert.setTitle(
                "No se puede crear el caso");

        alert.setHeaderText(
                "Revisa los datos del caso.");

        alert.setContentText(
                message == null || message.isBlank()
                        ? "Los datos ingresados no forman un caso válido."
                        : message);

        alert.showAndWait();
    }

    private record ObjectiveChoice(
            SearchObjective objective,
            String label) {

        @Override
        public String toString() {
            return label;
        }
    }
}
