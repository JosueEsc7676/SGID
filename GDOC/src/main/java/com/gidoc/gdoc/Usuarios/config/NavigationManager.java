package com.gidoc.gdoc.Usuarios.config;

import com.gidoc.gdoc.Usuarios.domain.entities.Usuario;
import com.gidoc.gdoc.Usuarios.web.controllers.HomeController;
import com.gidoc.gdoc.Usuarios.web.controllers.UserSession;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;

import java.io.IOException;

public class NavigationManager {

    public static void cambiarVista(String fxmlPath, ApplicationContext context, Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(NavigationManager.class.getResource(fxmlPath));
            loader.setControllerFactory(context::getBean);
            Parent root = loader.load();

            Object controller = loader.getController();

            // Restaurar usuario si es HomeController
            if (controller instanceof HomeController) {
                Usuario usuario = context.getBean(UserSession.class).getUsuarioActual();
                Platform.runLater(() -> ((HomeController) controller).cargarUsuario(usuario));
            }

            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
