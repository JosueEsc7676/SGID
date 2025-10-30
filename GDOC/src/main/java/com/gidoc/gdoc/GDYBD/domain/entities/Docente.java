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
    private String idpersonaA;

    @Column(name = "depersona", length = 60, nullable = false)
    private String depersona;

    // 🔹 Nuevos campos para importación
    @Column(name = "doc4", length = 100)
    private String doc4;

    @Column(name = "doc5", length = 100)
    private String doc5;

    @Column(name = "pens", length = 100)
    private String pens;

    @Column(name = "imported_at")
    private LocalDateTime importedAt;

    // Constructor auxiliar para importaciones
    public Docente(String idpersonaA, String depersona, String doc4, String doc5, String pens, LocalDateTime importedAt) {
        this.idpersonaA = idpersonaA;
        this.depersona = depersona;
        this.doc4 = doc4;
        this.doc5 = doc5;
        this.pens = pens;
        this.importedAt = importedAt;
    }
}
