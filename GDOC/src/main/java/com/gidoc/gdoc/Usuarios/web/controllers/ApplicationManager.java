package com.gidoc.gdoc.Usuarios.web.controllers;

import com.gidoc.gdoc.Usuarios.domain.entities.Usuario;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ApplicationManager {

    private final ApplicationContext applicationContext;
    private Stage primaryStage;
    private Stage currentStage;

    public ApplicationManager(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
        this.currentStage = stage;
    }

    public void mostrarLogin() {
        try {
            // Cerrar ventana actual si existe
            if (currentStage != null) {
                currentStage.close();
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/login.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            Stage loginStage = new Stage();
            loginStage.setTitle("Sistema GDOC - Login");
            loginStage.setScene(new Scene(root, 800, 600));
            loginStage.setMinWidth(600);
            loginStage.setMinHeight(500);

            // Configurar el cierre
            loginStage.setOnCloseRequest(e -> {
                log.info("Aplicación cerrada desde login");
                Platform.exit();
            });

            loginStage.show();
            this.currentStage = loginStage;

            log.info("Ventana de login mostrada correctamente");

        } catch (Exception e) {
            log.error("Error al mostrar login", e);
            Platform.exit();
        }
    }

    public void mostrarHome(Usuario usuario) {
        try {
            if (currentStage != null) {
                currentStage.close();
            }

            // ✅ Guardar usuario en sesión
            UserSession userSession = applicationContext.getBean(UserSession.class);
            userSession.setUsuarioActual(usuario);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/home.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            Stage homeStage = new Stage();
            homeStage.setTitle("Sistema GESTIÓN DE INCAPACIDAD DE DOCENTES - Inicio");
            Scene scene = new Scene(root, 1200, 800); // ✅ Tamaño base
            homeStage.setScene(scene);

            homeStage.setMaximized(true); // ✅ Pantalla completa

            homeStage.setOnCloseRequest(e -> {
                log.info("Aplicación cerrada desde home");
                Platform.exit();
            });

            homeStage.show();
            this.currentStage = homeStage;

            log.info("Ventana de home mostrada para usuario: {}", usuario.getUsername());

        } catch (Exception e) {
            log.error("Error al mostrar home", e);
            mostrarLogin();
        }
    }
    public void cambiarVista(String fxmlPath, String tituloVentana, boolean pantallaCompletaReal) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            if (currentStage == null) {
                currentStage = new Stage();
                currentStage.setMinWidth(1400);
                currentStage.setMinHeight(800);
                currentStage.setResizable(true);
            }

            currentStage.setTitle(tituloVentana);
            Scene scene = new Scene(root);
            currentStage.setScene(scene);

            if (pantallaCompletaReal) {
                currentStage.setFullScreen(true);
                currentStage.setFullScreenExitHint(""); // ✅ sin mensaje molesto// ✅ pantalla completa real
            } else {
                currentStage.setMaximized(true);  // ✅ pantalla completa con barra de título
            }

            currentStage.show();

        } catch (Exception e) {
            log.error("Error al cambiar vista: {}", fxmlPath, e);
            mostrarLogin();
        }
    }






    public void mostrarVista(String fxmlPath, String tituloVentana, boolean pantallaCompleta) {
        try {
            if (currentStage != null) {
                currentStage.close();
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle(tituloVentana);
            Scene scene = new Scene(root, 1200, 800); // ✅ Tamaño base
            stage.setScene(scene);

            if (pantallaCompleta) {
                stage.setMaximized(true); // ✅ Pantalla completa
            } else {
                stage.setMinWidth(1000);
                stage.setMinHeight(700);
            }

            stage.setOnCloseRequest(e -> {
                log.info("Ventana cerrada: {}", tituloVentana);
                Platform.exit();
            });

            stage.show();
            this.currentStage = stage;

            log.info("Vista mostrada: {}", tituloVentana);

        } catch (Exception e) {
            log.error("Error al mostrar vista: {}", fxmlPath, e);
            mostrarLogin(); // fallback
        }
    }

    public void cerrarSesion() {
        try {
            log.info("Cerrando sesión...");

            // Cerrar ventana actual
            if (currentStage != null) {
                currentStage.close();
                log.debug("Ventana actual cerrada");
            }

            // Mostrar login
            mostrarLogin();

            log.info("Sesión cerrada correctamente");

        } catch (Exception e) {
            log.error("Error al cerrar sesión", e);
            Platform.exit();
        }
    }

    public void cerrarAplicacion() {
        log.info("Cerrando aplicación...");
        Platform.exit();
    }

    public Stage getCurrentStage() {
        return currentStage;
    }
}