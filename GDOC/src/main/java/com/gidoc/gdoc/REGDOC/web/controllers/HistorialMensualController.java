package com.gidoc.gdoc.REGDOC.web.controllers;

import com.gidoc.gdoc.REGDOC.domain.entities.RegistroMes;
import com.gidoc.gdoc.REGDOC.domain.dto.RegistroDTO;
import com.gidoc.gdoc.REGDOC.domain.services.ExcelImportService;
import com.gidoc.gdoc.REGDOC.domain.services.HistorialMensualService;
import com.gidoc.gdoc.REGDOC.domain.services.DocenteService;
import com.gidoc.gdoc.REGDOC.domain.services.EscuelaService;
import com.gidoc.gdoc.GDYBD.domain.entities.Docente;
import com.gidoc.gdoc.GDYBD.domain.entities.Escuela;

import com.gidoc.gdoc.Usuarios.web.controllers.ApplicationManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar.ButtonData;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.prefs.Preferences;

@Component
public class HistorialMensualController {

    @Autowired
    private HistorialMensualService historialService;
    @Autowired
    private ExcelImportService excelImportService;

    @Autowired
    @Qualifier("regdocDocenteService")
    private DocenteService docenteService;
    @Autowired
    private ApplicationManager applicationManager;

    @Autowired
    private EscuelaService escuelaService;
    @FXML private Button btnTodosLosMeses; // ✅ NUEVO: Botón para ver todos los meses
    @FXML private TableColumn<RegistroMes, String> colDoc4;
    @FXML private TableColumn<RegistroMes, String> colDoc5;
    @FXML private TableColumn<RegistroMes, String> colPens;
    @FXML private Button btnVolverHome;

    @FXML private ComboBox<String> cbMesFilter;
    @FXML private TextField tfNip, tfCInfra;
    @FXML private Button btnLoad, btnRefresh, btnEdit, btnDelete, btnClose;
    @FXML private Label lblStatus;
    @FXML private TableView<RegistroMes> tableRecords;
    @FXML
    private Button btnImportar;
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
    @FXML private Button btnAgregarAReporte;
    @FXML private Button btnPrepararReporte;

    private final Preferences prefs = Preferences.userNodeForPackage(HistorialMensualController.class);

    @FXML
    public void initialize() {
        cbMesFilter.getItems().addAll(
                "enero","febrero","marzo","abril","mayo","junio",
                "julio","agosto","septiembre","octubre","noviembre","diciembre"
        );

        cargarMesesDisponibles();

        // Inicialización de columnas
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
        colDoc4.setCellValueFactory(new PropertyValueFactory<>("doc4"));
        colDoc5.setCellValueFactory(new PropertyValueFactory<>("doc5"));
        colPens.setCellValueFactory(new PropertyValueFactory<>("pens"));
        tableRecords.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // 🔥 Nuevo: carga automática al iniciar
        load(null);

        // 🔥 Nuevo: recarga automática al cambiar de mes
        cbMesFilter.valueProperty().addListener((obs, oldMes, nuevoMes) -> load(nuevoMes));

        btnEdit.setOnAction(evt -> editSelected());
        btnDelete.setOnAction(evt -> deleteSelected());
        btnClose.setOnAction(evt -> closeWindow());
        btnAgregarAReporte.setOnAction(evt -> agregarAReporte());
        btnPrepararReporte.setOnAction(evt -> abrirPrepararReporte());

        tableRecords.setRowFactory(tv -> {
            TableRow<RegistroMes> row = new TableRow<>();
            row.setOnMouseClicked(ev -> {
                if (ev.getClickCount() == 2 && !row.isEmpty()) editRegistro(row.getItem());
            });
            return row;
        });
    }

    private void loadForSelectedMonth() {
        String mes = cbMesFilter.getValue();
        load(mes);
    }
    // ✅ NUEVO: Método para cargar meses disponibles desde la base de datos
    private void cargarMesesDisponibles() {
        try {
            List<String> mesesDisponibles = historialService.obtenerMesesDisponibles();
            if (!mesesDisponibles.isEmpty()) {
                // Opcional: puedes mostrar los meses disponibles en un label o tooltip
                System.out.println("Meses disponibles en BD: " + mesesDisponibles);
            }
        } catch (Exception e) {
            System.err.println("Error al cargar meses disponibles: " + e.getMessage());
        }
    }
    public static List<RegistroMes> getRegistrosSeleccionados() {
        return registrosSeleccionados;
    }
    public static void limpiarRegistrosSeleccionados() {
        registrosSeleccionados.clear();
        System.out.println("✅ Lista de registros seleccionados limpiada correctamente.");
    }

    private void abrirPrepararReporte() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/preparar_reporte.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Preparar Reporte");
            stage.setScene(new Scene(loader.load()));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error al abrir la vista de reporte: " + e.getMessage());
        }
    }


    private void agregarAReporte() {
        List<RegistroMes> seleccionados = tableRecords.getSelectionModel().getSelectedItems();
        if (seleccionados == null || seleccionados.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Seleccione uno o más registros para agregar al reporte.");
            return;
        }
        registrosSeleccionados.addAll(seleccionados);
        showAlert(Alert.AlertType.INFORMATION, "Agregados " + seleccionados.size() + " registros al reporte.");
    }

    private void load(String mes) {
        List<RegistroMes> registros;

        if (mes == null || mes.isBlank()) {
            // ✅ NUEVO: Si no hay mes seleccionado, cargar todos los registros
            registros = historialService.buscarRegistros(null, tfNip.getText(), tfCInfra.getText());
            lblStatus.setText("Todos los registros: " + registros.size() + " - Filtrados por: " +
                    (tfNip.getText().isEmpty() ? "" : "NIP ") +
                    (tfCInfra.getText().isEmpty() ? "" : "C_INFRA"));
        } else {
            // Carga normal por mes
            registros = historialService.buscarRegistros(mes.toLowerCase(), tfNip.getText(), tfCInfra.getText());
            lblStatus.setText("Registros: " + registros.size() + " — mes: " + mes);
        }

        tableRecords.getItems().setAll(registros);
    }

    // ✅ NUEVO: Método para limpiar filtros y mostrar todos los meses
    @FXML
    private void mostrarTodosLosMeses() {
        cbMesFilter.getSelectionModel().clearSelection();
        tfNip.clear();
        tfCInfra.clear();
        load(null);
    }


    private void refresh() { load(cbMesFilter.getValue()); }

    private void editSelected() {
        RegistroMes sel = tableRecords.getSelectionModel().getSelectedItem();
        if (sel != null) editRegistro(sel);
    }

    private void editRegistro(RegistroMes r) {
        Dialog<RegistroDTO> dialog = new Dialog<>();
        dialog.setTitle("Editar registro - NIP: " + r.getNip());
        ButtonType btnOk = new ButtonType("Guardar", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnOk, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(8); grid.setVgap(8);
        grid.setPadding(new Insets(10));

        TextField tfCInfraD = new TextField(r.getCInfra());
        TextField tfNipD = new TextField(r.getNip());
        TextField tfNombreD = new TextField(r.getNombreDocente());
        TextField tfCentroD = new TextField(r.getCentroEducativo());
        TextField tfMunicipioD = new TextField(r.getMunicipio());
        TextField tfDistritoD = new TextField(r.getDistrito());
        DatePicker dpDesde = new DatePicker(r.getDesde());
        DatePicker dpHasta = new DatePicker(r.getHasta());
        TextField tfHoras = new TextField(r.getHoras() == null ? "" : r.getHoras().toString());
        TextField tfPat = new TextField(r.getPatologia());
        TextArea taObs = new TextArea(r.getObservaciones());
        TextField tfDoc4 = new TextField(r.getDoc4());
        TextField tfDoc5 = new TextField(r.getDoc5());
        TextField tfPens = new TextField(r.getPens());

        ComboBox<String> cbMesLocal = new ComboBox<>();
        cbMesLocal.getItems().addAll(
                "enero","febrero","marzo","abril","mayo","junio",
                "julio","agosto","septiembre","octubre","noviembre","diciembre"
        );
        cbMesLocal.getSelectionModel().select(r.getMes());

        grid.add(new Label("C INFRA"), 0, 0); grid.add(tfCInfraD, 1, 0);
        grid.add(new Label("NIP"), 0, 1); grid.add(tfNipD, 1, 1);
        grid.add(new Label("NOMBRE"), 0, 2); grid.add(tfNombreD, 1, 2);
        grid.add(new Label("CENTRO"), 0, 3); grid.add(tfCentroD, 1, 3);
        grid.add(new Label("MUNICIPIO"), 0, 4); grid.add(tfMunicipioD, 1, 4);
        grid.add(new Label("DISTRITO"), 0, 5); grid.add(tfDistritoD, 1, 5);
        grid.add(new Label("DESDE"), 0, 6); grid.add(dpDesde, 1, 6);
        grid.add(new Label("HASTA"), 0, 7); grid.add(dpHasta, 1, 7);
        grid.add(new Label("HORAS"), 0, 8); grid.add(tfHoras, 1, 8);
        grid.add(new Label("PATOLOGIA"), 0, 9); grid.add(tfPat, 1, 9);
        grid.add(new Label("OBSERVACIONES"), 0, 10); grid.add(taObs, 1, 10);
        grid.add(new Label("MES"), 0, 11); grid.add(cbMesLocal, 1, 11);

        dialog.getDialogPane().setContent(grid);

        // Listeners autocompletar NIP -> Nombre
        tfNipD.setOnAction(evt -> buscarNip(tfNipD, tfNombreD));
        tfNipD.focusedProperty().addListener((obs, oldV, newV) -> { if (!newV) buscarNip(tfNipD, tfNombreD); });

        // Listeners autocompletar C_INFRA -> Centro/Municipio/Distrito
        tfCInfraD.setOnAction(evt -> buscarEscuela(tfCInfraD, tfCentroD, tfMunicipioD, tfDistritoD));
        tfCInfraD.focusedProperty().addListener((obs, oldV, newV) -> { if (!newV) buscarEscuela(tfCInfraD, tfCentroD, tfMunicipioD, tfDistritoD); });

        Node okButtonNode = dialog.getDialogPane().lookupButton(btnOk);
        okButtonNode.setDisable(cbMesLocal.getSelectionModel().isEmpty());
        cbMesLocal.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> okButtonNode.setDisable(n == null || n.isBlank()));

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnOk) {
                return RegistroDTO.builder()
                        .cInfra(tfCInfraD.getText())
                        .nip(tfNipD.getText())
                        .nombreDocente(tfNombreD.getText())
                        .centroEducativo(tfCentroD.getText())
                        .municipio(tfMunicipioD.getText())
                        .distrito(tfDistritoD.getText())
                        .horas(parseInteger(tfHoras.getText()))
                        .desde(dpDesde.getValue())
                        .hasta(dpHasta.getValue())
                        .patologia(tfPat.getText())
                        .observaciones(taObs.getText())
                        .doc4(tfDoc4.getText())
                        .doc5(tfDoc5.getText())
                        .pens(tfPens.getText())
                        .mes(cbMesLocal.getValue() == null ? "" : cbMesLocal.getValue().toLowerCase())
                        .build();
            }
            return null;
        });

        Optional<RegistroDTO> result = dialog.showAndWait();
        result.ifPresent(dto -> {
            historialService.update(r.getId(), dto);
            load(dto.getMes());
            showAlert(Alert.AlertType.INFORMATION, "Registro actualizado.");
        });
    }

    private void buscarNip(TextField nipField, TextField nameField) {
        String nip = nipField.getText();
        if (nip == null || nip.isBlank()) { nameField.clear(); return; }
        docenteService.findByNip(nip.trim()).ifPresent(d -> nameField.setText(d.getDepersona()));
    }

    private void buscarEscuela(TextField cInfraField, TextField centro, TextField municipio, TextField distrito) {
        String cin = cInfraField.getText();
        if (cin == null || cin.isBlank()) { centro.clear(); municipio.clear(); distrito.clear(); return; }
        escuelaService.findByCInfra(cin.trim()).ifPresent(e -> {
            centro.setText(e.getNombre());
            municipio.setText(e.getMunicipio());
            distrito.setText(e.getDistrito());
        });
    }
    private static final List<RegistroMes> registrosSeleccionados = new ArrayList<>();

    @FXML
    private void importarExcelAction() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx", "*.xls"));
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            TextInputDialog dialog = new TextInputDialog("enero");
            dialog.setHeaderText("Mes al que asignar los registros");
            dialog.setContentText("Ingrese el mes:");
            dialog.showAndWait().ifPresent(mes -> {
                try {
                    // Usamos la instancia inyectada, no la clase
                    List<RegistroDTO> importados = excelImportService.importarExcel(file, mes);
                    System.out.println("Registros importados: " + importados.size());
                    showAlert(Alert.AlertType.INFORMATION, "Importación completada: " + importados.size() + " registros.");
                    load(mes.toLowerCase()); // recarga la tabla para mostrar los nuevos registros
                } catch (Exception e) {
                    e.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Error al importar Excel: " + e.getMessage());
                    alert.showAndWait();
                }
            });
        }
    }

    private void deleteSelected() {
        RegistroMes sel = tableRecords.getSelectionModel().getSelectedItem();
        if (sel != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "¿Eliminar registro NIP " + sel.getNip() + "?", ButtonType.YES, ButtonType.NO);
            Optional<ButtonType> r = confirm.showAndWait();
            if (r.isPresent() && r.get() == ButtonType.YES) {
                historialService.deleteById(sel.getId());
                loadForSelectedMonth();
                showAlert(Alert.AlertType.INFORMATION, "Registro eliminado.");
            }
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


    @FXML
    private void volverAlHome() {
        applicationManager.cambiarVista("/Views/home.fxml", "Gestión de Escuelas",true);
    }

}
