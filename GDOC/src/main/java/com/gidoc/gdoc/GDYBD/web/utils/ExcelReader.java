package com.gidoc.gdoc.GDYBD.web.utils;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.eventusermodel.*;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public final class ExcelReader {
    private ExcelReader() {}

    public static List<Map<String,String>> readAll(File file) throws IOException {
        List<Map<String,String>> data = new ArrayList<>();
        try (OPCPackage pkg = OPCPackage.open(file)) {
            XSSFReader reader = new XSSFReader(pkg);
            XSSFReader.SheetIterator sheets = (XSSFReader.SheetIterator) reader.getSheetsData();

            if (!sheets.hasNext()) return data;

            try (InputStream sheetStream = sheets.next()) {
                DataFormatter formatter = new DataFormatter();
                SheetHandler handler = new SheetHandler(data, formatter);
                XMLReader parser = XMLReaderFactory.createXMLReader();
                parser.setContentHandler(handler);
                parser.parse(new InputSource(sheetStream));
            }

        } catch (Exception e) {
            throw new IOException("Error leyendo Excel en streaming", e);
        }
        return data;
    }

    // ------------------------
    // Handler SAX para streaming
    // ------------------------
    private static class SheetHandler extends DefaultHandler {
        private final List<Map<String,String>> data;
        private final DataFormatter formatter;
        private Map<String,String> currentRow;
        private List<String> headers = new ArrayList<>();
        private String currentCellValue;
        private int currentCol = -1;
        private boolean isHeader = true;

        public SheetHandler(List<Map<String,String>> data, DataFormatter formatter) {
            this.data = data;
            this.formatter = formatter;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            if ("row".equals(qName)) {
                currentRow = new LinkedHashMap<>();
                currentCol = -1;
            } else if ("c".equals(qName)) { // cell
                String r = attributes.getValue("r");
                currentCol = getColumnIndex(r);
                currentCellValue = "";
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            currentCellValue += new String(ch, start, length);
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            if ("v".equals(qName) || "t".equals(qName)) {
                if (isHeader) {
                    headers.add(currentCellValue.trim());
                } else if (currentCol >= 0 && currentCol < headers.size()) {
                    currentRow.put(headers.get(currentCol), currentCellValue.trim());
                }
            } else if ("row".equals(qName)) {
                if (isHeader) {
                    isHeader = false;
                } else if (!currentRow.isEmpty()) {
                    data.add(currentRow);
                }
            }
        }

        private int getColumnIndex(String cellRef) {
            if (cellRef == null) return -1;
            int col = 0;
            for (char c : cellRef.toCharArray()) {
                if (Character.isLetter(c)) {
                    col = col * 26 + (c - 'A' + 1);
                } else break;
            }
            return col - 1;
        }
    }
}
