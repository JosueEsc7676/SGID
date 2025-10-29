package com.gidoc.gdoc.Resp.ENFGRA.domain.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnfGraDto{
    private String cInfra;
    private String nip;
    private String nombre;
    private String lugarTrabajo;
    private String dias;
    private String hora;
    private String desde;
    private String hasta;
    private String observaciones;
    private String fechaRecibido;
}
