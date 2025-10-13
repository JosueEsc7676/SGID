package com.gidoc.gdoc.GDYBD.domain.services;

import com.gidoc.gdoc.GDYBD.domain.entities.Docente;
import com.gidoc.gdoc.GDYBD.domain.entities.Escuela;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface ImportService {

    enum Type { DOCENTES, ESCUELAS }
    enum Mode { INSERT, UPDATE }

    // Métodos de importación
    void importarExcel(File archivo, String tipo) throws IOException;
    void importarDBF(File archivo, String tipo) throws IOException;
    void actualizarDesdeExcel(File archivo, String tipo) throws IOException;
    void actualizarDesdeDBF(File archivo, String tipo) throws IOException;

    void importFile(File file, Type type, Mode mode) throws Exception;

    // Métodos para obtener datos
    List<Docente> obtenerTodosDocentes();
    List<Escuela> obtenerTodasEscuelas();

    // ================= CRUD ESCUELAS =================
    Escuela agregarEscuela(Escuela escuela);
    Escuela editarEscuela(Escuela escuela);
    void eliminarEscuela(Escuela escuela);
}
