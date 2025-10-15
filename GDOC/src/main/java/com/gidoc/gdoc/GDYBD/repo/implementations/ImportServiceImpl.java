package com.gidoc.gdoc.GDYBD.repo.implementations;

import com.gidoc.gdoc.GDYBD.domain.entities.Docente;
import com.gidoc.gdoc.GDYBD.domain.entities.Escuela;
import com.gidoc.gdoc.GDYBD.domain.services.ImportService;
import com.gidoc.gdoc.GDYBD.repo.interfaces.DocenteRepository;
import com.gidoc.gdoc.GDYBD.repo.interfaces.EscuelaRepository;
import com.gidoc.gdoc.GDYBD.web.utils.DbfReaderUtil;
import com.gidoc.gdoc.GDYBD.web.utils.FileReaderUtil;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ImportServiceImpl implements ImportService {

    private static final int BATCH_SIZE = 500;

    private final DocenteRepository docenteRepo;
    private final EscuelaRepository escuelaRepo;
    private final EntityManager em;

    @Autowired
    public ImportServiceImpl(DocenteRepository docenteRepo, EscuelaRepository escuelaRepo, EntityManager em) {
        this.docenteRepo = docenteRepo;
        this.escuelaRepo = escuelaRepo;
        this.em = em;
    }

    @Override
    @Transactional
    public void importarExcel(File archivo, String tipo) throws IOException {
        importFile(archivo, Type.valueOf(tipo.toUpperCase()), Mode.INSERT);
    }

    @Override
    @Transactional
    public void importarDBF(File archivo, String tipo) throws IOException {
        importFile(archivo, Type.valueOf(tipo.toUpperCase()), Mode.INSERT);
    }

    @Override
    @Transactional
    public void actualizarDesdeExcel(File archivo, String tipo) throws IOException {
        importFile(archivo, Type.valueOf(tipo.toUpperCase()), Mode.UPDATE);
    }

    @Override
    @Transactional
    public void actualizarDesdeDBF(File archivo, String tipo) throws IOException {
        importFile(archivo, Type.valueOf(tipo.toUpperCase()), Mode.UPDATE);
    }

    @Override
    @Transactional
    public void importFile(File file, Type type, Mode mode) throws IOException {
        String name = file.getName().toLowerCase();
        List<Map<String, String>> rows;

        if (name.endsWith(".dbf")) {
            rows = DbfReaderUtil.readAll(file, "Cp850");
        } else if (name.endsWith(".xls") || name.endsWith(".xlsx") || name.endsWith(".xlsm") || name.endsWith(".csv")) {
            rows = FileReaderUtil.readAll(file, type);
        } else {
            throw new IllegalArgumentException("Formato no soportado: " + name);
        }

        // Validación de tipo por fila
        List<Integer> filasInvalidas = validateRowsByType(rows, type);
        if (!filasInvalidas.isEmpty()) {
            throw new IllegalArgumentException(
                    "Archivo inválido para el tipo " + type + ". Filas incorrectas: " + filasInvalidas
            );
        }

        if (type == Type.DOCENTES) {
            if (mode == Mode.INSERT) importDocentes(rows);
            else updateDocentes(rows);
        } else {
            if (mode == Mode.INSERT) importEscuelas(rows);
            else updateEscuelas(rows);
        }
    }

    private List<Integer> validateRowsByType(List<Map<String, String>> rows, Type type) {
        List<Integer> invalidRows = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            Map<String, String> row = rows.get(i);
            boolean valid = false;
            Set<String> keysNormalized = new HashSet<>();
            for (String k : row.keySet()) {
                keysNormalized.add(k.replaceAll("\\s+", "").toLowerCase());
            }

            if (type == Type.DOCENTES) {
                valid = keysNormalized.stream().anyMatch(k -> k.contains("idpersona") || k.contains("depersona"));
            } else if (type == Type.ESCUELAS) {
                valid = keysNormalized.stream().anyMatch(k -> k.contains("cinfra") || k.contains("numero") || k.contains("nombre"));
            }

            if (!valid) invalidRows.add(Integer.valueOf(i + 1));
            ;
        }
        return invalidRows;
    }

    private void importDocentes(List<Map<String, String>> rows) {
        List<Docente> entidades = new ArrayList<>();
        for (Map<String, String> r : rows) {
            Docente d = new Docente();
            d.setIdpersonaA(firstNonEmpty(r, "idpersona_", "idpersona_a", "IDPERSONA_"));
            d.setDepersona(firstNonEmpty(r, "depersona", "nombre", "DEPERSONA"));
            d.setImportedAt(LocalDateTime.now());
            entidades.add(d);
        }
        saveInBatchesDocentes(entidades);
    }

    private void updateDocentes(List<Map<String, String>> rows) {
        for (Map<String, String> r : rows) {
            String idPersona = firstNonEmpty(r, "idpersona_", "idpersona_a", "IDPERSONA_");
            if (idPersona == null) continue;

            docenteRepo.findByIdpersonaA(idPersona).ifPresent(d -> {
                d.setDepersona(firstNonEmpty(r, "depersona", "nombre", "DEPERSONA"));
                d.setImportedAt(LocalDateTime.now());
                docenteRepo.save(d);
            });
        }
    }

    private void importEscuelas(List<Map<String, String>> rows) {
        List<Escuela> entidades = new ArrayList<>();
        for (Map<String, String> r : rows) {
            Escuela e = new Escuela();
            e.setCInfra(firstNonEmpty(r,"c infra","c_infra","C_INFRA","cinfra"));
            e.setNumero(firstNonEmpty(r,"n°","numero","no"));
            e.setDistrito(firstNonEmpty(r,"distrito","DISTRITO"));
            e.setNombre(firstNonEmpty(r,"nombre del centro escolar","nombre"));
            e.setMunicipio(firstNonEmpty(r,"municipio","MUNICIPIO"));
            e.setImportedAt(LocalDateTime.now());
            entidades.add(e);
        }
        saveInBatchesEscuelas(entidades);
    }

    private void updateEscuelas(List<Map<String, String>> rows) {
        for (Map<String, String> r : rows) {
            String numero = firstNonEmpty(r,"n°","numero","no");
            if (numero == null) continue;

            escuelaRepo.findByNumero(numero).ifPresent(e -> {
                e.setCInfra(firstNonEmpty(r,"c infra","c_infra","C_INFRA","cinfra"));
                e.setDistrito(firstNonEmpty(r,"distrito","DISTRITO"));
                e.setNombre(firstNonEmpty(r,"nombre del centro escolar","nombre"));
                e.setMunicipio(firstNonEmpty(r,"municipio","MUNICIPIO"));
                e.setImportedAt(LocalDateTime.now());
                escuelaRepo.save(e);
            });
        }
    }

    private void saveInBatchesDocentes(List<Docente> entidades) {
        for (int i = 0; i < entidades.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, entidades.size());
            docenteRepo.saveAll(entidades.subList(i, end));
            em.flush();
            em.clear();
        }
    }

    private void saveInBatchesEscuelas(List<Escuela> entidades) {
        for (int i = 0; i < entidades.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, entidades.size());
            escuelaRepo.saveAll(entidades.subList(i, end));
            em.flush();
            em.clear();
        }
    }

    @Override
    public List<Docente> obtenerTodosDocentes() {
        return docenteRepo.findAll();
    }

    @Override
    public List<Escuela> obtenerTodasEscuelas() {
        return escuelaRepo.findAll();
    }

    private String firstNonEmpty(Map<String, String> map, String... candidates) {
        for (String cand : candidates) {
            String candNorm = cand.replaceAll("\\s+", "").toLowerCase();
            for (String key : map.keySet()) {
                String keyNorm = key.replaceAll("\\s+", "").toLowerCase();
                if (keyNorm.equals(candNorm)) {
                    String v = map.get(key);
                    if (v != null && !v.isBlank()) return v.trim();
                }
            }
        }
        return null;
    }

    // ===== CRUD ESCUELAS =====
    @Transactional
    public Escuela agregarEscuela(Escuela escuela) {
        escuela.setImportedAt(LocalDateTime.now());
        return escuelaRepo.save(escuela);
    }

    @Transactional
    public Escuela editarEscuela(Escuela escuela) {
        escuela.setImportedAt(LocalDateTime.now());
        return escuelaRepo.save(escuela);
    }

    @Transactional
    public void eliminarEscuela(Escuela escuela) {
        escuelaRepo.delete(escuela);
    }
}
