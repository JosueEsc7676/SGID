package com.gidoc.gdoc.REGDOC.domain.services;

import org.springframework.stereotype.Service;
import com.gidoc.gdoc.REGDOC.domain.dto.RegistroDTO;
import com.gidoc.gdoc.REGDOC.domain.entities.RegistroMes;
import com.gidoc.gdoc.REGDOC.repo.interfaces.RegistroMesRepository;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class RegistroService {
    private final RegistroMesRepository repo;

    public RegistroService(RegistroMesRepository repo) {
        this.repo = repo;
    }

    public RegistroMes save(RegistroDTO dto) {
        int dias = 0;
        if (dto.getDesde() != null && dto.getHasta() != null) {
            long diff = ChronoUnit.DAYS.between(dto.getDesde(), dto.getHasta());
            dias = (int) diff + 1;
            if (dias < 0) dias = 0;
        }

        RegistroMes r = RegistroMes.builder()
                .cInfra(dto.getCInfra())
                .nip(dto.getNip())
                .nombreDocente(dto.getNombreDocente())
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
}
