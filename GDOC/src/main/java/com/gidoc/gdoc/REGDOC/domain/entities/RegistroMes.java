package com.gidoc.gdoc.REGDOC.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "registro_mes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroMes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Campos del formulario
    private String cInfra;
    private String nip; // guardamos tal cual (varchar)
    private String nombreDocente;
    private String centroEducativo;
    private String municipio;
    private String distrito;
    private Integer dias;
    private Integer horas;
    private LocalDate desde;
    private LocalDate hasta;
    private String patologia;
    private String observaciones;

    private String mes; // "enero", "febrero", etc.

    private LocalDateTime createdAt;
}
