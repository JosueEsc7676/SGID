package com.gidoc.gdoc.Usuarios.repo.interfaces;

import com.gidoc.gdoc.Usuarios.domain.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);
    Optional<Usuario> findByEmail(String email);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);

    @Query("SELECT u FROM Usuario u WHERE u.activo = true AND u.username = :username")
    Optional<Usuario> findActiveByUsername(@Param("username") String username);
}