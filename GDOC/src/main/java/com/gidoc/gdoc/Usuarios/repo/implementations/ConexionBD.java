package com.gidoc.gdoc.Usuarios.repo.implementations;//package com.gdoc.gdoc.Usuarios.repo.implementations;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.SQLException;
//
//@Component
//@Slf4j
//public class ConexionBD {
//
//    private static final String URL = "jdbc:h2:file:./database/gdoc;DB_CLOSE_ON_EXIT=FALSE;AUTO_RECONNECT=TRUE";
//    private static final String USER = "sa";
//    private static final String PASSWORD = "";
//
//    public Connection obtenerConexion() throws SQLException {
//        try {
//            return DriverManager.getConnection(URL, USER, PASSWORD);
//        } catch (SQLException e) {
//            log.error("Error al conectar con la base de datos", e);
//            throw e;
//        }
//    }
//
//    public void probarConexion() {
//        try (Connection conn = obtenerConexion()) {
//            log.info("Conexión a la base de datos establecida correctamente");
//        } catch (SQLException e) {
//            log.error("Error al probar la conexión a la base de datos", e);
//        }
//    }
//}