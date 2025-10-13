package com.gidoc.gdoc.Usuarios.domain.dto;

import lombok.Data;

@Data
public class CambioPasswordDto {
    private String token;
    private String nuevaPassword;
    private String confirmacionPassword;
}