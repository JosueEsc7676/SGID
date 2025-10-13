package com.gidoc.gdoc.Usuarios.domain.dto;

import lombok.Data;

@Data
public class UsuarioDto {
    private Long id;
    private String username;
    private String email;
    private String nombreCompleto;
    private Boolean activo;
    private Boolean administrador;
}
