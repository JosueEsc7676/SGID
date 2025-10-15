package com.gidoc.gdoc.Usuarios.web.controllers;

import com.gidoc.gdoc.Usuarios.domain.entities.Usuario;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * UserSession como bean singleton de Spring para mantener la sesión del usuario.
 */
@Component
@Scope(value = "singleton", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class UserSession {

    private Usuario usuarioActual;

    public void setUsuarioActual(Usuario usuario) {
        this.usuarioActual = usuario;
    }

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public void clear() {
        usuarioActual = null;
    }
}
