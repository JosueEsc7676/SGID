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
            // Cerrar ventana actual si existe
            if (currentStage != null) {
                currentStage.close();
            }

            // Abrir home
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Views/home.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            Parent root = loader.load();

            HomeController homeController = loader.getController();
            homeController.setUsuarioLogueado(usuario);

            Stage homeStage = new Stage();
            homeStage.setTitle("Sistema GESTION DE INCAPACIDAD DE DOCENTES - Inicio");
            homeStage.setScene(new Scene(root, 1200, 800));
            homeStage.setMinWidth(1000);
            homeStage.setMinHeight(700);

            homeStage.setOnCloseRequest(e -> {
                log.info("Aplicación cerrada desde home");
                Platform.exit();
            });

            homeStage.show();
            this.currentStage = homeStage;

            log.info("Ventana de home mostrada para usuario: {}", usuario.getUsername());

        } catch (Exception e) {
            log.error("Error al mostrar home", e);
            mostrarLogin(); // Volver al login si hay error
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