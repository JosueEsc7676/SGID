package com.gidoc.gdoc.REGDOC.web.controllers;

import com.gidoc.gdoc.REGDOC.domain.entities.RegistroMes;
import com.itextpdf.text.Document;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.xwpf.usermodel.*;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import static com.gidoc.gdoc.REGDOC.web.controllers.HistorialMensualController.getRegistrosSeleccionados;

@Component
public class PrepararReporteController {

    @FXML private TableView<RegistroMes> tableSeleccionados;
    @FXML private TableColumn<RegistroMes, String> colMunicipio;
    @FXML private TableColumn<RegistroMes, String> colDistrito;
    @FXML private TableColumn<RegistroMes, String> colCInfra;
    @FXML private TableColumn<RegistroMes, String> colCentro;
    @FXML private TableColumn<RegistroMes, String> colNip;
    @FXML private TableColumn<RegistroMes, String> colNombre;
    @FXML private TableColumn<RegistroMes, String> colDoc4;
    @FXML private TableColumn<RegistroMes, String> colDoc5;
    @FXML private TableColumn<RegistroMes, String> colPens;
    @FXML private Button btnExportarWord;
    @FXML private Button btnExportarPDF;
    @FXML private Button btnCerrar;
    @FXML private Button btnLimpiar; // 🆕 Nuevo botón

    @FXML
    public void initialize() {
        colMunicipio.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getMunicipio()));
        colDistrito.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDistrito()));
        colCInfra.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getCInfra()));
        colCentro.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getCentroEducativo()));
        colNip.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNip()));
        colNombre.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNombreDocente()));
        colDoc4.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDoc4()));
        colDoc5.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDoc5()));
        colPens.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getPens()));
        btnLimpiar.setOnAction(evt -> limpiarTabla()); // 🆕 Acción nueva

        List<RegistroMes> seleccionados = getRegistrosSeleccionados();
        tableSeleccionados.getItems().setAll(seleccionados);

        btnExportarWord.setOnAction(evt -> exportarWord());
        btnExportarPDF.setOnAction(evt -> exportarPDF());
        btnCerrar.setOnAction(evt -> cerrarVentana());
    }
    // 🧹 Limpia la tabla completamente
    private void limpiarTabla() {
        if (tableSeleccionados.getItems().isEmpty()) {
            mostrarAlerta("No hay registros para limpiar.", Alert.AlertType.INFORMATION);
            return;
        }
// Limpia la tabla visual
        tableSeleccionados.getItems().clear();

        // Limpia también la lista estática del otro controlador
        HistorialMensualController.limpiarRegistrosSeleccionados();

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText("Confirmar acción");
        confirm.setContentText("¿Deseas eliminar todos los registros de la tabla?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                tableSeleccionados.getItems().clear();
                mostrarAlerta("Registros limpiados correctamente.", Alert.AlertType.INFORMATION);
            }
        });
    }
    // ========== EXPORTAR A WORD ==========
    private void exportarWord() {
        try {
            List<RegistroMes> registros = tableSeleccionados.getItems();
            if (registros.isEmpty()) {
                mostrarAlerta("No hay datos para exportar", Alert.AlertType.WARNING);
                return;
            }

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar reporte Word");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Documento Word (*.docx)", "*.docx"));
            File file = fileChooser.showSaveDialog(btnExportarWord.getScene().getWindow());
            if (file == null) return;

            XWPFDocument doc = new XWPFDocument();

            XWPFParagraph title = doc.createParagraph();
            title.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun run = title.createRun();
            run.setText("Reporte de Registros Mensuales");
            run.setBold(true);
            run.setFontSize(14);

            doc.createParagraph().createRun().addBreak();

            // Encabezados
            String[] headers = {
                    "Municipio", "Distrito", "C_INFRA", "Centro Educativo",
                    "NIP", "Nombre Docente", "NIT (DOC4)", "NUP (DOC5)", "PENS"
            };

            XWPFTable table = doc.createTable();
            XWPFTableRow headerRow = table.getRow(0);
            for (int i = 0; i < headers.length; i++) {
                if (i == 0) headerRow.getCell(0).setText(headers[i]);
                else headerRow.addNewTableCell().setText(headers[i]);
            }

            // Datos
            for (RegistroMes r : registros) {
                XWPFTableRow row = table.createRow();
                row.getCell(0).setText(nvl(r.getMunicipio()));
                row.getCell(1).setText(nvl(r.getDistrito()));
                row.getCell(2).setText(nvl(r.getCInfra()));
                row.getCell(3).setText(nvl(r.getCentroEducativo()));
                row.getCell(4).setText(nvl(r.getNip()));
                row.getCell(5).setText(nvl(r.getNombreDocente()));
                row.getCell(6).setText(nvl(r.getDoc4()));
                row.getCell(7).setText(nvl(r.getDoc5()));
                row.getCell(8).setText(nvl(r.getPens()));
            }

            try (FileOutputStream out = new FileOutputStream(file)) {
                doc.write(out);
            }

            mostrarAlerta("Reporte Word exportado correctamente", Alert.AlertType.INFORMATION);

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error al exportar Word: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // ========== EXPORTAR A PDF ==========
    private void exportarPDF() {
        try {
            List<RegistroMes> registros = tableSeleccionados.getItems();
            if (registros.isEmpty()) {
                mostrarAlerta("No hay datos para exportar", Alert.AlertType.WARNING);
                return;
            }

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar reporte PDF");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo PDF (*.pdf)", "*.pdf"));
            File file = fileChooser.showSaveDialog(btnExportarPDF.getScene().getWindow());
            if (file == null) return;

            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            Font fontTitle = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
            Paragraph title = new Paragraph("Reporte de Registros Mensuales", fontTitle);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            // 9 columnas
            PdfPTable table = new PdfPTable(9);
            table.setWidthPercentage(100);
            String[] headers = {
                    "Municipio", "Distrito", "C_INFRA", "Centro Educativo",
                    "NIP", "Nombre Docente", "NIT (DOC4)", "NUP (DOC5)", "PENS"
            };

            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h));
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            for (RegistroMes r : registros) {
                table.addCell(nvl(r.getMunicipio()));
                table.addCell(nvl(r.getDistrito()));
                table.addCell(nvl(r.getCInfra()));
                table.addCell(nvl(r.getCentroEducativo()));
                table.addCell(nvl(r.getNip()));
                table.addCell(nvl(r.getNombreDocente()));
                table.addCell(nvl(r.getDoc4()));
                table.addCell(nvl(r.getDoc5()));
                table.addCell(nvl(r.getPens()));
            }

            document.add(table);
            document.close();

            mostrarAlerta("Reporte PDF exportado correctamente", Alert.AlertType.INFORMATION);

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error al exportar PDF: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private String nvl(String val) {
        return val == null ? "" : val;
    }

    private void cerrarVentana() {
        Stage stage = (Stage) btnCerrar.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
