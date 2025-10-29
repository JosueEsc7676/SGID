package com.gidoc.gdoc.Resp.ENFGRA.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "enfermedad_grave")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class EnfGra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // 🔗 Relación con escuela por C_INFRA
    @Column(name = "c_infra", length = 100, nullable = false)
    private String cInfra;

    // 🔗 Relación con docente por NIP
    @Column(name = "nip", length = 100, nullable = false)
    private String nip;

    @Column(name = "nombre", length = 255, nullable = false)
    private String nombre;

    @Column(name = "lugar_trabajo", length = 255, nullable = false)
    private String lugarTrabajo;

    @Column(name = "dias", length = 20)
    private String dias;

    @Column(name = "hora", length = 20)
    private String hora;

    @Column(name = "desde")
    private LocalDate desde;

    @Column(name = "hasta")
    private LocalDate hasta;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "fecha_recibido")
    private LocalDate fechaRecibido;

    @Column(name = "imported_at")
    private LocalDateTime importedAt;

    // ✅ Método auxiliar para calcular días automáticamente
    public void calcularDias() {
        if (desde != null && hasta != null && !hasta.isBefore(desde)) {
            long cantidadDias = ChronoUnit.DAYS.between(desde, hasta) + 1;
            this.dias = String.valueOf(cantidadDias);
        } else {
            this.dias = "0";
        }
    }
}
