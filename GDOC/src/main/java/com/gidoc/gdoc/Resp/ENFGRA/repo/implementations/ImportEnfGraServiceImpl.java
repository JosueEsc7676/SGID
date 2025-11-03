package com.gidoc.gdoc.Resp.ENFGRA.repo.implementations;

import com.gidoc.gdoc.Resp.ENFGRA.domain.entities.EnfGra;
import com.gidoc.gdoc.Resp.ENFGRA.domain.services.ImportEnfGraService;
import com.gidoc.gdoc.GDYBD.domain.entities.Docente;
import com.gidoc.gdoc.GDYBD.domain.entities.Escuela;
import com.gidoc.gdoc.GDYBD.repo.interfaces.DocenteRepository;
import com.gidoc.gdoc.GDYBD.repo.interfaces.EscuelaRepository;
import com.gidoc.gdoc.Resp.ENFGRA.repo.interfaces.EnfGraRepository;
import com.gidoc.gdoc.Resp.ENFGRA.repo.interfaces.EnfGraRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ImportEnfGraServiceImpl implements ImportEnfGraService {

    private final EscuelaRepository escuelaRepo;
    private final DocenteRepository docenteRepo;
    private final EnfGraRepository enfermedadRepo;

    @Autowired
    public ImportEnfGraServiceImpl(EscuelaRepository escuelaRepo, DocenteRepository docenteRepo, EnfGraRepository enfermedadRepo) {
        this.escuelaRepo = escuelaRepo;
        this.docenteRepo = docenteRepo;
        this.enfermedadRepo = enfermedadRepo;
    }

    @Override
    public String obtenerLugarTrabajoPorInfra(String cInfra) {
        return escuelaRepo.findBycInfra(cInfra)
                .map(Escuela::getNombre)
                .orElse("No encontrado");
    }

    @Override
    public String obtenerNombrePorNip(String nip) {
        return docenteRepo.findByIdpersonaAContainingIgnoreCase(nip).stream()
                .findFirst()
                .map(Docente::getDepersona)
                .orElse("No encontrado");
    }

    @Override
    @Transactional
    public EnfGra guardarRegistro(EnfGra registro) {
        registro.calcularDias(); // ✅ Calcula los días automáticamente
        return enfermedadRepo.save(registro);
    }

    @Override
    public List<EnfGra> obtenerTodos() {
        return enfermedadRepo.findAll();
    }
}
