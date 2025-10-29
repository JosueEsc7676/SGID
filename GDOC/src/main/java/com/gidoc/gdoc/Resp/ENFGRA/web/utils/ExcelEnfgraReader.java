package com.gidoc.gdoc.Resp.ENFGRA.web.utils;

import com.gidoc.gdoc.Resp.ENFGRA.web.utils.ColumnNormalizer;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

public class ExcelEnfgraReader {

    public static List<Map<String, String>> leer(File archivo) throws Exception {
        List<Map<String, String>> registros = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(archivo);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet hoja = workbook.getSheetAt(0);
            Iterator<Row> filas = hoja.iterator();

            List<String> columnas = new ArrayList<>();
            if (filas.hasNext()) {
                Row encabezado = filas.next();
                for (Cell celda : encabezado) {
                    columnas.add(ColumnNormalizer.normalize(celda.getStringCellValue()));
                }
            }

            while (filas.hasNext()) {
                Row fila = filas.next();
                Map<String, String> registro = new HashMap<>();
                for (int i = 0; i < columnas.size(); i++) {
                    Cell celda = fila.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    celda.setCellType(CellType.STRING);
                    registro.put(columnas.get(i), celda.getStringCellValue().trim());
                }
                registros.add(registro);
            }
        }

        return registros;
    }
}
