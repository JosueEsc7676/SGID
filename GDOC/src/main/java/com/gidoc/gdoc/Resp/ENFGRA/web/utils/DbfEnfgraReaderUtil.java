package com.gidoc.gdoc.Resp.ENFGRA.web.utils;

import com.gidoc.gdoc.Resp.ENFGRA.web.utils.ColumnNormalizer;
import com.linuxense.javadbf.DBFReader;

import java.io.FileInputStream;
import java.util.*;

public class DbfEnfgraReaderUtil {

    public static List<Map<String, String>> leer(FileInputStream fis) throws Exception {
        List<Map<String, String>> registros = new ArrayList<>();

        try (DBFReader reader = new DBFReader(fis)) {
            int numCampos = reader.getFieldCount();
            List<String> columnas = new ArrayList<>();

            for (int i = 0; i < numCampos; i++) {
                columnas.add(ColumnNormalizer.normalize(reader.getField(i).getName()));
            }

            Object[] fila;
            while ((fila = reader.nextRecord()) != null) {
                Map<String, String> registro = new HashMap<>();
                for (int i = 0; i < fila.length; i++) {
                    registro.put(columnas.get(i), String.valueOf(fila[i]).trim());
                }
                registros.add(registro);
            }
        }

        return registros;
    }
}
