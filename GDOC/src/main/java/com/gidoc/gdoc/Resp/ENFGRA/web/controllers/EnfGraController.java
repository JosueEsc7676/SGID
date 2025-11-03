package com.gidoc.gdoc.Resp.ENFGRA.web.controllers;

import com.gidoc.gdoc.Resp.ENFGRA.domain.entities.EnfGra;
import com.gidoc.gdoc.Resp.ENFGRA.domain.services.ImportEnfGraService;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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
    @FXML private TextField txtDias;
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
    @FXML private Button btnVolverInicio;
    @FXML private Button btnVolverFormulario;



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
            long dias = ChronoUnit.DAYS.between(desde, hasta) + 1;
            txtDias.setText(String.valueOf(dias));
            txtHora.clear(); // horas se escriben manualmente
        } else {
            txtDias.clear();
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
            registro.setDias(txtDias.getText()); // ✅ guarda los días calculados
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

    @FXML
    private void abrirVistaRegistros() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/EnfGraRegistros.fxml"));
            Parent vista = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Registros Guardados");
            stage.setScene(new Scene(vista));
            stage.show();
        } catch (IOException e) {
            log.error("No se pudo abrir la vista de registros", e);
        }
    }

    @FXML
    private void volverInicio() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/Home.fxml"));
            Parent vista = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Inicio");
            stage.setScene(new Scene(vista));
            stage.show();

            // Opcional: cerrar la ventana actual
            Stage actual = (Stage) btnVolverInicio.getScene().getWindow();
            actual.close();
        } catch (IOException e) {
            log.error("No se pudo regresar al inicio", e);
        }
    }
    @FXML
    private void abrirFormularioEnfGra() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/EnfGra.fxml"));
            Parent vista = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Formulario Enfermedades Graves");
            stage.setScene(new Scene(vista));
            stage.show();

            // Cierra la ventana actual
            Stage actual = (Stage) btnVolverFormulario.getScene().getWindow();
            actual.close();
        } catch (IOException e) {
            log.error("No se pudo abrir el formulario de Enfermedades Graves", e);
        }
    }
}
