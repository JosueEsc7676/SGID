package com.gidoc.gdoc.GDYBD.domain.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocenteDto {
    private String idpersonaA;
    private String depersona;
    private String nip;
    private String codigoInfra;

    // 🔹 Nuevos campos
    private String doc4;
    private String doc5;
    private String pens;
}
