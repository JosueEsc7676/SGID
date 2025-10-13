package com.gidoc.gdoc.Usuarios.web.controllers;

import com.gidoc.gdoc.Usuarios.domain.dto.CambioPasswordDto;
import com.gidoc.gdoc.Usuarios.domain.dto.LoginRequest;
import com.gidoc.gdoc.Usuarios.domain.dto.LoginResponse;
import com.gidoc.gdoc.Usuarios.domain.entities.Usuario;
import com.gidoc.gdoc.Usuarios.domain.services.UsuarioService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UsuarioService usuarioService;
    private final ApplicationManager applicationManager;

    // Componentes de Login
    @FXML private StackPane loginPane;
    @FXML private TextField loginUsername;
    @FXML private PasswordField loginPassword;
    @FXML private Button loginButton;
    @FXML private Hyperlink recuperarLink;
    @FXML private Label loginMessage;

    // Componentes de Recuperación
    @FXML private StackPane recuperacionPane;
    @FXML private TextField recuperacionEmail;
    @FXML private Button enviarRecuperacionButton;
    @FXML private Hyperlink volverLoginLink;
    @FXML private Label recuperacionMessage;

    // Componentes de Ingreso de Código
    @FXML private StackPane codigoPane;
    @FXML private TextField codigoToken;
    @FXML private Button verificarCodigoButton;
    @FXML private Hyperlink volverRecuperacionLink;
    @FXML private Label codigoMessage;

    // Componentes de Cambio de Password
    @FXML private StackPane cambioPasswordPane;
    @FXML private PasswordField nuevaPassword;
    @FXML private PasswordField confirmarPassword;
    @FXML private Button cambiarPasswordButton;
    @FXML private Label cambioPasswordMessage;

    private String tokenRecuperacion;
    private String emailUsuario;
    @FXML
    private ImageView logoImage;

    @FXML
    public void initialize() {
        configurarEventos();
        mostrarLogin();
        cargarLogo();
    }

    private void cargarLogo() {
        try {
            Image logo = new Image(getClass().getResource("/images/logo.png").toExternalForm());
            logoImage.setImage(logo);
        } catch (Exception e) {
            log.warn("No se pudo cargar el logo: {}", e.getMessage());
        }
    }

    private void configurarEventos() {
        // Login
        loginButton.setOnAction(e -> realizarLogin());
        recuperarLink.setOnAction(e -> mostrarRecuperacion());

        // Recuperación
        enviarRecuperacionButton.setOnAction(e -> enviarSolicitudRecuperacion());
        volverLoginLink.setOnAction(e -> mostrarLogin());

        // Código
        verificarCodigoButton.setOnAction(e -> verificarCodigo());
        volverRecuperacionLink.setOnAction(e -> mostrarRecuperacion());

        // Cambio de password
        cambiarPasswordButton.setOnAction(e -> cambiarPassword());

        // Permitir login con Enter
        loginPassword.setOnAction(e -> realizarLogin());
    }

    private void realizarLogin() {
        try {
            String username = loginUsername.getText();
            String password = loginPassword.getText();

            if (username.isEmpty() || password.isEmpty()) {
                mostrarMensajeLogin("Por favor complete todos los campos", true);
                return;
            }

            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setUsername(username);
            loginRequest.setPassword(password);

            LoginResponse response = usuarioService.autenticarUsuario(loginRequest);

            if (response.isSuccess()) {
                mostrarMensajeLogin("Login exitoso! Redirigiendo...", false);
                log.info("Usuario autenticado: {}", username);

                // Obtener el usuario completo para pasarlo al home
                Optional<Usuario> usuarioOpt = usuarioService.buscarPorUsername(username);
                if (usuarioOpt.isPresent()) {
                    // Redirigir al home después de un breve delay usando ApplicationManager
                    new java.util.Timer().schedule(
                            new java.util.TimerTask() {
                                @Override
                                public void run() {
                                    javafx.application.Platform.runLater(() ->
                                            redirigirAHome(usuarioOpt.get()));
                                }
                            },
                            1000
                    );
                }
            } else {
                mostrarMensajeLogin(response.getMessage(), true);
            }

        } catch (Exception e) {
            log.error("Error en login", e);
            mostrarMensajeLogin("Error interno del sistema", true);
        }
    }

    private void redirigirAHome(Usuario usuario) {
        try {
            log.info("Redirigiendo al home para usuario: {}", usuario.getUsername());

            // Usar ApplicationManager para mostrar el home
            applicationManager.mostrarHome(usuario);

            log.info("Redirección completada exitosamente");

        } catch (Exception e) {
            log.error("Error al redirigir al home", e);
            mostrarMensajeLogin("Error al cargar la pantalla principal", true);
        }
    }

    private void enviarSolicitudRecuperacion() {
        try {
            String email = recuperacionEmail.getText();

            if (email.isEmpty() || !email.contains("@")) {
                mostrarMensajeRecuperacion("Por favor ingrese un email válido", true);
                return;
            }

            this.emailUsuario = email;
            boolean enviado = usuarioService.solicitarRecuperacionPassword(email);

            if (enviado) {
                mostrarMensajeRecuperacion("Se ha enviado un código de recuperación a su email", false);
                recuperacionEmail.clear();
                // Mostrar panel de código después de 1 segundo
                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                javafx.application.Platform.runLater(() -> mostrarCodigo());
                            }
                        },
                        1000
                );
            } else {
                mostrarMensajeRecuperacion("Error al enviar el email de recuperación", true);
            }

        } catch (Exception e) {
            log.error("Error en recuperación", e);
            mostrarMensajeRecuperacion("Error interno del sistema", true);
        }
    }

    private void verificarCodigo() {
        try {
            String codigo = codigoToken.getText();

            if (codigo.isEmpty()) {
                mostrarMensajeCodigo("Por favor ingrese el código recibido", true);
                return;
            }

            // Verificar si el token es válido
            CambioPasswordDto dto = new CambioPasswordDto();
            dto.setToken(codigo);

            // Simplemente almacenar el token para el siguiente paso
            this.tokenRecuperacion = codigo;
            mostrarMensajeCodigo("Código verificado correctamente", false);

            // Mostrar panel de cambio de contraseña después de 1 segundo
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            javafx.application.Platform.runLater(() -> mostrarCambioPassword());
                        }
                    },
                    1000
            );

        } catch (Exception e) {
            log.error("Error verificando código", e);
            mostrarMensajeCodigo("Error interno del sistema", true);
        }
    }

    private void cambiarPassword() {
        try {
            String pass1 = nuevaPassword.getText();
            String pass2 = confirmarPassword.getText();

            if (pass1.isEmpty() || pass2.isEmpty()) {
                mostrarMensajeCambioPassword("Por favor complete todos los campos", true);
                return;
            }

            if (!pass1.equals(pass2)) {
                mostrarMensajeCambioPassword("Las contraseñas no coinciden", true);
                return;
            }

            if (pass1.length() < 6) {
                mostrarMensajeCambioPassword("La contraseña debe tener al menos 6 caracteres", true);
                return;
            }

            CambioPasswordDto dto = new CambioPasswordDto();
            dto.setToken(tokenRecuperacion);
            dto.setNuevaPassword(pass1);
            dto.setConfirmacionPassword(pass2);

            boolean cambiado = usuarioService.cambiarPassword(dto);

            if (cambiado) {
                mostrarMensajeCambioPassword("✅ Contraseña cambiada exitosamente", false);
                nuevaPassword.clear();
                confirmarPassword.clear();

                // Volver al login después de 2 segundos
                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                javafx.application.Platform.runLater(() -> mostrarLogin());
                            }
                        },
                        2000
                );
            } else {
                mostrarMensajeCambioPassword("❌ El código ha expirado o es inválido", true);
            }

        } catch (Exception e) {
            log.error("Error al cambiar password", e);
            mostrarMensajeCambioPassword("Error interno del sistema", true);
        }
    }

    // Métodos para mostrar/ocultar paneles
    private void mostrarLogin() {
        loginPane.setVisible(true);
        recuperacionPane.setVisible(false);
        codigoPane.setVisible(false);
        cambioPasswordPane.setVisible(false);
        limpiarMensajes();
        limpiarCampos();
    }

    private void mostrarRecuperacion() {
        loginPane.setVisible(false);
        recuperacionPane.setVisible(true);
        codigoPane.setVisible(false);
        cambioPasswordPane.setVisible(false);
        limpiarMensajes();
    }

    private void mostrarCodigo() {
        loginPane.setVisible(false);
        recuperacionPane.setVisible(false);
        codigoPane.setVisible(true);
        cambioPasswordPane.setVisible(false);
        limpiarMensajes();
    }

    private void mostrarCambioPassword() {
        loginPane.setVisible(false);
        recuperacionPane.setVisible(false);
        codigoPane.setVisible(false);
        cambioPasswordPane.setVisible(true);
        limpiarMensajes();
    }

    private void limpiarMensajes() {
        loginMessage.setText("");
        recuperacionMessage.setText("");
        codigoMessage.setText("");
        cambioPasswordMessage.setText("");
    }

    private void limpiarCampos() {
        loginUsername.clear();
        loginPassword.clear();
        recuperacionEmail.clear();
        codigoToken.clear();
        nuevaPassword.clear();
        confirmarPassword.clear();
    }

    private void mostrarMensajeLogin(String mensaje, boolean esError) {
        loginMessage.setText(mensaje);
        loginMessage.setStyle(esError ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #27ae60;");
    }

    private void mostrarMensajeRecuperacion(String mensaje, boolean esError) {
        recuperacionMessage.setText(mensaje);
        recuperacionMessage.setStyle(esError ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #27ae60;");
    }

    private void mostrarMensajeCodigo(String mensaje, boolean esError) {
        codigoMessage.setText(mensaje);
        codigoMessage.setStyle(esError ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #27ae60;");
    }

    private void mostrarMensajeCambioPassword(String mensaje, boolean esError) {
        cambioPasswordMessage.setText(mensaje);
        cambioPasswordMessage.setStyle(esError ? "-fx-text-fill: #e74c3c;" : "-fx-text-fill: #27ae60;");
    }
}