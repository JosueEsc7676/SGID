package com.gidoc.gdoc.Usuarios.domain.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private boolean success;
    private String message;
    private String token;
    private String nombreUsuario;
    private Boolean esAdministrador;

    public static LoginResponse success(String token, String nombreUsuario, Boolean esAdministrador) {
        LoginResponse response = new LoginResponse();
        response.setSuccess(true);
        response.setMessage("Login exitoso");
        response.setToken(token);
        response.setNombreUsuario(nombreUsuario);
        response.setEsAdministrador(esAdministrador);
        return response;
    }

    public static LoginResponse error(String message) {
        LoginResponse response = new LoginResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }
}