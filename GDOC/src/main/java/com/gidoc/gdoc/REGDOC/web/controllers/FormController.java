package com.gidoc.gdoc.REGDOC.web.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.event.ActionEvent;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import java.time.LocalDate;
import java.util.Optional;

import com.gidoc.gdoc.REGDOC.domain.dto.RegistroDTO;
import com.gidoc.gdoc.REGDOC.domain.services.EscuelaService;
import com.gidoc.gdoc.REGDOC.domain.services.DocenteService;
import com.gidoc.gdoc.REGDOC.domain.services.RegistroService;

// Ajusta imports de tus entidades si necesitas usarlas directamente
import com.gidoc.gdoc.GDYBD.domain.entities.Escuela;
import com.gidoc.gdoc.GDYBD.domain.entities.Docente;

@Component
public class FormController {

    @Autowired
    private EscuelaService escuelaService;

    // Inyectamos explícitamente el bean REGDOC con @Qualifier para evitar ambigüedad
    @Autowired
    @Qualifier("regdocDocenteService")
    private DocenteService docenteService;

    @Autowired
    private RegistroService registroService;

    // FXML controls (nombres acordes al formulario de la imagen)
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

    @FXML
    public void initialize() {
        // llenar combo de meses
        cbMes.getItems().addAll("enero","febrero","marzo","abril","mayo","junio",
                "julio","agosto","septiembre","octubre","noviembre","diciembre");

        // listeners básicos
        txtCInfra.setOnKeyReleased(this::onCInfraTyped);

        // Búsqueda de NIP: onKeyReleased (con umbral), Enter y al perder foco
        txtNip.setOnKeyReleased(this::onNipTyped);
        txtNip.setOnAction(this::onNipEnter); // cuando presionan Enter en el campo
        txtNip.focusedProperty().addListener((obs, oldV, newV) -> {
            if (!newV) { // perdió foco
                buscarNipAhora();
            }
        });

        dpDesde.valueProperty().addListener((obs, oldV, newV) -> calcularDias());
        dpHasta.valueProperty().addListener((obs, oldV, newV) -> calcularDias());

        btnGuardar.setOnAction(evt -> guardarRegistro());
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
        if (nip == null || nip.isBlank()) {
            return;
        }

        Optional<Docente> opt = docenteService.findByNip(nip.trim());
        if (opt.isPresent()) {
            Docente d = opt.get();
            System.out.println("[DEBUG] docente encontrado: idpersonaA='" + d.getIdpersonaA() + "' depersona='" + d.getDepersona() + "'");
            // Si quieres mostrar sólo el número en otro campo, extrae sólo-dígitos aquí:
            // String onlyDigits = nip.replaceAll("\\D+", "");
            txtNombre.setText(d.getDepersona());
        } else {
            System.out.println("[DEBUG] docente NO encontrado para nip: '" + nip + "'");
            // opcional: txtNombre.clear();
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

    private void guardarRegistro() {
        RegistroDTO dto = RegistroDTO.builder()
                .cInfra(txtCInfra.getText())
                .nip(txtNip.getText())
                .nombreDocente(txtNombre.getText())
                .centroEducativo(txtCentro.getText())
                .municipio(txtMunicipio.getText())
                .distrito(txtDistrito.getText())
                .horas(parseInteger(txtHoras.getText()))
                .desde(dpDesde.getValue())
                .hasta(dpHasta.getValue())
                .patologia(txtPatologia.getText())
                .observaciones(taObservaciones.getText())
                .mes(cbMes.getValue() == null ? "" : cbMes.getValue())
                .build();

        registroService.save(dto);

        Alert a = new Alert(Alert.AlertType.INFORMATION, "Registro guardado en mes: " + dto.getMes(), ButtonType.OK);
        a.showAndWait();
        limpiarFormulario();
    }

    private Integer parseInteger(String s) {
        try { return (s == null || s.isBlank()) ? 0 : Integer.parseInt(s.trim()); }
        catch (NumberFormatException ex) { return 0; }
    }

    private void limpiarFormulario() {
        txtCInfra.clear(); txtNip.clear(); txtNombre.clear(); txtCentro.clear();
        txtMunicipio.clear(); txtDistrito.clear(); txtDias.clear(); txtHoras.clear();
        dpDesde.setValue(null); dpHasta.setValue(null); txtPatologia.clear(); taObservaciones.clear();
        cbMes.getSelectionModel().clearSelection();
    }
}
