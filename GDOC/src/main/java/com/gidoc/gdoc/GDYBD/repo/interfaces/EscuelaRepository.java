package com.gidoc.gdoc.GDYBD.repo.interfaces;

import com.gidoc.gdoc.GDYBD.domain.entities.Escuela;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EscuelaRepository extends JpaRepository<Escuela, Long> {

    /**
     * Buscar una escuela por su número.
     */
    Optional<Escuela> findByNumero(String numero);

    /**
     * Buscar una escuela por su código de infraestructura (cInfra).
     */
    Optional<Escuela> findBycInfra(String cInfra);

    /**
     * Eliminar una escuela por su código de infraestructura.
     */
    void deleteBycInfra(String cInfra);
}
