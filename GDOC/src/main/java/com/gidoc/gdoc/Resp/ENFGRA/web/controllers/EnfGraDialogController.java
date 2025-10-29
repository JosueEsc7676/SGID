package com.gidoc.gdoc.Resp.ENFGRA.web.controllers;

import com.gidoc.gdoc.Resp.ENFGRA.domain.entities.EnfGra;
import com.gidoc.gdoc.Resp.ENFGRA.domain.services.ImportEnfGraService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;

@Slf4j
@Controller
public class EnfGraDialogController {

    private Stage dialogStage;
    private EnfGra enfermedadGrave;
    private boolean okClicked = false;

    @Autowired
    private ImportEnfGraService importService;

    @FXML private TextField txtCInfra;
    @FXML private TextField txtLugarTrabajo;
    @FXML private TextField txtNip;
    @FXML private TextField txtNombre;
    @FXML private TextField txtHora;
    @FXML private DatePicker dpDesde;
    @FXML private DatePicker dpHasta;
    @FXML private TextArea txtObservaciones;
    @FXML private DatePicker dpFechaRecibido;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;

    @FXML
    private void initialize() {
        txtCInfra.textProperty().addListener((obs, oldVal, newVal) -> {
            txtLugarTrabajo.setText(importService.obtenerLugarTrabajoPorInfra(newVal));
        });

        txtNip.textProperty().addListener((obs, oldVal, newVal) -> {
            txtNombre.setText(importService.obtenerNombrePorNip(newVal));
        });

        dpDesde.valueProperty().addListener((obs, oldVal, newVal) -> calcularDias());
        dpHasta.valueProperty().addListener((obs, oldVal, newVal) -> calcularDias());
    }

    private void calcularDias() {
        LocalDate desde = dpDesde.getValue();
        LocalDate hasta = dpHasta.getValue();
        if (desde != null && hasta != null && !hasta.isBefore(desde)) {
            long dias = hasta.toEpochDay() - desde.toEpochDay() + 1;
            txtHora.setText(String.valueOf(dias));
        }
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setEnfermedadGrave(EnfGra enfermedadGrave) {
        this.enfermedadGrave = enfermedadGrave;

        txtCInfra.setText(enfermedadGrave.getCInfra());
        txtLugarTrabajo.setText(enfermedadGrave.getLugarTrabajo());
        txtNip.setText(enfermedadGrave.getNip());
        txtNombre.setText(enfermedadGrave.getNombre());
        txtHora.setText(enfermedadGrave.getHora());
        dpDesde.setValue(enfermedadGrave.getDesde());
        dpHasta.setValue(enfermedadGrave.getHasta());
        txtObservaciones.setText(enfermedadGrave.getObservaciones());
        dpFechaRecibido.setValue(enfermedadGrave.getFechaRecibido());
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    @FXML
    private void handleGuardar() {
        try {
            enfermedadGrave.setCInfra(txtCInfra.getText());
            enfermedadGrave.setLugarTrabajo(txtLugarTrabajo.getText());
            enfermedadGrave.setNip(txtNip.getText());
            enfermedadGrave.setNombre(txtNombre.getText());
            enfermedadGrave.setHora(txtHora.getText());
            enfermedadGrave.setDesde(dpDesde.getValue());
            enfermedadGrave.setHasta(dpHasta.getValue());
            enfermedadGrave.setObservaciones(txtObservaciones.getText());
            enfermedadGrave.setFechaRecibido(dpFechaRecibido.getValue());

            okClicked = true;
            dialogStage.close();
        } catch (Exception e) {
            log.error("Error al guardar datos del formulario", e);
            mostrarAlerta("Error", "No se pudo guardar el registro: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleCancelar() {
        dialogStage.close();
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
