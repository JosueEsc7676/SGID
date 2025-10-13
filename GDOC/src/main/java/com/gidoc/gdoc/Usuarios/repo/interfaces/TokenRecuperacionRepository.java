package com.gidoc.gdoc.Usuarios.repo.interfaces;

import com.gidoc.gdoc.Usuarios.domain.entities.TokenRecuperacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TokenRecuperacionRepository extends JpaRepository<TokenRecuperacion, Long> {
    Optional<TokenRecuperacion> findByTokenAndUtilizadoFalse(String token);

    @Query("SELECT t FROM TokenRecuperacion t WHERE t.token = :token AND t.utilizado = false AND t.fechaExpiracion > :now")
    Optional<TokenRecuperacion> findValidToken(@Param("token") String token, @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE TokenRecuperacion t SET t.utilizado = true WHERE t.usuario.id = :usuarioId AND t.utilizado = false")
    void invalidarTokensAnteriores(@Param("usuarioId") Long usuarioId);

    @Modifying
    @Query("DELETE FROM TokenRecuperacion t WHERE t.fechaExpiracion < :fecha")
    void eliminarTokensExpirados(@Param("fecha") LocalDateTime fecha);
}