package com.gidoc.gdoc.GDYBD.web.utils;

import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFReader;

import java.io.*;
import java.util.*;

public final class DbfReaderUtil {
    private DbfReaderUtil() {}

    public static List<Map<String,String>> readAll(File dbfFile, String charset) throws IOException {
        List<Map<String,String>> rows = new ArrayList<>();
        try (InputStream is = new FileInputStream(dbfFile); DBFReader reader = new DBFReader(is)) {
            reader.setCharactersetName(charset != null ? charset : "Cp850");

            int fieldsCount = reader.getFieldCount();
            List<String> headers = new ArrayList<>();
            for (int i = 0; i < fieldsCount; i++) {
                DBFField field = reader.getField(i);
                headers.add(field.getName());
            }

            Object[] rowObjects;
            while ((rowObjects = reader.nextRecord()) != null) {
                Map<String,String> map = new LinkedHashMap<>();
                for (int i = 0; i < rowObjects.length; i++) {
                    Object val = rowObjects[i];
                    map.put(headers.get(i), val != null ? val.toString().trim() : "");
                }
                rows.add(map);
            }
        } catch (Exception e) {
            throw new IOException("Error leyendo DBF: " + dbfFile.getName(), e);
        }
        return rows;
    }
}
