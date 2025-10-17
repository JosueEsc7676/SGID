package com.gidoc.gdoc.GDYBD.repo.interfaces;

import com.gidoc.gdoc.GDYBD.domain.entities.Docente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocenteRepository extends JpaRepository<Docente, Long> {

    /**
     * Buscar un docente por su idpersonaA (columna única).
     */
    Optional<Docente> findByIdpersonaA(String idpersonaA);

    /**
     * Buscar por coincidencia parcial (contiene).
     * Útil cuando la columna guarda prefijos como "NR  0307381".
     */
    List<Docente> findByIdpersonaAContainingIgnoreCase(String fragment);

    /**
     * Buscar por exacto ignorando mayúsculas/minúsculas.
     */
    Optional<Docente> findByIdpersonaAIgnoreCase(String idpersonaA);
    /**
     * Elimina un docente por su idpersonaA.
     */
    void deleteByIdpersonaA(String idpersonaA);

    // Otros métodos útiles según necesites
}
