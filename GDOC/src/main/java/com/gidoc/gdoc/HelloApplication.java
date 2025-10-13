package com.gidoc.gdoc;

import com.gidoc.gdoc.Usuarios.domain.entities.Usuario;
import com.gidoc.gdoc.Usuarios.domain.services.UsuarioService;
import com.gidoc.gdoc.Usuarios.web.controllers.ApplicationManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@Slf4j
public class HelloApplication extends Application {




    private ConfigurableApplicationContext springContext;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() throws Exception {
        try {
            log.info("Inicializando Spring Boot...");
            springContext = SpringApplication.run(HelloApplication.class);
            log.info("Spring Boot inicializado correctamente");
            crearUsuarioAdministrador();
        } catch (Exception e) {
            log.error("Error durante la inicialización", e);
            Platform.exit();
            throw e;
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            log.info("Iniciando aplicación JavaFX...");

            // Obtener ApplicationManager y configurar primary stage
            ApplicationManager applicationManager = springContext.getBean(ApplicationManager.class);
            applicationManager.setPrimaryStage(primaryStage);

            // Mostrar la ventana de login usando ApplicationManager
            applicationManager.mostrarLogin();

            log.info("Aplicación JavaFX iniciada correctamente");

        } catch (Exception e) {
            log.error("Error al iniciar la aplicación JavaFX", e);
            throw e;
        }
    }

    @Override
    public void stop() throws Exception {
        log.info("Deteniendo aplicación...");
        if (springContext != null && springContext.isActive()) {
            springContext.close();
        }
        super.stop();
    }

    private void crearUsuarioAdministrador() {
        try {
            UsuarioService usuarioService = springContext.getBean(UsuarioService.class);
            var usuarioOpt = usuarioService.buscarPorUsername("admin");
            if (usuarioOpt.isEmpty()) {
                Usuario admin = new Usuario();
                admin.setUsername("admin");
                admin.setPassword("admin123");
                admin.setEmail("JosueEscobar000@hotmail.com");
                admin.setNombreCompleto("Administrador del Sistema");
                admin.setAdministrador(true);
                usuarioService.crearUsuario(admin);
                log.info("✅ Usuario administrador creado: admin / admin123");
            } else {
                log.info("✅ Usuario administrador ya existe");
            }
        } catch (Exception e) {
            log.error("❌ Error al crear usuario administrador", e);
        }
    }
}