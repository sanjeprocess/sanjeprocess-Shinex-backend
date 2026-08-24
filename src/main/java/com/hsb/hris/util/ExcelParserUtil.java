package com.hsb.hris.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Utility to parse an Excel file (.xlsx) into a list of maps by header name.
 * Provides helpers to build header->column index map and read typed values.
 */
public class ExcelParserUtil {

    public static class ParsedResult {
        public final List<Map<String, Object>> rows = new ArrayList<>();
        public final List<Map<String, Object>> errors = new ArrayList<>();
        public int totalRows = 0;
        public int inserted = 0;
        public int updated = 0;
    }

    public static ParsedResult parseSheet(MultipartFile file, int headerRowIndex) throws Exception {
        try (InputStream is = file.getInputStream(); Workbook wb = new XSSFWorkbook(is)) {
            Sheet sheet = wb.getSheetAt(0);
            return parseSheet(sheet, headerRowIndex);
        }
    }

    public static ParsedResult parseSheet(Sheet sheet, int headerRowIndex) {
        ParsedResult out = new ParsedResult();
        Row headerRow = sheet.getRow(headerRowIndex);
        if (headerRow == null) return out;
        Map<String, Integer> headerIndex = new HashMap<>();
        for (int c = 0; c < headerRow.getLastCellNum(); c++) {
            Cell cell = headerRow.getCell(c);
            if (cell == null) continue;
            String raw = cell.getStringCellValue();
            if (raw == null) continue;
            String key = normalizeHeader(raw);
            headerIndex.put(key, c);
        }

        for (int r = headerRowIndex + 1; r <= sheet.getLastRowNum(); r++) {
            Row row = sheet.getRow(r);
            if (row == null) continue;

            // detect blank row
            boolean blank = true;
            for (int c = 0; c < headerRow.getLastCellNum(); c++) {
                Cell cell = row.getCell(c);
                if (cell != null && cell.getCellType() != CellType.BLANK) { blank = false; break; }
            }
            if (blank) continue;

            out.totalRows++;
            Map<String, Object> map = new HashMap<>();
            try {
                for (Map.Entry<String, Integer> e : headerIndex.entrySet()) {
                    String header = e.getKey();
                    int idx = e.getValue();
                    Cell cell = row.getCell(idx);
                    Object val = readCell(cell);
                    map.put(header, val);
                }
                out.rows.add(map);
            } catch (Exception ex) {
                Map<String,Object> err = new HashMap<>();
                err.put("row", r+1);
                err.put("reason", ex.getMessage());
                out.errors.add(err);
            }
        }
        return out;
    }

    public static String normalizeHeader(String s) {
        if (s == null) return null;
        return s.trim().toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    public static Object readCell(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    Date d = cell.getDateCellValue();
                    // prefer date-time if time component present
                    Calendar cal = Calendar.getInstance(); cal.setTime(d);
                    if (cal.get(Calendar.HOUR_OF_DAY) == 0 && cal.get(Calendar.MINUTE) == 0 && cal.get(Calendar.SECOND) == 0) {
                        return LocalDate.ofInstant(d.toInstant(), java.time.ZoneId.systemDefault());
                    } else {
                        return LocalDateTime.ofInstant(d.toInstant(), java.time.ZoneId.systemDefault());
                    }
                } else {
                    double nv = cell.getNumericCellValue();
                    if (nv == Math.rint(nv)) return (long) nv; // integer
                    return nv;
                }
            case BOOLEAN: return cell.getBooleanCellValue();
            case FORMULA:
                try { return cell.getStringCellValue(); } catch (Exception ex) { return cell.getNumericCellValue(); }
            default: return null;
        }
    }

    // Convenience parsers
    public static LocalDate parseLocalDate(Object o) {
        if (o == null) return null;
        if (o instanceof LocalDate) return (LocalDate)o;
        if (o instanceof LocalDateTime) return ((LocalDateTime)o).toLocalDate();
        String s = String.valueOf(o).trim();
        if (s.isEmpty()) return null;
        try { return LocalDate.parse(s); } catch (DateTimeParseException ex) { return null; }
    }
    public static LocalTime parseLocalTime(Object o) {
        if (o == null) return null;
        if (o instanceof LocalTime) return (LocalTime)o;
        if (o instanceof LocalDateTime) return ((LocalDateTime)o).toLocalTime();
        String s = String.valueOf(o).trim();
        if (s.isEmpty()) return null;
        try { return LocalTime.parse(s); } catch (DateTimeParseException ex) { return null; }
    }
    public static LocalDateTime parseLocalDateTime(Object o) {
        if (o == null) return null;
        if (o instanceof LocalDateTime) return (LocalDateTime)o;
        if (o instanceof LocalDate) return ((LocalDate)o).atStartOfDay();
        String s = String.valueOf(o).trim();
        if (s.isEmpty()) return null;
        try { return LocalDateTime.parse(s); } catch (DateTimeParseException ex) {
            // try space-separated
            try { return LocalDateTime.parse(s.replace(' ', 'T')); } catch (Exception e) { return null; }
        }
    }
}
