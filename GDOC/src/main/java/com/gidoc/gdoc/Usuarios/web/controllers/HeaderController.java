//package com.gidoc.gdoc.Usuarios.web.controllers;
//
//import javafx.fxml.FXML;
//import javafx.scene.control.Button;
//import javafx.stage.Stage;
//
//public class HeaderController {
//
//    @FXML private Button btnClose;
//    @FXML private Button btnMinimize;
//    @FXML private Button btnMaximize;
//
//    private double xOffset = 0;
//    private double yOffset = 0;
//
//    @FXML
//    public void initialize() {
//        // Cerrar
//        btnClose.setOnAction(e -> {
//            Stage stage = (Stage) btnClose.getScene().getWindow();
//            stage.close();
//        });
//
//        // Minimizar
//        btnMinimize.setOnAction(e -> {
//            Stage stage = (Stage) btnMinimize.getScene().getWindow();
//            stage.setIconified(true);
//        });
//
//        // Maximizar / Restaurar
//        btnMaximize.setOnAction(e -> {
//            Stage stage = (Stage) btnMaximize.getScene().getWindow();
//            stage.setMaximized(!stage.isMaximized());
//        });
//
//        // Permitir arrastrar ventana
//        btnClose.getScene().getRoot().setOnMousePressed(event -> {
//            Stage stage = (Stage) btnClose.getScene().getWindow();
//            xOffset = event.getSceneX();
//            yOffset = event.getSceneY();
//        });
//
//        btnClose.getScene().getRoot().setOnMouseDragged(event -> {
//            Stage stage = (Stage) btnClose.getScene().getWindow();
//            if (!stage.isMaximized()) {
//                stage.setX(event.getScreenX() - xOffset);
//                stage.setY(event.getScreenY() - yOffset);
//            }
//        });
//    }
//}
