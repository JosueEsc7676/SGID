package com.gidoc.gdoc.Usuarios.config;

import javafx.stage.Stage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class JavaFXConfig {

    @Bean
    @Lazy
    public Stage primaryStage() {
        return new Stage();
    }
}