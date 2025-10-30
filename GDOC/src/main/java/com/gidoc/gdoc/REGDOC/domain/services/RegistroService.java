package com.gidoc.gdoc.REGDOC.domain.services;

import org.springframework.stereotype.Service;
import com.gidoc.gdoc.REGDOC.domain.dto.RegistroDTO;
import com.gidoc.gdoc.REGDOC.domain.entities.RegistroMes;
import com.gidoc.gdoc.REGDOC.repo.interfaces.RegistroMesRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class RegistroService {
    private final RegistroMesRepository repo;

    public RegistroService(RegistroMesRepository repo) {
        this.repo = repo;
    }

    public RegistroMes save(RegistroDTO dto) {
        int dias = calcularDias(dto.getDesde(), dto.getHasta());

        RegistroMes r = RegistroMes.builder()
                .cInfra(dto.getCInfra())
                .nip(dto.getNip())
                .nombreDocente(dto.getNombreDocente())
                .mes(dto.getMes())
                .doc4(dto.getDoc4())   // 👈 Nuevo
                .doc5(dto.getDoc5())   // 👈 Nuevo
                .pens(dto.getPens())
                .centroEducativo(dto.getCentroEducativo())
                .municipio(dto.getMunicipio())
                .distrito(dto.getDistrito())
                .dias(dias)
                .horas(dto.getHoras())
                .desde(dto.getDesde())
                .hasta(dto.getHasta())
                .patologia(dto.getPatologia())
                .observaciones(dto.getObservaciones())
                .mes(dto.getMes())
                .createdAt(LocalDateTime.now())
                .build();

        return repo.save(r);
    }

    public List<RegistroMes> findByMes(String mes) {
        if (mes == null) return List.of();
        return repo.findByMes(mes.toLowerCase().trim());
    }

    public Optional<RegistroMes> findById(Long id) {
        return repo.findById(id);
    }

    public RegistroMes update(Long id, RegistroDTO dto) {
        return repo.findById(id).map(existing -> {
            existing.setCInfra(dto.getCInfra());
            existing.setNip(dto.getNip());
            existing.setNombreDocente(dto.getNombreDocente());
            existing.setDoc4(dto.getDoc4());
            existing.setDoc5(dto.getDoc5());
            existing.setPens(dto.getPens());

            existing.setCentroEducativo(dto.getCentroEducativo());
            existing.setMunicipio(dto.getMunicipio());
            existing.setDistrito(dto.getDistrito());
            existing.setHoras(dto.getHoras());
            existing.setDesde(dto.getDesde());
            existing.setHasta(dto.getHasta());
            existing.setDias(calcularDias(dto.getDesde(), dto.getHasta()));
            existing.setPatologia(dto.getPatologia());
            existing.setObservaciones(dto.getObservaciones());
            existing.setMes(dto.getMes());
            // note: don't change createdAt to preserve original timestamp
            return repo.save(existing);
        }).orElseGet(() -> {
            // si no existe, crear uno nuevo (opcional)
            return save(dto);
        });
    }

    public void deleteById(Long id) {
        repo.deleteById(id);
    }

    private int calcularDias(java.time.LocalDate desde, java.time.LocalDate hasta) {
        if (desde == null || hasta == null) return 0;
        long diff = ChronoUnit.DAYS.between(desde, hasta);
        int dias = (int) diff + 1;
        return Math.max(0, dias);
    }


}

