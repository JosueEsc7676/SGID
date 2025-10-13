package com.gidoc.gdoc.GDYBD.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "docentes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class Docente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id; // clave primaria auto_increment

    @Column(name = "idpersona_a", length = 100, nullable = false, unique = true)
    private String idpersonaA; // clave única para evitar null

    @Column(name = "depersona", length = 60, nullable = false)
    private String depersona;

    @Column(name = "imported_at")
    private LocalDateTime importedAt;

    // Constructor auxiliar para importaciones
    public Docente(String idpersonaA, String depersona, LocalDateTime importedAt) {
        this.idpersonaA = idpersonaA;
        this.depersona = depersona;
        this.importedAt = importedAt;
    }
}
