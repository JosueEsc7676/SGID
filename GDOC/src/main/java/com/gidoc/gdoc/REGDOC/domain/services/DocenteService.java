package com.gidoc.gdoc.REGDOC.domain.services;

import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.List;

import com.gidoc.gdoc.GDYBD.repo.interfaces.DocenteRepository;
import com.gidoc.gdoc.GDYBD.domain.entities.Docente;

/**
 * Servicio REGDOC para búsquedas de Docente.
 * Anotado como "regdocDocenteService" para evitar conflicto con el servicio
 * DocenteService existente en com.gidoc.gdoc.GDYBD.domain.services.
 */
@Service("regdocDocenteService")
public class DocenteService {
    private final DocenteRepository repo;

    public DocenteService(DocenteRepository repo) {
        this.repo = repo;
    }

    public Optional<Docente> findByNip(String nip) {
        if (nip == null || nip.isBlank()) return Optional.empty();

        String raw = nip.trim();
        Optional<Docente> exactRaw = repo.findByIdpersonaA(raw);
        if (exactRaw.isPresent()) return exactRaw;

        String onlyDigits = raw.replaceAll("\\D+", "");
        if (onlyDigits != null && !onlyDigits.isBlank()) {
            Optional<Docente> exactDigits = repo.findByIdpersonaA(onlyDigits);
            if (exactDigits.isPresent()) return exactDigits;

            List<Docente> possibles = repo.findByIdpersonaAContainingIgnoreCase(onlyDigits);
            if (possibles != null && !possibles.isEmpty()) {
                return Optional.of(possibles.get(0));
            }
        }

        List<Docente> possiblesRaw = repo.findByIdpersonaAContainingIgnoreCase(raw);
        if (possiblesRaw != null && !possiblesRaw.isEmpty()) {
            return Optional.of(possiblesRaw.get(0));
        }

        Optional<Docente> exactIgnore = repo.findByIdpersonaAIgnoreCase(raw);
        if (exactIgnore.isPresent()) return exactIgnore;

        return Optional.empty();
    }
}
