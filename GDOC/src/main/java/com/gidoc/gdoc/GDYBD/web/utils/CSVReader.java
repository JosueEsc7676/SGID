package com.gidoc.gdoc.GDYBD.web.utils;

import java.io.*;
import java.util.*;

public class CSVReader {

    public static List<Map<String,String>> readAll(File file) throws IOException {
        List<Map<String,String>> rows = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            String[] headers = null;

            while ((line = br.readLine()) != null) {
                String[] cols = line.split(",", -1); // mantener columnas vacías
                if (headers == null) {
                    headers = cols;
                    continue;
                }
                Map<String,String> row = new HashMap<>();
                for (int i = 0; i < headers.length && i < cols.length; i++) {
                    row.put(headers[i].trim(), cols[i].trim());
                }
                rows.add(row);
            }
        }
        return rows;
    }
}
