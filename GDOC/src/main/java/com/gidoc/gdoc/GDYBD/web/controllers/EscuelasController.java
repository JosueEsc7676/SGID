package com.gidoc.gdoc.GDYBD.web.controllers;

import com.gidoc.gdoc.GDYBD.domain.entities.Escuela;
import com.gidoc.gdoc.GDYBD.domain.services.ImportService;
import com.gidoc.gdoc.Usuarios.config.NavigationManager;
import com.gidoc.gdoc.Usuarios.domain.entities.Usuario;
import com.gidoc.gdoc.Usuarios.web.controllers.ApplicationManager;
import com.gidoc.gdoc.Usuarios.web.controllers.HomeController;
import com.gidoc.gdoc.Usuarios.web.controllers.UserSession;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Slf4j
@Controller
public class EscuelasController {

    private final ImportService importService;
    private final ApplicationContext applicationContext;
    @Autowired
    private UserSession userSession;
    @Autowired
    private ApplicationManager applicationManager;

    @Autowired
    public EscuelasController(ImportService importService, ApplicationContext applicationContext, UserSession userSession) {
        this.importService = importService;
        this.applicationContext = applicationContext;
        this.userSession = userSession; // ✅ inyectado
    }
    @FXML private Label fileLabel;
    @FXML private ProgressBar progressBar;
    @FXML private Button importarButton;
    @FXML private Button actualizarButton;
    @FXML private Button seleccionarArchivoButton;

    @FXML private TableView<Escuela> tblEscuelas;
    @FXML private TableColumn<Escuela, String> colEscInfra;
    @FXML private TableColumn<Escuela, String> colEscNum;
    @FXML private TableColumn<Escuela, String> colEscDist;
    @FXML private TableColumn<Escuela, String> colEscName;
    @FXML private TableColumn<Escuela, String> colEscMun;
    @FXML private TableColumn<Escuela, Object> colEscImported;

    // Overlay flotante
    @FXML private StackPane overlayPane;
    @FXML private Label overlayLabel;
    @FXML private ProgressBar overlayProgress;
    @FXML private TextArea overlayMessages;

    private File archivoSeleccionado;

    @FXML
    public void initialize() {
        progressBar.setProgress(0);
        importarButton.setDisable(true);
        actualizarButton.setDisable(true);

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
        fileChooser.setTitle("Seleccionar archivo de Escuelas");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivos soportados (*.xls, *.xlsx, *.xlsm, *.csv, *.dbf)",
                        "*.xls", "*.xlsx", "*.xlsm", "*.csv", "*.dbf")
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
        if (archivoSeleccionado == null) {
            mostrarAlerta("Error", "Debe seleccionar un archivo.", Alert.AlertType.WARNING);
            return;
        }

        overlayLabel.setText("Cargando base de Escuelas...");
        overlayProgress.setProgress(0);
        overlayMessages.clear();
        overlayMessages.appendText("⚠ Esto puede tardar varios minutos...\n");
        overlayMessages.appendText("⚠ Archivos grandes pueden demorar más.\n");
        overlayMessages.appendText("⚠ No cierre la aplicación mientras se realiza la importación.\n");
        overlayPane.setVisible(true);

        new Thread(() -> {
            try {
                int batchSize = 500;
                List<Escuela> all = importService.obtenerTodasEscuelas();
                int total = all.size() > 0 ? all.size() : 1;
                int processed = 0;

                importService.importarExcel(archivoSeleccionado, "Escuelas");

                for (int i = 0; i < total; i += batchSize) {
                    processed += Math.min(batchSize, total - i);
                    double progress = (double) processed / total;
                    int currentBatch = i / batchSize + 1;
                    Platform.runLater(() -> {
                        overlayProgress.setProgress(progress);
                        overlayMessages.appendText("Procesando lote " + currentBatch + "...\n");
                    });
                    Thread.sleep(100);
                }

                Platform.runLater(() -> {
                    overlayProgress.setProgress(1);
                    overlayLabel.setText("Carga de Escuelas finalizada ✔");
                    overlayMessages.appendText("\n✅ Importación completada correctamente.\n");
                    refrescarTabla();

                    new Thread(() -> {
                        try { Thread.sleep(2500); } catch (InterruptedException ignored) {}
                        Platform.runLater(() -> overlayPane.setVisible(false));
                    }).start();
                });

            } catch (Exception e) {
                log.error("Error importando escuelas", e);
                Platform.runLater(() -> {
                    overlayPane.setVisible(false);
                    mostrarAlerta("Error", "Ocurrió un error al importar: " + e.getMessage(), Alert.AlertType.ERROR);
                });
            }
        }).start();
    }

    @FXML
    private void actualizarDatos(ActionEvent event) {
        if (archivoSeleccionado == null) {
            mostrarAlerta("Error", "Debe seleccionar un archivo.", Alert.AlertType.WARNING);
            return;
        }

        overlayLabel.setText("Actualizando base de Escuelas...");
        overlayProgress.setProgress(-1);
        overlayMessages.clear();
        overlayMessages.appendText("⚠ Esto puede tardar varios minutos...\n");
        overlayMessages.appendText("⚠ No cierre la aplicación mientras se actualiza la base de Escuelas.\n");
        overlayPane.setVisible(true);

        new Thread(() -> {
            try {
                importService.actualizarDesdeExcel(archivoSeleccionado, "Escuelas");
                Platform.runLater(() -> {
                    overlayProgress.setProgress(1);
                    overlayLabel.setText("Actualización completada ✔");
                    overlayMessages.appendText("\n✅ Base de Escuelas actualizada correctamente.\n");
                    refrescarTabla();

                    new Thread(() -> {
                        try { Thread.sleep(2500); } catch (InterruptedException ignored) {}
                        Platform.runLater(() -> overlayPane.setVisible(false));
                    }).start();
                });

            } catch (Exception e) {
                log.error("Error actualizando escuelas", e);
                Platform.runLater(() -> {
                    overlayPane.setVisible(false);
                    mostrarAlerta("Error", "Ocurrió un error al actualizar: " + e.getMessage(), Alert.AlertType.ERROR);
                });
            }
        }).start();
    }

    private void refrescarTabla() {
        tblEscuelas.setItems(FXCollections.observableArrayList(importService.obtenerTodasEscuelas()));
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }


    @FXML
    private void volverABaseDocentes() {
        applicationManager.cambiarVista("/Views/import_view.fxml", "Gestión de Escuelas",true);
    }

    @FXML
    private void volverAlHome() {
        applicationManager.cambiarVista("/Views/home.fxml", "Gestión de Escuelas",true);
    }

    @FXML
    private void agregarEscuela() {
        try {
            Escuela nueva = new Escuela();
            boolean ok = mostrarDialogEscuela(nueva);
            if (ok) {
                importService.agregarEscuela(nueva);
                refrescarTabla();
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
                    refrescarTabla();
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
                    refrescarTabla();
                }
            });
        } else {
            mostrarAlerta("Atención", "Seleccione una escuela para eliminar.", Alert.AlertType.WARNING);
        }
    }

    private boolean mostrarDialogEscuela(Escuela escuela) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/EscuelaDialog.fxml"));
        loader.setControllerFactory(applicationContext::getBean);
        Parent page = loader.load();

        Stage dialogStage = new Stage();
        dialogStage.setTitle("Formulario Escuela");

        // ✅ Establecer como modal
        dialogStage.initModality(Modality.WINDOW_MODAL);

        // ✅ Establecer el dueño correctamente
        dialogStage.initOwner(tblEscuelas.getScene().getWindow());

        // ✅ Tamaño fijo y centrado
        Scene scene = new Scene(page, 600, 400); // ajusta según tu diseño
        dialogStage.setScene(scene);
        dialogStage.setResizable(false);
        dialogStage.centerOnScreen();

        // ✅ Configurar el controlador
        EscuelaDialogController controller = loader.getController();
        controller.setDialogStage(dialogStage);
        controller.setEscuela(escuela);

        dialogStage.showAndWait();
        return controller.isOkClicked();
    }

}
