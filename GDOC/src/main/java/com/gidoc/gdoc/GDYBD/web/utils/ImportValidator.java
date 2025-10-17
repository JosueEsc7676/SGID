package com.gidoc.gdoc.GDYBD.web.utils;

import com.gidoc.gdoc.GDYBD.domain.services.ImportService;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class ImportValidator {

    private ImportValidator() {}

    public static void verificarTipoDeArchivo(List<Map<String, String>> filas, ImportService.Type tipoEsperado) {
        if (filas.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío o no contiene datos válidos.");
        }

        // Normalizar todas las cabeceras de todas las filas
        Set<String> columnas = filas.stream()
                .flatMap(row -> row.keySet().stream())
                .map(k -> k.replaceAll("\\s+", "").toLowerCase())
                .collect(Collectors.toSet());

        Set<String> cabecerasDocentes = Set.of("idpersona_a","depersona","nip","codigo_infra");
        Set<String> cabecerasEscuelas = Set.of("c_infra","numero","distrito","nombre","municipio");

        boolean esArchivoDocentes = columnas.containsAll(cabecerasDocentes);
        boolean esArchivoEscuelas = columnas.containsAll(cabecerasEscuelas);

        if (esArchivoDocentes && tipoEsperado == ImportService.Type.ESCUELAS) {
            throw new IllegalArgumentException(
                    "🚫 El archivo parece ser de DOCENTES, pero estás en la sección de ESCUELAS.\n" +
                            "Selecciona el archivo correcto."
            );
        }

        if (esArchivoEscuelas && tipoEsperado == ImportService.Type.DOCENTES) {
            throw new IllegalArgumentException(
                    "🚫 El archivo parece ser de ESCUELAS, pero estás en la sección de DOCENTES.\n" +
                            "Selecciona el archivo correcto."
            );
        }

        if (!esArchivoDocentes && !esArchivoEscuelas) {
            throw new IllegalArgumentException(
                    "⚠ No se reconoce el formato del archivo. Revisa que tenga cabeceras válidas."
            );
        }
    }
}
