package com.gidoc.gdoc.GDYBD.web.utils;

import com.gidoc.gdoc.GDYBD.domain.services.ImportService;
import org.apache.poi.ss.usermodel.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class FileReaderUtil {

    private FileReaderUtil() {}

    public static List<Map<String,String>> readAll(File file) throws IOException {
        String name = file.getName().toLowerCase();
        if (name.endsWith(".xls") || name.endsWith(".xlsx") || name.endsWith(".xlsm")) {
            return readExcel(file);
        } else if (name.endsWith(".csv")) {
            return readCSV(file);
        } else {
            throw new IllegalArgumentException("Formato no soportado: " + name);
        }
    }

    public static List<Map<String,String>> readAll(File file, ImportService.Type type) throws IOException {
        List<Map<String,String>> data = readAll(file);
        List<String> expectedHeaders = new ArrayList<>();

        if (type == ImportService.Type.DOCENTES) {
            expectedHeaders = Arrays.asList("idpersona_a","depersona","nip","codigo_infra");
        } else if (type == ImportService.Type.ESCUELAS) {
            expectedHeaders = Arrays.asList("c_infra","numero","distrito","nombre","municipio");
        }

        List<Map<String,String>> fixed = new ArrayList<>();
        for (Map<String,String> row : data) {
            Map<String,String> newRow = new LinkedHashMap<>();
            int i = 0;
            for (String key : row.keySet()) {
                String value = row.get(key);
                String newKey = i < expectedHeaders.size() ? expectedHeaders.get(i) : key;
                newRow.put(newKey, value);
                i++;
            }
            fixed.add(newRow);
        }
        return fixed;
    }

    private static List<Map<String,String>> readExcel(File file) throws IOException {
        List<Map<String,String>> data = new ArrayList<>();
        try (InputStream is = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();
            if (!rows.hasNext()) return data;

            Row headerRow = rows.next();
            List<String> headers = new ArrayList<>();
            for (Cell c : headerRow) headers.add(cellToString(c).trim());

            while (rows.hasNext()) {
                Row r = rows.next();
                Map<String,String> map = new LinkedHashMap<>();
                for (int i = 0; i < headers.size(); i++) {
                    Cell cell = r.getCell(i);
                    map.put(headers.get(i), cellToString(cell).trim());
                }
                data.add(map);
            }

        } catch (Exception e) {
            throw new IOException("Error leyendo Excel: " + file.getName(), e);
        }
        return data;
    }

    private static String cellToString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) return cell.getLocalDateTimeCellValue().toString();
                double d = cell.getNumericCellValue();
                long lv = (long) d;
                return lv == d ? Long.toString(lv) : Double.toString(d);
            case BOOLEAN: return Boolean.toString(cell.getBooleanCellValue());
            case FORMULA:
                try { return cell.getStringCellValue(); }
                catch (IllegalStateException e) { return Double.toString(cell.getNumericCellValue()); }
            case BLANK: return "";
            default: return cell.toString();
        }
    }

    private static List<Map<String,String>> readCSV(File file) throws IOException {
        List<Map<String,String>> data = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            String[] headers = null;

            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;

                char separator = line.contains(";") ? ';' : ',';
                String[] cols = line.split(String.valueOf(separator), -1);

                if (headers == null) {
                    headers = cols;
                } else {
                    Map<String,String> row = new LinkedHashMap<>();
                    for (int i = 0; i < headers.length && i < cols.length; i++) {
                        row.put(headers[i].trim(), cols[i].trim());
                    }
                    data.add(row);
                }
            }
        }
        return data;
    }
}
