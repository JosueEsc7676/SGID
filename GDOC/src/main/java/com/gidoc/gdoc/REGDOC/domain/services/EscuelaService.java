package com.gidoc.gdoc.REGDOC.domain.services;

import org.springframework.stereotype.Service;
import java.util.Optional;
import com.gidoc.gdoc.GDYBD.repo.interfaces.EscuelaRepository;
// Ajusta import si es necesario:
import com.gidoc.gdoc.GDYBD.domain.entities.Escuela;

@Service
public class EscuelaService {
    private final EscuelaRepository repo;

    public EscuelaService(EscuelaRepository repo) {
        this.repo = repo;
    }

    public Optional<Escuela> findByCInfra(String cInfra) {
        if (cInfra == null || cInfra.isBlank()) return Optional.empty();
        return repo.findBycInfra(cInfra.trim());
    }
}
