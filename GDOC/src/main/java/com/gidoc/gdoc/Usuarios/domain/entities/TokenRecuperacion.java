package com.gidoc.gdoc.Usuarios.domain.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tokens_recuperacion")
@Data
public class TokenRecuperacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDateTime fechaExpiracion;

    @Column(nullable = false)
    private Boolean utilizado = false;

    @PrePersist
    protected void generarToken() {
        if (token == null) {
            token = UUID.randomUUID().toString();
        }
        if (fechaExpiracion == null) {
            fechaExpiracion = LocalDateTime.now().plusHours(24);
        }
    }

    public boolean esValido() {
        return !utilizado && LocalDateTime.now().isBefore(fechaExpiracion);
    }
}