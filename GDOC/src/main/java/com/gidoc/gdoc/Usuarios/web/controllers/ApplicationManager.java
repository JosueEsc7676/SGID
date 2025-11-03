package com.gidoc.gdoc.Usuarios.web.controllers;

import com.gidoc.gdoc.Usuarios.domain.entities.Usuario;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
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
    private double xOffset = 0;
    private double yOffset = 0;

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
    private void hacerVentanaArrastrable(Stage stage, Parent root) {
        // Detecta la posición inicial del clic
        root.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        // Permite mover la ventana según el arrastre del mouse
        root.setOnMouseDragged(event -> {
            if (!stage.isMaximized()) { // No mover si está maximizada
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            }
        });
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
            Scene scene = new Scene(root, 1440, 900); // ✅ Tamaño base
            homeStage.setScene(scene);
//            homeStage.initStyle(javafx.stage.StageStyle.UNDECORATED); // ❌ Sin barra del sistema

            homeStage.setMaximized(false); // ✅ Pantalla completa

            homeStage.setOnCloseRequest(e -> {
                log.info("Aplicación cerrada desde home");
                Platform.exit();
            });
// Obtener tamaño de pantalla
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

// Ajustar tamaño dinámicamente (por ejemplo, al 90% de la pantalla)
            scene.getWindow().setWidth(screenBounds.getWidth() * 0.9);
            scene.getWindow().setHeight(screenBounds.getHeight() * 0.9);
            hacerVentanaRedimensionable(homeStage, root);
            hacerVentanaArrastrable(homeStage, root);

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
                currentStage.setMinWidth(1440);
                currentStage.setMinHeight(900);
                currentStage.setResizable(true);
            }

            currentStage.setTitle(tituloVentana);
            Scene scene = new Scene(root);
            currentStage.setScene(scene);

            if (pantallaCompletaReal) {
                currentStage.setFullScreen(false);
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

    private void hacerVentanaRedimensionable(Stage stage, Parent root) {
        final double borde = 8;

        root.setOnMouseMoved(event -> {
            double x = event.getX();
            double y = event.getY();
            double ancho = stage.getWidth();
            double alto = stage.getHeight();

            if (x < borde && y < borde) root.setCursor(Cursor.NW_RESIZE);
            else if (x > ancho - borde && y < borde) root.setCursor(Cursor.NE_RESIZE);
            else if (x < borde && y > alto - borde) root.setCursor(Cursor.SW_RESIZE);
            else if (x > ancho - borde && y > alto - borde) root.setCursor(Cursor.SE_RESIZE);
            else if (x < borde) root.setCursor(Cursor.W_RESIZE);
            else if (x > ancho - borde) root.setCursor(Cursor.E_RESIZE);
            else if (y < borde) root.setCursor(Cursor.N_RESIZE);
            else if (y > alto - borde) root.setCursor(Cursor.S_RESIZE);
            else root.setCursor(Cursor.DEFAULT);
        });

        root.setOnMouseDragged(event -> {
            if (root.getCursor() == Cursor.DEFAULT) return;

            double x = event.getSceneX();
            double y = event.getSceneY();

            if (root.getCursor() == Cursor.E_RESIZE) stage.setWidth(x);
            else if (root.getCursor() == Cursor.S_RESIZE) stage.setHeight(y);
            else if (root.getCursor() == Cursor.SE_RESIZE) {
                stage.setWidth(x);
                stage.setHeight(y);
            }
            event.consume();
        });
    }





//    public void mostrarVista(String fxmlPath, String tituloVentana, boolean pantallaCompleta) {
//        try {
//            if (currentStage != null) {
//                currentStage.close();
//            }
//
//            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
//            loader.setControllerFactory(applicationContext::getBean);
//            Parent root = loader.load();
//
//            Stage stage = new Stage();
//            stage.setTitle(tituloVentana);
//            Scene scene = new Scene(root, 1200, 800); // ✅ Tamaño base
//            stage.setScene(scene);
//
//            if (pantallaCompleta) {
//                stage.setMaximized(true); // ✅ Pantalla completa
//            } else {
//                stage.setMinWidth(1000);
//                stage.setMinHeight(700);
//            }
//
//            stage.setOnCloseRequest(e -> {
//                log.info("Ventana cerrada: {}", tituloVentana);
//                Platform.exit();
//            });
//
//            stage.show();
//            this.currentStage = stage;
//
//            log.info("Vista mostrada: {}", tituloVentana);
//
//        } catch (Exception e) {
//            log.error("Error al mostrar vista: {}", fxmlPath, e);
//            mostrarLogin(); // fallback
//        }
//    }

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