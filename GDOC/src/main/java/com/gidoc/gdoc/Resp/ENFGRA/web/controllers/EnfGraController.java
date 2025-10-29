package com.gidoc.gdoc.Resp.ENFGRA.web.controllers;

import com.gidoc.gdoc.Resp.ENFGRA.domain.entities.EnfGra;
import com.gidoc.gdoc.Resp.ENFGRA.domain.services.ImportEnfGraService;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Controller
public class EnfGraController {

    private final ImportEnfGraService importService;

    @Autowired
    public EnfGraController(ImportEnfGraService importService) {
        this.importService = importService;
    }

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
    @FXML private TableView<EnfGra> tblEnfgra;
    @FXML private TableColumn<EnfGra, String> colInfra;
    @FXML private TableColumn<EnfGra, String> colNip;
    @FXML private TableColumn<EnfGra, String> colNombre;
    @FXML private TableColumn<EnfGra, String> colLugar;
    @FXML private TableColumn<EnfGra, String> colDias;
    @FXML private TableColumn<EnfGra, String> colHora;
    @FXML private TableColumn<EnfGra, LocalDate> colDesde;
    @FXML private TableColumn<EnfGra, LocalDate> colHasta;
    @FXML private TableColumn<EnfGra, String> colObs;
    @FXML private TableColumn<EnfGra, LocalDate> colFechaRecibido;

    @FXML
    public void initialize() {
        colInfra.setCellValueFactory(new PropertyValueFactory<>("cInfra"));
        colNip.setCellValueFactory(new PropertyValueFactory<>("nip"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colLugar.setCellValueFactory(new PropertyValueFactory<>("lugarTrabajo"));
        colDias.setCellValueFactory(new PropertyValueFactory<>("dias"));
        colHora.setCellValueFactory(new PropertyValueFactory<>("hora"));
        colDesde.setCellValueFactory(new PropertyValueFactory<>("desde"));
        colHasta.setCellValueFactory(new PropertyValueFactory<>("hasta"));
        colObs.setCellValueFactory(new PropertyValueFactory<>("observaciones"));
        colFechaRecibido.setCellValueFactory(new PropertyValueFactory<>("fechaRecibido"));

        refrescarTabla();

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
            txtHora.setText(String.valueOf(dias)); // Puedes cambiar esto si "hora" no representa días
        }
    }

    @FXML
    private void guardarRegistro(ActionEvent event) {
        try {
            EnfGra registro = new EnfGra();
            registro.setCInfra(txtCInfra.getText());
            registro.setLugarTrabajo(txtLugarTrabajo.getText());
            registro.setNip(txtNip.getText());
            registro.setNombre(txtNombre.getText());
            registro.setHora(txtHora.getText());
            registro.setDesde(dpDesde.getValue());
            registro.setHasta(dpHasta.getValue());
            registro.setObservaciones(txtObservaciones.getText());
            registro.setFechaRecibido(dpFechaRecibido.getValue());

            importService.guardarRegistro(registro);
            mostrarAlerta("Éxito", "Registro guardado correctamente.", Alert.AlertType.INFORMATION);
            refrescarTabla();
        } catch (Exception e) {
            log.error("Error al guardar registro", e);
            mostrarAlerta("Error", "No se pudo guardar el registro: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void refrescarTabla() {
        List<EnfGra> registros = importService.obtenerTodos();
        tblEnfgra.setItems(FXCollections.observableArrayList(registros));
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
