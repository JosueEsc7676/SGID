package com.gidoc.gdoc.REGDOC.domain.dto;

import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroDTO {
    private String cInfra;
    private String nip;
    private String nombreDocente;
    private String centroEducativo;
    private String municipio;
    private String distrito;
    private Integer horas;
    private LocalDate desde;
    private LocalDate hasta;
    private String patologia;
    private String observaciones;
    private String mes;
}
