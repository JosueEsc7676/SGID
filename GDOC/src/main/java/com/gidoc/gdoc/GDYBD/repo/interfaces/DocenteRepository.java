package com.gidoc.gdoc.GDYBD.repo.interfaces;

import com.gidoc.gdoc.GDYBD.domain.entities.Docente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocenteRepository extends JpaRepository<Docente, Long> {

    /**
     * Buscar un docente por su idpersonaA (columna única).
     */
    Optional<Docente> findByIdpersonaA(String idpersonaA);

    /**
     * Elimina un docente por su idpersonaA.
     */
    void deleteByIdpersonaA(String idpersonaA);

    // Otros métodos útiles según necesites
}
