package com.gidoc.gdoc.Usuarios.web.controllers;

import com.gidoc.gdoc.GDYBD.repo.implementations.ImportServiceImpl;
import com.gidoc.gdoc.GDYBD.web.controllers.ImportController;
import com.gidoc.gdoc.Usuarios.domain.entities.Usuario;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.io.IOException;
import java.time.YearMonth;
@Component

@Controller
@Slf4j
public class HomeController {

    @FXML private Label welcomeLabel;
    @FXML private Label userInfoLabel;
    @FXML private Label menuUserInfo;
    @FXML private Button logoutButton;
    @FXML private Button menuButton;
//    @FXML private ScrollPane menuPanel;
    @FXML private VBox adminSecuritySection;
    @FXML private FlowPane quickMonthButtons;
    @FXML private StackPane mainContent;
    @FXML private AnchorPane menuPanel;
    @FXML
    private Rectangle menuOverlay;

    private Usuario usuarioLogueado;
    private boolean menuAbierto = false;
    private boolean animando = false;
    private YearMonth currentYearMonth;

    private final ApplicationManager applicationManager;
    private final UserSession userSession; // 🔹 Inyección del UserSession
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    public HomeController(ApplicationManager applicationManager, UserSession userSession) {
        this.applicationManager = applicationManager;
        this.userSession = userSession;
    }


    @FXML
    public void initialize() {
        log.info("HomeController inicializado correctamente");

        menuPanel.setTranslateX(-250);
        menuPanel.setVisible(false);
        menuPanel.setManaged(false);

        currentYearMonth = YearMonth.now();

        // ✅ Restaurar usuario desde sesión
        Platform.runLater(() -> {
            Usuario usuario = userSession.getUsuarioActual();
            if (usuario != null) {
                setUsuarioLogueado(usuario);
                log.info("Usuario restaurado automáticamente en initialize: {}", usuario.getUsername());
            } else {
                log.warn("No se encontró usuario en sesión al inicializar Home");
            }
        });
        // 🔹 Cargar automáticamente el formulario principal al abrir el Home
        Platform.runLater(this::cargarVistaRegistroDocente);  // 👈 añadido
    }



    public void setUsuarioLogueado(Usuario usuario) {
        this.usuarioLogueado = usuario;
        if (usuario != null) {
            welcomeLabel.setText("¡Bienvenido, " + usuario.getNombreCompleto() + "!");
            userInfoLabel.setText("Usuario: " + usuario.getUsername() +
                    " | " + (usuario.getAdministrador() ? "Administrador" : "Docente"));
            menuUserInfo.setText(usuario.getNombreCompleto());
            adminSecuritySection.setVisible(usuario.getAdministrador());
            adminSecuritySection.setManaged(usuario.getAdministrador());
        }
    }
    public void cargarUsuario(Usuario usuario) {
        if (usuario != null) {
            userSession.setUsuarioActual(usuario);
            setUsuarioLogueado(usuario);
            log.info("Usuario restaurado al volver al Home: {}", usuario.getUsername());
        }
    }

    // 🔹 Alternar apertura/cierre del menú lateral
    @FXML
    private void toggleMenu() {
        if (animando) return;

        if (menuAbierto) {
            cerrarMenu();
        } else {
            abrirMenu();
        }
    }

    private void abrirMenu() {
        animando = true;
        menuPanel.setVisible(true);
        menuPanel.setManaged(true);

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), menuPanel);
        slideIn.setFromX(-250);
        slideIn.setToX(0);
        slideIn.setOnFinished(e -> {
            menuAbierto = true;
            animando = false;
        });
        slideIn.play();
    }

    private void cerrarMenu() {
        animando = true;
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), menuPanel);
        slideOut.setFromX(0);
        slideOut.setToX(-250);
        slideOut.setOnFinished(e -> {
            menuPanel.setVisible(false);   // ✅ Ocultar
            menuPanel.setManaged(false);   // ✅ Excluir del layout
            menuAbierto = false;           // ✅ Marcar como cerrado
            animando = false;
        });
        slideOut.play();
    }




    // 🔹 Cargar el formulario REGDOC automáticamente con contexto Spring
    private void cargarVistaRegistroDocente() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/Views/registro_form.fxml")
            );

            // ✅ Usa el ApplicationContext para que @Autowired funcione
            loader.setControllerFactory(applicationContext::getBean);

            Parent vistaForm = loader.load();
            mainContent.getChildren().setAll(vistaForm);



            log.info("Formulario REGDOC cargado automáticamente con inyección Spring");
            // --------- NUEVO: fijar tamaño preferido y margen ----------
            vistaForm.prefWidth(1500);
            vistaForm.prefHeight(1200);
            // ----------------------------------------------------------

        } catch (IOException e) {
            log.error("Error al cargar el formulario REGDOC automáticamente", e);
        }
    }


    @FXML
    private void logout() {
        log.info("Cerrando sesión de usuario...");
        applicationManager.cerrarSesion();
        userSession.clear(); // 🔹 Limpiamos la sesión
    }

    @FXML
    private void abrirImportarBD() {
        applicationManager.cambiarVista("/Views/import_view.fxml", "Importar Docentes",true);

    }

    @FXML
    private void abrirRegistroDocente() {
        cerrarMenu();
        log.info("Abriendo Registrar Docente (REGDOC)");
        applicationManager.cambiarVista("/Views/registro_form.fxml", "Registrar Incapacidad - Registro Docente", true);
    }

    @FXML
    private void abrirHistorialMensual() { cerrarMenu(); log.info("Abriendo Historial Mensual");
        applicationManager.cambiarVista("/Views/historial_mensual.fxml", "Gestion De Historial Mensual", true);
    }


    // 🔹 Resto de métodos de navegación se mantiene intacto
    @FXML private void abrirDashboard() { cerrarMenu(); log.info("Abriendo Dashboard Principal"); }
    @FXML private void abrirVistaGeneral() { cerrarMenu(); log.info("Abriendo Vista General"); }
    @FXML private void abrirAccesosRapidos() { cerrarMenu(); log.info("Abriendo Accesos Rápidos"); }
    @FXML private void abrirCambioPassword() { cerrarMenu(); log.info("Abriendo Cambio de Contraseña"); }
    @FXML private void abrirGestionUsuarios() { cerrarMenu(); log.info("Abriendo Gestión de Usuarios"); }
    @FXML private void abrirImportarDocentes() { cerrarMenu(); log.info("Abriendo Importar Docentes"); }
    @FXML private void abrirBuscarNIP() { cerrarMenu(); log.info("Abriendo Buscar por NIP"); }
    @FXML private void abrirBuscarINFRA() { cerrarMenu(); log.info("Abriendo Buscar INFRA"); }

    @FXML private void abrirHistorialIncapacidades() { cerrarMenu(); log.info("Abriendo Historial Incapacidades"); }
    @FXML private void abrirRegistrarMaternidad() { cerrarMenu(); log.info("Abriendo Registrar Maternidad"); }
    @FXML private void abrirReporteWord() { cerrarMenu(); log.info("Abriendo Reporte Word"); }
    @FXML private void abrirReporteExcelCentro() { cerrarMenu(); log.info("Abriendo Reporte Excel Centro"); }
    @FXML private void abrirExportarConsolidado() { cerrarMenu(); log.info("Abriendo Exportar Consolidado"); }

    @FXML private void abrirGestionDocumentos() { cerrarMenu(); log.info("Abriendo Gestión de Documentos"); }
    @FXML private void abrirMisDatos() { cerrarMenu(); log.info("Abriendo Mis Datos"); }
    @FXML private void abrirReportes() { cerrarMenu(); log.info("Abriendo Reportes"); }

    @FXML private void abrirEstructuraBD(ActionEvent event) { cerrarMenu(); log.info("Abriendo Estructura BD"); }
    @FXML private void abrirEditarIncapacidades(ActionEvent event) { cerrarMenu(); log.info("Abriendo Editar Incapacidades"); }
    @FXML private void abrirRegistrarSinGoce(ActionEvent event) { cerrarMenu(); log.info("Abriendo Registrar Sin Goce"); }
    @FXML private void abrirRegistrarDuelo(ActionEvent event) { cerrarMenu(); log.info("Abriendo Registrar Duelo"); }
    @FXML private void abrirRegistrarTerapia(ActionEvent event) { cerrarMenu(); log.info("Abriendo Registrar Terapia"); }
    @FXML private void abrirHistorialPermisos(ActionEvent event) { cerrarMenu(); log.info("Abriendo Historial Permisos"); }
    @FXML private void abrirReporteExcelNIP(ActionEvent event) { cerrarMenu(); log.info("Abriendo Reporte Excel NIP"); }
    @FXML private void abrirReporteMensual(ActionEvent event) { cerrarMenu(); log.info("Abriendo Reporte Mensual"); }
    @FXML private void abrirFiltrosConsolidado(ActionEvent event) { cerrarMenu(); log.info("Abriendo Filtros Consolidado"); }
    @FXML private void abrirExportarImprimir(ActionEvent event) { cerrarMenu(); log.info("Abriendo Exportar/Imprimir"); }
}
