package com.gidoc.gdoc.Usuarios.domain.services;


import com.gidoc.gdoc.Usuarios.domain.dto.CambioPasswordDto;
import com.gidoc.gdoc.Usuarios.domain.dto.LoginRequest;
import com.gidoc.gdoc.Usuarios.domain.dto.LoginResponse;
import com.gidoc.gdoc.Usuarios.domain.entities.TokenRecuperacion;
import com.gidoc.gdoc.Usuarios.domain.entities.Usuario;
import com.gidoc.gdoc.Usuarios.repo.interfaces.TokenRecuperacionRepository;
import com.gidoc.gdoc.Usuarios.repo.interfaces.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final TokenRecuperacionRepository tokenRecuperacionRepository;
    private final SecurityService securityService;
    private final EmailService emailService;

    public Optional<Usuario> buscarPorUsername(String username) {
        return usuarioRepository.findActiveByUsername(username);
    }

    public LoginResponse autenticarUsuario(LoginRequest loginRequest) {
        try {
            Optional<Usuario> usuarioOpt = buscarPorUsername(loginRequest.getUsername());

            if (usuarioOpt.isEmpty()) {
                return LoginResponse.error("Usuario no encontrado");
            }

            Usuario usuario = usuarioOpt.get();

            if (!securityService.verificarPassword(loginRequest.getPassword(), usuario.getPassword())) {
                return LoginResponse.error("Contraseña incorrecta");
            }

            if (!usuario.getActivo()) {
                return LoginResponse.error("Usuario inactivo");
            }

            // Actualizar último acceso
            usuario.setFechaUltimoAcceso(LocalDateTime.now());
            usuarioRepository.save(usuario);

            String token = securityService.generarTokenAutenticacion();
            return LoginResponse.success(token, usuario.getNombreCompleto(), usuario.getAdministrador());

        } catch (Exception e) {
            log.error("Error en autenticación", e);
            return LoginResponse.error("Error interno del sistema");
        }
    }

    @Transactional
    public boolean solicitarRecuperacionPassword(String email) {
        try {
            Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);

            if (usuarioOpt.isEmpty()) {
                log.warn("Intento de recuperación para email no registrado: {}", email);
                // Por seguridad, no revelamos si el email existe o no
                return true;
            }

            Usuario usuario = usuarioOpt.get();

            // Invalidar tokens anteriores
            tokenRecuperacionRepository.invalidarTokensAnteriores(usuario.getId());

            // Crear nuevo token
            TokenRecuperacion token = new TokenRecuperacion();
            token.setUsuario(usuario);
            tokenRecuperacionRepository.save(token);

            // Enviar email
            return emailService.enviarEmailRecuperacion(usuario.getEmail(), usuario.getNombreCompleto(), token.getToken());

        } catch (Exception e) {
            log.error("Error en solicitud de recuperación de password", e);
            return false;
        }
    }

    @Transactional
    public boolean cambiarPassword(CambioPasswordDto cambioPasswordDto) {
        try {
            if (!cambioPasswordDto.getNuevaPassword().equals(cambioPasswordDto.getConfirmacionPassword())) {
                return false;
            }

            Optional<TokenRecuperacion> tokenOpt = tokenRecuperacionRepository
                    .findValidToken(cambioPasswordDto.getToken(), LocalDateTime.now());

            if (tokenOpt.isEmpty()) {
                return false;
            }

            TokenRecuperacion token = tokenOpt.get();
            Usuario usuario = token.getUsuario();

            // Actualizar password
            usuario.setPassword(securityService.encriptarPassword(cambioPasswordDto.getNuevaPassword()));
            usuarioRepository.save(usuario);

            // Marcar token como utilizado
            token.setUtilizado(true);
            tokenRecuperacionRepository.save(token);

            return true;

        } catch (Exception e) {
            log.error("Error al cambiar password", e);
            return false;
        }
    }

    @Transactional
    public Usuario crearUsuario(Usuario usuario) {
        try {
            // Verificar que el username no exista
            if (usuarioRepository.existsByUsername(usuario.getUsername())) {
                throw new RuntimeException("El nombre de usuario ya existe");
            }

            // Verificar que el email no exista
            if (usuarioRepository.existsByEmail(usuario.getEmail())) {
                throw new RuntimeException("El email ya está registrado");
            }

            // Encriptar password
            usuario.setPassword(securityService.encriptarPassword(usuario.getPassword()));

            return usuarioRepository.save(usuario);

        } catch (Exception e) {
            log.error("Error al crear usuario", e);
            throw e;
        }
    }

    public void limpiarTokensExpirados() {
        try {
            tokenRecuperacionRepository.eliminarTokensExpirados(LocalDateTime.now().minusDays(1));
            log.info("Tokens expirados eliminados");
        } catch (Exception e) {
            log.error("Error al limpiar tokens expirados", e);
        }
    }
}