package com.gidoc.gdoc.GDYBD.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "escuelas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class Escuela {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id; // clave primaria auto_increment

    @Column(name = "c_infra", length = 100, nullable = false, unique = true)
    private String cInfra; // clave única para evitar null

    @Column(name = "numero", nullable = false)
    private String numero;

    @Column(name = "distrito")
    private String distrito;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "municipio")
    private String municipio;

    @Column(name = "imported_at")
    private LocalDateTime importedAt;

    // Constructor auxiliar para importaciones
    public Escuela(String cInfra, String numero, String distrito, String nombre, String municipio, LocalDateTime importedAt) {
        this.cInfra = cInfra;
        this.numero = numero;
        this.distrito = distrito;
        this.nombre = nombre;
        this.municipio = municipio;
        this.importedAt = importedAt;
    }
}
