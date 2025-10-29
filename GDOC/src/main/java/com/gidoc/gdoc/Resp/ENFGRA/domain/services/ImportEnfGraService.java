package com.gidoc.gdoc.Resp.ENFGRA.domain.services;

import com.gidoc.gdoc.Resp.ENFGRA.domain.entities.EnfGra;

import java.util.List;

public interface ImportEnfGraService {

    /**
     * Buscar el nombre del centro escolar por C_INFRA.
     */
    String obtenerLugarTrabajoPorInfra(String cInfra);

    /**
     * Buscar el nombre del docente por NIP.
     */
    String obtenerNombrePorNip(String idpersonaA);

    /**
     * Guardar un registro de enfermedad grave.
     */
    EnfGra guardarRegistro(EnfGra registro);

    /**
     * Obtener todos los registros de enfermedad grave.
     */
    List<EnfGra> obtenerTodos();
}
