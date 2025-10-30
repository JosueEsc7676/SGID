package com.gidoc.gdoc.REGDOC.domain.services;

import com.gidoc.gdoc.REGDOC.domain.dto.RegistroDTO;
import com.gidoc.gdoc.REGDOC.domain.entities.RegistroMes;
import com.gidoc.gdoc.REGDOC.repo.interfaces.RegistroMesRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class HistorialMensualService {

    private final RegistroMesRepository repo;

    public HistorialMensualService(RegistroMesRepository repo) {
        this.repo = repo;
    }

    public List<RegistroMes> buscarRegistros(String mes, String nip, String cInfra) {
        if (mes != null && !mes.isBlank()) {
            if (nip != null && !nip.isBlank()) return repo.findByMesAndNipIgnoreCase(mes, nip);
            if (cInfra != null && !cInfra.isBlank()) return repo.findByMesAndCInfraIgnoreCase(mes, cInfra);
            return repo.findByMesIgnoreCase(mes);
        } else {
            if (nip != null && !nip.isBlank()) return repo.findByNip(nip);
            if (cInfra != null && !cInfra.isBlank()) return repo.findByCInfraIgnoreCase(cInfra);
            // ✅ NUEVO: Si no hay filtros, mostrar todos los registros ordenados por mes
            return repo.findAllOrderByMes();
        }
    }

    // ✅ NUEVO: Método para obtener la lista de meses disponibles
    public List<String> obtenerMesesDisponibles() {
        return repo.findAllMeses();
    }

    public RegistroMes save(RegistroDTO dto) {
        int dias = calcularDias(dto.getDesde(), dto.getHasta());
        RegistroMes registro = RegistroMes.builder()
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
                .doc4(dto.getDoc4())
                .doc5(dto.getDoc5())
                .pens(dto.getPens())
                .build();
        return repo.save(registro);
    }

    public Optional<RegistroMes> findById(Long id) {
        return repo.findById(id);
    }

    public RegistroMes update(Long id, RegistroDTO dto) {
        return repo.findById(id).map(existing -> {
            existing.setCInfra(dto.getCInfra());
            existing.setNip(dto.getNip());
            existing.setNombreDocente(dto.getNombreDocente());
            existing.setCentroEducativo(dto.getCentroEducativo());
            existing.setMunicipio(dto.getMunicipio());
            existing.setDistrito(dto.getDistrito());
            existing.setHoras(dto.getHoras());
            existing.setDesde(dto.getDesde());
            existing.setHasta(dto.getHasta());
            existing.setDias(calcularDias(dto.getDesde(), dto.getHasta()));
            existing.setPatologia(dto.getPatologia());
            existing.setObservaciones(dto.getObservaciones());
            existing.setDoc4(dto.getDoc4());
            existing.setDoc5(dto.getDoc5());
            existing.setPens(dto.getPens());


            existing.setMes(dto.getMes());

            return repo.save(existing);
        }).orElseGet(() -> save(dto));
    }

    public void deleteById(Long id) {
        repo.deleteById(id);
    }

    private int calcularDias(java.time.LocalDate desde, java.time.LocalDate hasta) {
        if (desde == null || hasta == null) return 0;
        long diff = ChronoUnit.DAYS.between(desde, hasta);
        return (int) Math.max(0, diff + 1);
    }
}