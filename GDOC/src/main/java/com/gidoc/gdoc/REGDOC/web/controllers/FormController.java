package com.gidoc.gdoc.REGDOC.web.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.event.ActionEvent;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.time.LocalDate;
import java.util.Optional;
import java.util.prefs.Preferences;

import com.gidoc.gdoc.REGDOC.domain.dto.RegistroDTO;
import com.gidoc.gdoc.REGDOC.domain.services.EscuelaService;
import com.gidoc.gdoc.REGDOC.domain.services.DocenteService;
import com.gidoc.gdoc.REGDOC.domain.services.RegistroService;

// Ajusta imports de tus entidades si necesitas usarlas directamente
import com.gidoc.gdoc.GDYBD.domain.entities.Escuela;
import com.gidoc.gdoc.GDYBD.domain.entities.Docente;

@Component
public class FormController {

    private static final String PREF_KEY_DEFAULT_MONTH = "regdoc.defaultMonth";

    @Autowired
    private EscuelaService escuelaService;

    @Autowired
    @Qualifier("regdocDocenteService")
    private DocenteService docenteService;

    @Autowired
    private RegistroService registroService;

    @Autowired
    private ApplicationContext appContext;

    // FXML controls
    @FXML private TextField txtCInfra;
    @FXML private TextField txtNip;
    @FXML private TextField txtNombre;
    @FXML private TextField txtCentro;
    @FXML private TextField txtMunicipio;
    @FXML private TextField txtDistrito;
    @FXML private TextField txtDias;
    @FXML private TextField txtHoras;
    @FXML private DatePicker dpDesde;
    @FXML private DatePicker dpHasta;
    @FXML private TextField txtPatologia;
    @FXML private TextArea taObservaciones;
    @FXML private ComboBox<String> cbMes;
    @FXML private Button btnGuardar;
    @FXML private Button btnViewRecords;
// FormController.java

    private String hiddenDoc4;
    private String hiddenDoc5;
    private String hiddenPens;

    private final Preferences prefs = Preferences.userNodeForPackage(FormController.class);

    @FXML
    public void initialize() {
        // llenar combo de meses (minúsculas para consistencia)
        cbMes.getItems().addAll(
                "enero","febrero","marzo","abril","mayo","junio",
                "julio","agosto","septiembre","octubre","noviembre","diciembre"
        );

        // cargar mes por defecto desde preferences (si existe)
        String defaultMonth = prefs.get(PREF_KEY_DEFAULT_MONTH, "");
        if (!defaultMonth.isBlank()) {
            cbMes.getSelectionModel().select(defaultMonth.toLowerCase());
        }

        // listeners
        txtCInfra.setOnKeyReleased(this::onCInfraTyped);

        txtNip.setOnKeyReleased(this::onNipTyped);
        txtNip.setOnAction(this::onNipEnter);
        txtNip.focusedProperty().addListener((obs, oldV, newV) -> {
            if (!newV) buscarNipAhora();
        });

        dpDesde.valueProperty().addListener((obs, oldV, newV) -> calcularDias());
        dpHasta.valueProperty().addListener((obs, oldV, newV) -> calcularDias());

        // cuando el usuario cambie el mes en el ComboBox lo guardamos como mes por defecto
        cbMes.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isBlank()) {
                saveDefaultMonth(newVal.toLowerCase());
            }
        });
        // Ajuste de tamaño
        btnViewRecords.setPrefWidth(100);
        btnViewRecords.setPrefHeight(60);
        btnGuardar.setOnAction(evt -> guardarRegistro());
        btnViewRecords.setOnAction(evt -> abrirVistaRegistros());
//        Platform.runLater(() -> {
//            Node rootNode = txtCInfra.getScene().getRoot();
//            if (rootNode instanceof Region) {
//                ((Region) rootNode).setPrefWidth(1900);
//                ((Region) rootNode).setPrefHeight(1900);
//            }
//        });
        // Tamaños desde Java (esto funciona cuando el layout ya está asociado)
//        btnGuardar.setPrefWidth(110);
//        btnGuardar.setPrefHeight(38);
//        btnGuardar.setMinWidth(80);
//        btnGuardar.setMaxWidth(160);
//
//        btnViewRecords.setPrefWidth(210);
//        btnViewRecords.setPrefHeight(38);
//        btnViewRecords.setMinWidth(100);
//        btnViewRecords.setMaxWidth(260);
//
//        // opcional: si quieres forzar que no se expanda con el GridPane:
//        btnGuardar.setMaxWidth(Region.USE_COMPUTED_SIZE);
//        btnViewRecords.setMaxWidth(Region.USE_COMPUTED_SIZE);
    }

    private void onCInfraTyped(KeyEvent e) {
        String cInfra = txtCInfra.getText();
        if (cInfra == null || cInfra.isBlank()) return;
        Optional<Escuela> opt = escuelaService.findByCInfra(cInfra.trim());
        opt.ifPresent(esc -> {
            txtCentro.setText(esc.getNombre());
            txtMunicipio.setText(esc.getMunicipio());
            txtDistrito.setText(esc.getDistrito());
        });
    }

    private void onNipTyped(KeyEvent e) {
        String current = txtNip.getText();
        if (current != null && current.trim().length() >= 3) {
            buscarNipAhora();
        } else {
            if (current == null || current.trim().isEmpty()) {
                txtNombre.clear();
            }
        }
    }

    private void onNipEnter(ActionEvent evt) {
        buscarNipAhora();
    }

    private void buscarNipAhora() {
        String nip = txtNip.getText();
        System.out.println("[DEBUG] buscarNipAhora input: '" + nip + "'");
        if (nip == null || nip.isBlank()) return;

        Optional<Docente> opt = docenteService.findByNip(nip.trim());
        if (opt.isPresent()) {
            Docente d = opt.get();
            txtNombre.setText(d.getDepersona());
            // 👇 Guardamos ocultamente los valores extra
            hiddenDoc4 = d.getDoc4();
            hiddenDoc5 = d.getDoc5();
            hiddenPens = d.getPens();

        } else {
            System.out.println("[DEBUG] docente NO encontrado para nip: '" + nip + "'");
            hiddenDoc4 = null;
            hiddenDoc5 = null;
            hiddenPens = null;
        }
    }

    private void calcularDias() {
        LocalDate desde = dpDesde.getValue();
        LocalDate hasta = dpHasta.getValue();
        if (desde != null && hasta != null) {
            long diff = java.time.temporal.ChronoUnit.DAYS.between(desde, hasta) + 1;
            txtDias.setText(String.valueOf(Math.max(0, diff)));
        } else {
            txtDias.setText("");
        }
    }

    // Guarda el mes en Preferences
    private void saveDefaultMonth(String monthLower) {
        if (monthLower == null) return;
        prefs.put(PREF_KEY_DEFAULT_MONTH, monthLower.toLowerCase());
        System.out.println("[DEBUG] mes por defecto guardado: " + monthLower);
    }

    // Recupera el mes por defecto (puede ser vacío)
    private String getDefaultMonth() {
        return prefs.get(PREF_KEY_DEFAULT_MONTH, "");
    }

    // abre la vista de registros (igual que ya tenías); opcional: pasar mes activo al controlador
    private void abrirVistaRegistros() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/records_view.fxml"));
            loader.setControllerFactory(appContext::getBean);
            Parent root = loader.load();

            // pasar mes activo al nuevo controlador si quieres que abra ya filtrado
            Object controller = loader.getController();
            if (controller instanceof com.gidoc.gdoc.REGDOC.web.controllers.RecordsController) {
                com.gidoc.gdoc.REGDOC.web.controllers.RecordsController rc =
                        (com.gidoc.gdoc.REGDOC.web.controllers.RecordsController) controller;
                String mesActivo = (cbMes.getValue() == null || cbMes.getValue().isBlank())
                        ? getDefaultMonth()
                        : cbMes.getValue().toLowerCase();
                if (mesActivo != null && !mesActivo.isBlank()) rc.load(mesActivo.toLowerCase());
            }

            Stage stage = new Stage();
            stage.setTitle("Registros por mes");
            if (btnViewRecords != null && btnViewRecords.getScene() != null) {
                stage.initOwner(btnViewRecords.getScene().getWindow());
                stage.initModality(Modality.WINDOW_MODAL);
            } else {
                stage.initModality(Modality.APPLICATION_MODAL);
            }
            stage.setScene(new Scene(root));
            stage.setResizable(true);
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error al abrir la vista de registros:\n" + ex.getMessage(), ButtonType.OK).showAndWait();
        }
    }

    private void guardarRegistro() {
        // Validación de campos obligatorios
        String cInfra = txtCInfra.getText();
        String nip = txtNip.getText();
        LocalDate desde = dpDesde.getValue();
        LocalDate hasta = dpHasta.getValue();

        if (cInfra == null || cInfra.isBlank()) {
            mostrarAlerta("Campo requerido", "Debe ingresar el código CInfra de la escuela.");
            txtCInfra.requestFocus();
            return;
        }

        if (nip == null || nip.isBlank()) {
            mostrarAlerta("Campo requerido", "Debe ingresar el NIP del docente.");
            txtNip.requestFocus();
            return;
        }

        if (desde == null) {
            mostrarAlerta("Campo requerido", "Debe seleccionar la fecha 'Desde'.");
            dpDesde.requestFocus();
            return;
        }

        if (hasta == null) {
            mostrarAlerta("Campo requerido", "Debe seleccionar la fecha 'Hasta'.");
            dpHasta.requestFocus();
            return;
        }

        if (hasta.isBefore(desde)) {
            mostrarAlerta("Fechas inválidas", "La fecha 'Hasta' no puede ser anterior a la fecha 'Desde'.");
            dpHasta.requestFocus();
            return;
        }

        String mes = cbMes.getValue();
        if (mes == null || mes.isBlank()) {
            mes = getDefaultMonth();
            if (mes == null || mes.isBlank()) {
                mostrarAlerta("Seleccione un mes", "Debe seleccionar o establecer un mes predeterminado.");
                cbMes.requestFocus();
                return;
            }
        }

        RegistroDTO dto = RegistroDTO.builder()
                .cInfra(cInfra.trim())
                .nip(nip.trim())
                .nombreDocente(txtNombre.getText())
                .centroEducativo(txtCentro.getText())
                .municipio(txtMunicipio.getText())
                .distrito(txtDistrito.getText())
                .horas(parseInteger(txtHoras.getText()))
                .desde(desde)
                .hasta(hasta)
                .patologia(txtPatologia.getText())
                .observaciones(taObservaciones.getText())
                .mes(mes.toLowerCase())
                // 👇 Agregamos los datos ocultos
                .doc4(hiddenDoc4)
                .doc5(hiddenDoc5)
                .pens(hiddenPens)
                .build();

        try {
            registroService.save(dto);
            mostrarAlertaInfo("Éxito", "Registro guardado correctamente para el mes de " + dto.getMes() + ".");
            limpiarFormulario(false); // <-- false indica que NO se limpian las fechas
        } catch (Exception e) {
            mostrarAlerta("Error al guardar", "No se pudo guardar el registro:\n" + e.getMessage());
        }
    }

    // Alerta de advertencia (error o campo faltante)
    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
    private Integer parseInteger(String s) {
        try {
            return (s == null || s.isBlank()) ? 0 : Integer.parseInt(s.trim());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    // Alerta informativa (éxito)
    private void mostrarAlertaInfo(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    /**
     * Limpia el formulario.
     * @param limpiarFechas si es true también borra dpDesde y dpHasta; si es false mantiene las fechas.
     */
    private void limpiarFormulario(boolean limpiarFechas) {
        txtCInfra.clear();
        txtNip.clear();
        txtNombre.clear();
        txtCentro.clear();
        txtMunicipio.clear();
        txtDistrito.clear();
        txtDias.clear();
        txtHoras.clear();
        txtPatologia.clear();
        taObservaciones.clear();

        if (limpiarFechas) {
            dpDesde.setValue(null);
            dpHasta.setValue(null);
        }

        // No limpiamos cbMes para mantener la selección actual
    }

    /** Versión sin argumentos para compatibilidad: mantiene las fechas por defecto. */
    private void limpiarFormulario() {
        limpiarFormulario(false);
    }

}
