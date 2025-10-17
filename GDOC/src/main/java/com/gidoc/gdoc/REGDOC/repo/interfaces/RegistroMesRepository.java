package com.gidoc.gdoc.REGDOC.repo.interfaces;

import com.gidoc.gdoc.REGDOC.domain.entities.RegistroMes;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RegistroMesRepository extends JpaRepository<RegistroMes, Long> {
    List<RegistroMes> findByMes(String mes);
}
