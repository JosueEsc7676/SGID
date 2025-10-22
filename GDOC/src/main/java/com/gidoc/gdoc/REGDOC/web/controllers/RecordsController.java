package com.gidoc.gdoc.REGDOC.web.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar.ButtonData;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.prefs.Preferences;

import com.gidoc.gdoc.REGDOC.domain.entities.RegistroMes;
import com.gidoc.gdoc.REGDOC.domain.dto.RegistroDTO;
import com.gidoc.gdoc.REGDOC.domain.services.RegistroService;
import com.gidoc.gdoc.REGDOC.domain.services.DocenteService;
import com.gidoc.gdoc.REGDOC.domain.services.EscuelaService;
import com.gidoc.gdoc.GDYBD.domain.entities.Docente;
import com.gidoc.gdoc.GDYBD.domain.entities.Escuela;

@Component
public class RecordsController {

    private static final String PREF_KEY_DEFAULT_MONTH = "regdoc.defaultMonth";

    @Autowired
    private RegistroService registroService;

    // Para autocompletar NOMBRE desde NIP
    @Autowired
    @Qualifier("regdocDocenteService")
    private DocenteService docenteService;

    // Para autocompletar CENTRO/MUNICIPIO/DISTRITO desde C_INFRA en el diálogo
    @Autowired
    private EscuelaService escuelaService;

    @FXML private ComboBox<String> cbMesFilter;
    @FXML private Button btnLoad;
    @FXML private Button btnRefresh;
    @FXML private Label lblStatus;

    @FXML private TableView<RegistroMes> tableRecords;
    @FXML private TableColumn<RegistroMes, String> colCInfra;
    @FXML private TableColumn<RegistroMes, String> colNip;
    @FXML private TableColumn<RegistroMes, String> colNombre;
    @FXML private TableColumn<RegistroMes, String> colCentro;
    @FXML private TableColumn<RegistroMes, String> colMunicipio;
    @FXML private TableColumn<RegistroMes, String> colDistrito;
    @FXML private TableColumn<RegistroMes, Integer> colDias;
    @FXML private TableColumn<RegistroMes, Integer> colHoras;
    @FXML private TableColumn<RegistroMes, LocalDate> colDesde;
    @FXML private TableColumn<RegistroMes, LocalDate> colHasta;
    @FXML private TableColumn<RegistroMes, String> colPatologia;
    @FXML private TableColumn<RegistroMes, String> colObservaciones;

    @FXML private Button btnEdit;
    @FXML private Button btnDelete;
    @FXML private Button btnClose;

    private final Preferences prefs = Preferences.userNodeForPackage(RecordsController.class);

    @FXML
    public void initialize() {
        cbMesFilter.getItems().addAll(
                "enero","febrero","marzo","abril","mayo","junio",
                "julio","agosto","septiembre","octubre","noviembre","diciembre"
        );

        String defaultMonth = prefs.get(PREF_KEY_DEFAULT_MONTH, "");
        if (!defaultMonth.isBlank()) {
            cbMesFilter.getSelectionModel().select(defaultMonth.toLowerCase());
            lblStatus.setText("Mes activo: " + defaultMonth);
        }

        // columnas (prop names deben coincidir con getters en RegistroMes)
        colCInfra.setCellValueFactory(new PropertyValueFactory<>("cInfra"));
        colNip.setCellValueFactory(new PropertyValueFactory<>("nip"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreDocente"));
        colCentro.setCellValueFactory(new PropertyValueFactory<>("centroEducativo"));
        colMunicipio.setCellValueFactory(new PropertyValueFactory<>("municipio"));
        colDistrito.setCellValueFactory(new PropertyValueFactory<>("distrito"));
        colDias.setCellValueFactory(new PropertyValueFactory<>("dias"));
        colHoras.setCellValueFactory(new PropertyValueFactory<>("horas"));
        colDesde.setCellValueFactory(new PropertyValueFactory<>("desde"));
        colHasta.setCellValueFactory(new PropertyValueFactory<>("hasta"));
        colPatologia.setCellValueFactory(new PropertyValueFactory<>("patologia"));
        colObservaciones.setCellValueFactory(new PropertyValueFactory<>("observaciones"));

        btnLoad.setOnAction(evt -> loadForSelectedMonth());
        btnRefresh.setOnAction(evt -> refresh());
        btnEdit.setOnAction(evt -> editSelected());
        btnDelete.setOnAction(evt -> deleteSelected());
        btnClose.setOnAction(evt -> closeWindow());

        // doble click para editar rápidamente
        tableRecords.setRowFactory(tv -> {
            TableRow<RegistroMes> row = new TableRow<>();
            row.setOnMouseClicked(ev -> {
                if (ev.getClickCount() == 2 && !row.isEmpty()) {
                    editRegistro(row.getItem());
                }
            });
            return row;
        });

        // si hay mes por defecto, cargarlo
        if (!defaultMonth.isBlank()) {
            load(defaultMonth.toLowerCase());
        }
    }

    private void loadForSelectedMonth() {
        String mes = cbMesFilter.getValue();
        if (mes == null || mes.isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Seleccione un mes primero.");
            return;
        }
        load(mes.toLowerCase());
    }

    public void load(String mesLower) {
        List<RegistroMes> list = registroService.findByMes(mesLower);
        tableRecords.getItems().setAll(list);
        lblStatus.setText("Registros: " + list.size() + " — mes: " + mesLower);
    }

    private void refresh() {
        String mes = cbMesFilter.getValue();
        if (mes == null || mes.isBlank()) {
            String defaultMonth = prefs.get(PREF_KEY_DEFAULT_MONTH, "");
            if (!defaultMonth.isBlank()) {
                load(defaultMonth.toLowerCase());
                return;
            }
            showAlert(Alert.AlertType.INFORMATION, "No hay mes seleccionado ni predeterminado.");
            return;
        }
        load(mes.toLowerCase());
    }

    private void editSelected() {
        RegistroMes sel = tableRecords.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showAlert(Alert.AlertType.WARNING, "Seleccione un registro para editar.");
            return;
        }
        editRegistro(sel);
    }

    /**
     * Abre dialogo para editar un registro.
     * Añade listeners para autocompletar NOMBRE (desde NIP) y ESCUELA (desde C_INFRA) dentro del diálogo.
     */
    private void editRegistro(RegistroMes r) {
        Dialog<RegistroDTO> dialog = new Dialog<>();
        dialog.setTitle("Editar registro - NIP: " + r.getNip());
        ButtonType btnOk = new ButtonType("Guardar", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnOk, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(8); grid.setVgap(8);
        grid.setPadding(new Insets(10));

        TextField tfCInfra = new TextField(r.getCInfra());
        TextField tfNip = new TextField(r.getNip());
        TextField tfNombre = new TextField(r.getNombreDocente());
        TextField tfCentro = new TextField(r.getCentroEducativo());
        TextField tfMunicipio = new TextField(r.getMunicipio());
        TextField tfDistrito = new TextField(r.getDistrito());
        DatePicker dpDesde = new DatePicker(r.getDesde());
        DatePicker dpHasta = new DatePicker(r.getHasta());
        TextField tfHoras = new TextField(r.getHoras() == null ? "" : r.getHoras().toString());
        TextField tfPat = new TextField(r.getPatologia());
        TextArea taObs = new TextArea(r.getObservaciones());
        ComboBox<String> cbMesLocal = new ComboBox<>();
        cbMesLocal.getItems().addAll(
                "enero","febrero","marzo","abril","mayo","junio",
                "julio","agosto","septiembre","octubre","noviembre","diciembre"
        );
        cbMesLocal.getSelectionModel().select(r.getMes());

        grid.add(new Label("C INFRA"), 0, 0); grid.add(tfCInfra, 1, 0);
        grid.add(new Label("NIP"), 0, 1); grid.add(tfNip, 1, 1);
        grid.add(new Label("NOMBRE"), 0, 2); grid.add(tfNombre, 1, 2);
        grid.add(new Label("CENTRO"), 0, 3); grid.add(tfCentro, 1, 3);
        grid.add(new Label("MUNICIPIO"), 0, 4); grid.add(tfMunicipio, 1, 4);
        grid.add(new Label("DISTRITO"), 0, 5); grid.add(tfDistrito, 1, 5);
        grid.add(new Label("DESDE"), 0, 6); grid.add(dpDesde, 1, 6);
        grid.add(new Label("HASTA"), 0, 7); grid.add(dpHasta, 1, 7);
        grid.add(new Label("HORAS"), 0, 8); grid.add(tfHoras, 1, 8);
        grid.add(new Label("PATOLOGIA"), 0, 9); grid.add(tfPat, 1, 9);
        grid.add(new Label("OBSERVACIONES"), 0, 10); grid.add(taObs, 1, 10);
        grid.add(new Label("MES"), 0, 11); grid.add(cbMesLocal, 1, 11);

        dialog.getDialogPane().setContent(grid);

        // --- LISTENERS para autocompletar NIP -> NOMBRE dentro del diálogo ---
        tfNip.setOnAction(evt -> buscarNipEnDialog(tfNip, tfNombre));
        tfNip.focusedProperty().addListener((obs, oldV, newV) -> { if (!newV) buscarNipEnDialog(tfNip, tfNombre); });
        tfNip.setOnKeyReleased(evt -> {
            String cur = tfNip.getText();
            if (cur != null && cur.trim().length() >= 3) buscarNipEnDialog(tfNip, tfNombre);
            else if (cur == null || cur.trim().isEmpty()) tfNombre.clear();
        });

        // --- LISTENERS para autocompletar C_INFRA -> CENTRO,MUNICIPIO,DISTRITO dentro del diálogo ---
        tfCInfra.setOnAction(evt -> buscarEscuelaEnDialog(tfCInfra, tfCentro, tfMunicipio, tfDistrito));
        tfCInfra.focusedProperty().addListener((obs, oldV, newV) -> { if (!newV) buscarEscuelaEnDialog(tfCInfra, tfCentro, tfMunicipio, tfDistrito); });
        tfCInfra.setOnKeyReleased(evt -> {
            String cur = tfCInfra.getText();
            if (cur != null && cur.trim().length() >= 2) buscarEscuelaEnDialog(tfCInfra, tfCentro, tfMunicipio, tfDistrito);
            else if (cur == null || cur.trim().isEmpty()) {
                tfCentro.clear(); tfMunicipio.clear(); tfDistrito.clear();
            }
        });

        // enable/disable OK dependiendo del mes
        Node okButton = dialog.getDialogPane().lookupButton(btnOk);
        okButton.setDisable(cbMesLocal.getSelectionModel().isEmpty());
        cbMesLocal.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> okButton.setDisable(n == null || n.isBlank()));

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnOk) {
                RegistroDTO dto = RegistroDTO.builder()
                        .cInfra(tfCInfra.getText())
                        .nip(tfNip.getText())
                        .nombreDocente(tfNombre.getText())
                        .centroEducativo(tfCentro.getText())
                        .municipio(tfMunicipio.getText())
                        .distrito(tfDistrito.getText())
                        .horas(parseInteger(tfHoras.getText()))
                        .desde(dpDesde.getValue())
                        .hasta(dpHasta.getValue())
                        .patologia(tfPat.getText())
                        .observaciones(taObs.getText())
                        .mes(cbMesLocal.getValue() == null ? "" : cbMesLocal.getValue().toLowerCase())
                        .build();
                return dto;
            }
            return null;
        });

        Optional<RegistroDTO> result = dialog.showAndWait();
        result.ifPresent(dto -> {
            registroService.update(r.getId(), dto);
            load(dto.getMes());
            showAlert(Alert.AlertType.INFORMATION, "Registro actualizado.");
        });
    }

    /**
     * Busca el docente usando el texto del TextField nipField y si lo encuentra
     * asigna el nombre en nameField.
     */
    private void buscarNipEnDialog(TextField nipField, TextField nameField) {
        String nipInput = nipField.getText();
        if (nipInput == null || nipInput.isBlank()) {
            nameField.clear();
            return;
        }

        try {
            Optional<Docente> maybe = docenteService.findByNip(nipInput.trim());
            if (maybe.isPresent()) {
                Docente d = maybe.get();
                nameField.setText(d.getDepersona());
                System.out.println("[DEBUG dialog] docente encontrado: " + d.getIdpersonaA() + " -> " + d.getDepersona());
            } else {
                System.out.println("[DEBUG dialog] docente NO encontrado para nip: '" + nipInput + "'");
                // nameField.clear(); // opcional
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Busca la escuela usando el texto del TextField cInfraField y si la encuentra
     * asigna centro/municipio/distrito en los TextFields correspondientes.
     */
    private void buscarEscuelaEnDialog(TextField cInfraField, TextField centroField, TextField municipioField, TextField distritoField) {
        String cin = cInfraField.getText();
        if (cin == null || cin.isBlank()) {
            centroField.clear(); municipioField.clear(); distritoField.clear();
            return;
        }

        try {
            Optional<Escuela> maybe = escuelaService.findByCInfra(cin.trim());
            if (maybe.isPresent()) {
                Escuela e = maybe.get();
                centroField.setText(e.getNombre());
                municipioField.setText(e.getMunicipio());
                distritoField.setText(e.getDistrito());
                System.out.println("[DEBUG dialog] escuela encontrada: cInfra=" + e.getCInfra() + " nombre=" + e.getNombre());
            } else {
                System.out.println("[DEBUG dialog] escuela NO encontrada para cInfra: '" + cin + "'");
                // centroField.clear(); municipioField.clear(); distritoField.clear();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void deleteSelected() {
        RegistroMes sel = tableRecords.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showAlert(Alert.AlertType.WARNING, "Seleccione un registro para eliminar.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar registro NIP " + sel.getNip() + " - " + sel.getNombreDocente() + "?",
                ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> r = confirm.showAndWait();
        if (r.isPresent() && r.get() == ButtonType.YES) {
            registroService.deleteById(sel.getId());
            loadForSelectedMonth();
            showAlert(Alert.AlertType.INFORMATION, "Registro eliminado.");
        }
    }

    private void closeWindow() {
        Stage st = (Stage) btnClose.getScene().getWindow();
        st.close();
    }

    private void showAlert(Alert.AlertType type, String msg) {
        Alert a = new Alert(type, msg, ButtonType.OK);
        a.showAndWait();
    }

    private Integer parseInteger(String s) {
        try { return (s == null || s.isBlank()) ? 0 : Integer.parseInt(s.trim()); }
        catch (NumberFormatException ex) { return 0; }
    }
}
