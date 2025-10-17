//package com.gidoc.gdoc.REGDOC.repo.implementations;
//
//import com.gidoc.gdoc.REGDOC.domain.entities.Incapacidad;
//import com.gidoc.gdoc.REGDOC.domain.services.IncapacidadService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class IncapacidadServiceImpl {
//    private final IncapacidadService incapacidadRepository;
//
//    public Incapacidad guardarIncapacidad(Incapacidad incapacidad) {
//        return incapacidadRepository.save(incapacidad);
//    }
//
//    public List<Incapacidad> obtenerIncapacidadesPorMes(String mes, Integer anio) {
//        return incapacidadRepository.findByMesReferenciaAndAnioReferencia(mes, anio);
//    }
//}