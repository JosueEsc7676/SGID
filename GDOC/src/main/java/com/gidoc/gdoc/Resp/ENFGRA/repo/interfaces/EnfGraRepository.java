package com.gidoc.gdoc.Resp.ENFGRA.repo.interfaces;

import com.gidoc.gdoc.Resp.ENFGRA.domain.entities.EnfGra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnfGraRepository extends JpaRepository<EnfGra, Long> {
    // Puedes agregar métodos personalizados si lo necesitas más adelante
}
