package com.gidoc.gdoc.GDYBD.web.controllers;

import com.gidoc.gdoc.GDYBD.domain.entities.Escuela;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.stereotype.Controller;

@Controller
public class EscuelaDialogController {

    @FXML private TextField txtCInfra;
    @FXML private TextField txtNumero;
    @FXML private TextField txtDistrito;
    @FXML private TextField txtNombre;
    @FXML private TextField txtMunicipio;

    private Stage dialogStage;
    private Escuela escuela;
    private boolean okClicked = false;

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    public void setEscuela(Escuela e) {
        this.escuela = e;
        if (e != null) {
            txtCInfra.setText(e.getCInfra());
            txtNumero.setText(e.getNumero());
            txtDistrito.setText(e.getDistrito());
            txtNombre.setText(e.getNombre());
            txtMunicipio.setText(e.getMunicipio());
        }
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    @FXML
    private void aceptar() {
        if (validarEntrada()) {
            escuela.setCInfra(txtCInfra.getText().trim());
            escuela.setNumero(txtNumero.getText().trim());
            escuela.setDistrito(txtDistrito.getText().trim());
            escuela.setNombre(txtNombre.getText().trim());
            escuela.setMunicipio(txtMunicipio.getText().trim());

            okClicked = true;
            dialogStage.close();
        }
    }

    @FXML
    private void cancelar() {
        dialogStage.close();
    }

    private boolean validarEntrada() {
        StringBuilder error = new StringBuilder();

        if (txtCInfra.getText() == null || txtCInfra.getText().isBlank())
            error.append("C INFRA es obligatorio.\n");
        if (txtNumero.getText() == null || txtNumero.getText().isBlank())
            error.append("Número es obligatorio.\n");
        if (txtNombre.getText() == null || txtNombre.getText().isBlank())
            error.append("Nombre es obligatorio.\n");

        if (!error.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Campos inválidos");
            alert.setHeaderText("Por favor corrija los siguientes errores:");
            alert.setContentText(error.toString());
            alert.showAndWait();
            return false;
        }
        return true;
    }
}
