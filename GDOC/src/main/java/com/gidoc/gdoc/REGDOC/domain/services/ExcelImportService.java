package com.gidoc.gdoc.REGDOC.domain.services;

import com.gidoc.gdoc.REGDOC.domain.dto.RegistroDTO;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExcelImportService {

    private final HistorialMensualService historialService;

    public ExcelImportService(HistorialMensualService historialService) {
        this.historialService = historialService;
    }

    /**
     * Importa registros desde un archivo Excel a un mes específico
     */
    public List<RegistroDTO> importarExcel(File file, String mes) throws Exception {
        List<RegistroDTO> registros = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheetAt(0); // primera hoja

            for (int i = 1; i <= sheet.getLastRowNum(); i++) { // saltamos cabecera (fila 0)
                Row row = sheet.getRow(i);
                if (row == null) continue;

                RegistroDTO dto = new RegistroDTO();
                dto.setCInfra(getCellString(row.getCell(0)));
                dto.setNip(getCellString(row.getCell(1)));
                dto.setNombreDocente(getCellString(row.getCell(2)));
                dto.setCentroEducativo(getCellString(row.getCell(3)));
                dto.setMunicipio(getCellString(row.getCell(4)));
                dto.setDistrito(getCellString(row.getCell(5)));
                dto.setHoras(getCellInt(row.getCell(6)));
                dto.setDesde(getCellDate(row.getCell(7)));
                dto.setHasta(getCellDate(row.getCell(8)));
                dto.setPatologia(getCellString(row.getCell(9)));
                dto.setObservaciones(getCellString(row.getCell(10)));
                dto.setMes(mes.toLowerCase());

                // Guardamos directamente en la base de datos
                historialService.save(dto);

                registros.add(dto);
            }
        }

        return registros;
    }

    private String getCellString(Cell cell) {
        return cell == null ? "" : cell.toString().trim();
    }

    private Integer getCellInt(Cell cell) {
        if (cell == null) return 0;
        if (cell.getCellType() == CellType.NUMERIC) return (int) cell.getNumericCellValue();
        try {
            return Integer.parseInt(cell.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private LocalDate getCellDate(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate();
        }
        return null;
    }
}
