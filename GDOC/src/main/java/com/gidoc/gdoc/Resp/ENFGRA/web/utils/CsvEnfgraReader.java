package com.gidoc.gdoc.Resp.ENFGRA.web.utils;

import com.gidoc.gdoc.Resp.ENFGRA.web.utils.ColumnNormalizer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

public class CsvEnfgraReader {

    public static List<Map<String, String>> leer(String rutaArchivo) throws Exception {
        List<Map<String, String>> registros = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            String linea;
            List<String> columnas = new ArrayList<>();

            if ((linea = br.readLine()) != null) {
                String[] encabezado = linea.split(",");
                for (String col : encabezado) {
                    columnas.add(ColumnNormalizer.normalize(col));
                }
            }

            while ((linea = br.readLine()) != null) {
                String[] valores = linea.split(",");
                Map<String, String> registro = new HashMap<>();
                for (int i = 0; i < columnas.size(); i++) {
                    registro.put(columnas.get(i), i < valores.length ? valores[i].trim() : "");
                }
                registros.add(registro);
            }
        }

        return registros;
    }
}
