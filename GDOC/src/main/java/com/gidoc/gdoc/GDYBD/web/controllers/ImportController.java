package com.gidoc.gdoc.GDYBD.web.controllers;

import com.gidoc.gdoc.GDYBD.domain.entities.Docente;
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
public class ImportController {

    private final ImportService importService;
    private final ApplicationContext applicationContext;
    @Autowired
    private UserSession userSession;
    @Autowired
    private ApplicationManager applicationManager;

    @Autowired
    public ImportController(ImportService importService, ApplicationContext applicationContext, UserSession userSession) {
        this.importService = importService;
        this.applicationContext = applicationContext;
        this.userSession = userSession; // ✅ inyectado
    }


    @FXML private Label fileLabel;
    @FXML private ProgressBar progressBar;
    @FXML private Button importarButton;
    @FXML private Button actualizarButton;
    @FXML private Button seleccionarArchivoButton;

    @FXML private TableView<Docente> tblDocentes;
    @FXML private TableColumn<Docente, String> colDocId;
    @FXML private TableColumn<Docente, String> colDocName;
    @FXML private TableColumn<Docente, Object> colDocImported;
    @FXML private TableColumn<Docente, String> colDoc4;
    @FXML private TableColumn<Docente, String> colDoc5;
    @FXML private TableColumn<Docente, String> colPens;

    // Overlay flotante
    @FXML private StackPane overlayPane;
    @FXML private Label overlayLabel;
    @FXML private ProgressBar overlayProgress;
    @FXML private TextArea overlayMessages;

    private File archivoSeleccionado;

    @FXML
    public void initialize() {
        overlayProgress.setProgress(0);
        importarButton.setDisable(true);
        actualizarButton.setDisable(true);

        colDocId.setCellValueFactory(new PropertyValueFactory<>("idpersonaA"));
        colDocName.setCellValueFactory(new PropertyValueFactory<>("depersona"));
        colDoc4.setCellValueFactory(new PropertyValueFactory<>("doc4"));
        colDoc5.setCellValueFactory(new PropertyValueFactory<>("doc5"));
        colPens.setCellValueFactory(new PropertyValueFactory<>("pens"));

        colDocImported.setCellValueFactory(new PropertyValueFactory<>("importedAt"));
        tblDocentes.setItems(FXCollections.observableArrayList(importService.obtenerTodosDocentes()));
    }

    @FXML
    private void seleccionarArchivo(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar archivo");
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
            mostrarAlerta("Error", "Debe seleccionar un archivo antes de importar.", Alert.AlertType.WARNING);
            return;
        }

        overlayLabel.setText("Cargando base de datos...");
        overlayProgress.setProgress(0);
        overlayMessages.clear();
        overlayMessages.appendText("⚠ Esto puede tardar varios minutos...\n");
        overlayMessages.appendText("⚠ Recuerde que se están cargando miles de datos.\n");
        overlayMessages.appendText("⚠ Archivos muy grandes pueden demorar más tiempo.\n");
        overlayMessages.appendText("⚠ No cierre la aplicación mientras se realiza la importación.\n");
        overlayPane.setVisible(true);

        new Thread(() -> {
            try {
                // Simular lote de datos para progreso real
                int batchSize = 500;
                List<Docente> all = importService.obtenerTodosDocentes(); // Solo para contar
                int total = all.size() > 0 ? all.size() : 1; // Evitar división por cero
                int processed = 0;

                // Llamada real de importación en batches
                importService.importarExcel(archivoSeleccionado, "Docentes");

                // Simulación de progreso visual por lotes
                for (int i = 0; i < total; i += batchSize) {
                    processed += Math.min(batchSize, total - i);
                    double progress = (double) processed / total;
                    int currentBatch = i / batchSize + 1;
                    Platform.runLater(() -> {
                        overlayProgress.setProgress(progress);
                        overlayMessages.appendText("Procesando lote " + currentBatch + "...\n");
                    });
                    Thread.sleep(100); // Simular tiempo de procesamiento
                }

                Platform.runLater(() -> {
                    overlayProgress.setProgress(1);
                    overlayLabel.setText("Carga de la base finalizada ✔");
                    overlayMessages.appendText("\n✅ Importación completada correctamente.\n");
                    refrescarTabla();

                    // Ocultar overlay después de 2.5 segundos
                    new Thread(() -> {
                        try { Thread.sleep(2500); } catch (InterruptedException ignored) {}
                        Platform.runLater(() -> overlayPane.setVisible(false));
                    }).start();
                });

            } catch (Exception e) {
                log.error("Error importando archivo", e);
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
            mostrarAlerta("Error", "Debe seleccionar un archivo antes de actualizar.", Alert.AlertType.WARNING);
            return;
        }

        overlayLabel.setText("Actualizando base de datos...");
        overlayProgress.setProgress(-1);
        overlayMessages.clear();
        overlayMessages.appendText("⚠ Esto puede tardar varios minutos...\n");
        overlayMessages.appendText("⚠ No cierre la aplicación mientras se actualiza la base de docentes.\n");
        overlayPane.setVisible(true);

        new Thread(() -> {
            try {
                importService.actualizarDesdeExcel(archivoSeleccionado, "Docentes");
                Platform.runLater(() -> {
                    overlayProgress.setProgress(1);
                    overlayLabel.setText("Actualización completada ✔");
                    overlayMessages.appendText("\n✅ Base de datos actualizada correctamente.\n");
                    refrescarTabla();

                    new Thread(() -> {
                        try { Thread.sleep(2500); } catch (InterruptedException ignored) {}
                        Platform.runLater(() -> overlayPane.setVisible(false));
                    }).start();
                });
            } catch (Exception e) {
                log.error("Error actualizando datos", e);
                Platform.runLater(() -> {
                    overlayPane.setVisible(false);
                    mostrarAlerta("Error", "Ocurrió un error al actualizar: " + e.getMessage(), Alert.AlertType.ERROR);
                });
            }
        }).start();
    }

    private void refrescarTabla() {
        tblDocentes.setItems(FXCollections.observableArrayList(importService.obtenerTodosDocentes()));
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
//Este bloque de codigo sirve para detectar si el hom esta activo como contenedor principal.
// si lo esta carga la vista, si no lo esta abre una ventana nueva
    @FXML
    private void abrirVistaEscuelas() {
        try {
            // Intenta obtener el controlador del Home actual
            HomeController homeController = applicationContext.getBean(HomeController.class);

            // Si el Home está visible en la escena actual, carga la vista dentro del mainContent
            if (homeController != null) {
                log.info("Cargando vista de Escuelas dentro del Home...");
                homeController.cargarVista("/Views/EscuelasView.fxml");
            } else {
                // En caso de no estar dentro del Home (fallback)
                log.warn("No se encontró HomeController activo, abriendo vista en nueva ventana.");
                applicationManager.cambiarVista("/Views/EscuelasView.fxml", "Gestión de Escuelas", true);
            }

        } catch (Exception e) {
            log.error("Error al intentar cargar EscuelasView dentro del Home", e);
            // Como respaldo, abre en una nueva ventana
            applicationManager.cambiarVista("/Views/EscuelasView.fxml", "Gestión de Escuelas", true);
        }
    }



    @FXML
    private void volverAlHome(ActionEvent event) {
        Usuario usuario = userSession.getUsuarioActual(); // ✅ Recupera el usuario actual
        if (usuario != null) {
            userSession.setUsuarioActual(usuario); // ✅ Reafirma que esté guardado
        }

        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        applicationManager.cambiarVista("/Views/home.fxml", "Inicio",true);
    }




}