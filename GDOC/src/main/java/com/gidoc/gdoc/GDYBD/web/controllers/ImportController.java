package com.gidoc.gdoc.GDYBD.web.controllers;

import com.gidoc.gdoc.GDYBD.domain.entities.Docente;
import com.gidoc.gdoc.GDYBD.domain.entities.Escuela;
import com.gidoc.gdoc.GDYBD.domain.services.ImportService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.time.LocalDateTime;

@Slf4j
@Controller
public class ImportController {

    private final ImportService importService;

    @Autowired
    public ImportController(ImportService importService) {
        this.importService = importService;
    }

    @FXML private ComboBox<String> tipoBaseCombo;
    @FXML private Label fileLabel;
    @FXML private ProgressBar progressBar;
    @FXML private Button importarButton;
    @FXML private Button actualizarButton;
    @FXML private Button seleccionarArchivoButton;

    @FXML private TableView<Docente> tblDocentes;
    @FXML private TableColumn<Docente, String> colDocId;
    @FXML private TableColumn<Docente, String> colDocName;
    @FXML private TableColumn<Docente, Object> colDocImported;

    @FXML private TableView<Escuela> tblEscuelas;
    @FXML private TableColumn<Escuela, String> colEscInfra;
    @FXML private TableColumn<Escuela, String> colEscNum;
    @FXML private TableColumn<Escuela, String> colEscDist;
    @FXML private TableColumn<Escuela, String> colEscName;
    @FXML private TableColumn<Escuela, String> colEscMun;
    @FXML private TableColumn<Escuela, Object> colEscImported;

    private File archivoSeleccionado;

    @FXML
    public void initialize() {
        ObservableList<String> tipos = FXCollections.observableArrayList("Docentes", "Escuelas");
        tipoBaseCombo.setItems(tipos);
        progressBar.setProgress(0);
        importarButton.setDisable(true);
        actualizarButton.setDisable(true);

        // Inicializar tabla Docentes
        colDocId.setCellValueFactory(new PropertyValueFactory<>("idpersonaA"));
        colDocName.setCellValueFactory(new PropertyValueFactory<>("depersona"));
        colDocImported.setCellValueFactory(new PropertyValueFactory<>("importedAt"));
        tblDocentes.setItems(FXCollections.observableArrayList(importService.obtenerTodosDocentes()));

        // Inicializar tabla Escuelas
        colEscInfra.setCellValueFactory(new PropertyValueFactory<>("cInfra"));
        colEscNum.setCellValueFactory(new PropertyValueFactory<>("numero"));
        colEscDist.setCellValueFactory(new PropertyValueFactory<>("distrito"));
        colEscName.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colEscMun.setCellValueFactory(new PropertyValueFactory<>("municipio"));
        colEscImported.setCellValueFactory(new PropertyValueFactory<>("importedAt"));
        tblEscuelas.setItems(FXCollections.observableArrayList(importService.obtenerTodasEscuelas()));
    }

    @FXML
    private void seleccionarArchivo(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar archivo");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Todos los formatos soportados (*.xls, *.xlsx, *.xlsm, *.csv, *.dbf)",
                        "*.xls","*.xlsx","*.xlsm","*.csv","*.dbf"
                )
        );

        Stage stage = (Stage) seleccionarArchivoButton.getScene().getWindow();
        archivoSeleccionado = fileChooser.showOpenDialog(stage);

        if (archivoSeleccionado != null) {
            fileLabel.setText("Seleccionado: " + archivoSeleccionado.getName());
            importarButton.setDisable(false);
            actualizarButton.setDisable(false);
        } else {
            fileLabel.setText("No se seleccionó ningún archivo");
        }
    }

    @FXML
    private void importarDatos(ActionEvent event) {
        if (archivoSeleccionado == null || tipoBaseCombo.getValue() == null) {
            mostrarAlerta("Error", "Debe seleccionar un archivo y un tipo de base de datos.", Alert.AlertType.WARNING);
            return;
        }

        try {
            progressBar.setProgress(-1);
            String tipo = tipoBaseCombo.getValue();

            importService.importarExcel(archivoSeleccionado, tipo); // detecta por extensión internamente

            progressBar.setProgress(1);
            mostrarAlerta("Éxito", "Importación completada correctamente.", Alert.AlertType.INFORMATION);
            refrescarTablas();
        } catch (Exception e) {
            log.error("Error importando archivo", e);
            mostrarAlerta("Error", "Ocurrió un error al importar: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void actualizarDatos(ActionEvent event) {
        if (archivoSeleccionado == null || tipoBaseCombo.getValue() == null) {
            mostrarAlerta("Error", "Debe seleccionar un archivo y un tipo de base de datos.", Alert.AlertType.WARNING);
            return;
        }

        try {
            progressBar.setProgress(-1);
            String tipo = tipoBaseCombo.getValue();

            importService.actualizarDesdeExcel(archivoSeleccionado, tipo);

            progressBar.setProgress(1);
            mostrarAlerta("Éxito", "Base de datos actualizada correctamente.", Alert.AlertType.INFORMATION);
            refrescarTablas();
        } catch (Exception e) {
            log.error("Error actualizando datos", e);
            mostrarAlerta("Error", "Ocurrió un error al actualizar: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void refrescarTablas() {
        tblDocentes.setItems(FXCollections.observableArrayList(importService.obtenerTodosDocentes()));
        tblEscuelas.setItems(FXCollections.observableArrayList(importService.obtenerTodasEscuelas()));
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    // ==================== CRUD MANUAL ESCUELAS ====================

    @FXML
    private void agregarEscuela() {
        try {
            Escuela nueva = new Escuela();
            boolean ok = mostrarDialogEscuela(nueva);
            if (ok) {
                importService.agregarEscuela(nueva);
                refrescarTablas();
                mostrarAlerta("Éxito", "Escuela agregada correctamente.", Alert.AlertType.INFORMATION);
            }
        } catch (Exception ex) {
            mostrarAlerta("Error", "No se pudo agregar la escuela: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void editarEscuela() {
        Escuela seleccionada = tblEscuelas.getSelectionModel().getSelectedItem();
        if (seleccionada != null) {
            try {
                Escuela copia = new Escuela(
                        seleccionada.getId(),
                        seleccionada.getCInfra(),
                        seleccionada.getNumero(),
                        seleccionada.getDistrito(),
                        seleccionada.getNombre(),
                        seleccionada.getMunicipio(),
                        seleccionada.getImportedAt()
                );

                boolean ok = mostrarDialogEscuela(copia);
                if (ok) {
                    seleccionada.setCInfra(copia.getCInfra());
                    seleccionada.setNumero(copia.getNumero());
                    seleccionada.setDistrito(copia.getDistrito());
                    seleccionada.setNombre(copia.getNombre());
                    seleccionada.setMunicipio(copia.getMunicipio());

                    importService.editarEscuela(seleccionada);
                    refrescarTablas();
                    mostrarAlerta("Éxito", "Escuela editada correctamente.", Alert.AlertType.INFORMATION);
                }
            } catch (Exception ex) {
                mostrarAlerta("Error", "No se pudo editar la escuela: " + ex.getMessage(), Alert.AlertType.ERROR);
            }
        } else {
            mostrarAlerta("Atención", "Seleccione una escuela para editar.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void eliminarEscuela() {
        Escuela seleccionada = tblEscuelas.getSelectionModel().getSelectedItem();
        if (seleccionada != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar eliminación");
            alert.setHeaderText("Eliminar escuela");
            alert.setContentText("¿Desea eliminar la escuela seleccionada?");
            alert.showAndWait().ifPresent(bt -> {
                if (bt == ButtonType.OK) {
                    importService.eliminarEscuela(seleccionada);
                    refrescarTablas();
                }
            });
        } else {
            mostrarAlerta("Atención", "Seleccione una escuela para eliminar.", Alert.AlertType.WARNING);
        }
    }

    private boolean mostrarDialogEscuela(Escuela escuela) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/EscuelaDialog.fxml"));
        Parent page = loader.load();

        Stage dialogStage = new Stage();
        dialogStage.setTitle("Formulario Escuela");
        dialogStage.initOwner(tblEscuelas.getScene().getWindow());
        dialogStage.setScene(new Scene(page));

        EscuelaDialogController controller = loader.getController();
        controller.setDialogStage(dialogStage);
        controller.setEscuela(escuela);

        dialogStage.showAndWait();
        return controller.isOkClicked();
    }

}
