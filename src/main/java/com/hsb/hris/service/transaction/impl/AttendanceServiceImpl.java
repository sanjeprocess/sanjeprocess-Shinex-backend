package com.hsb.hris.service.transaction.impl;

import com.hsb.hris.entity.Attendance;
import com.hsb.hris.entity.id.AttendanceId;
import com.hsb.hris.repository.AttendanceRepository;
import com.hsb.hris.service.transaction.AttendanceService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository repo;
    private final EntityManager em;
    private final com.hsb.hris.repository.EmployeeRepository employeeRepo;

    public AttendanceServiceImpl(AttendanceRepository repo, EntityManager em, com.hsb.hris.repository.EmployeeRepository employeeRepo) {
        this.repo = repo;
        this.em = em;
        this.employeeRepo = employeeRepo;
    }

    @Override
    @Transactional
    public Map<String, Object> uploadExcel(MultipartFile file, String businessCenter) throws Exception {
        Map<String, Object> result = new HashMap<>();
        List<Map<String,Object>> errors = new ArrayList<>();
        int inserted = 0, updated = 0, totalRows = 0;

        try (InputStream is = file.getInputStream(); Workbook wb = new XSSFWorkbook(is)) {
            Sheet sheet = wb.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                int displayRow = i + 1; // for human readable messages
                Row r = sheet.getRow(i);
                if (r == null) continue;
                totalRows++;
                try {
                    // Read cells defensively
                    String year = readStringCell(r, 0);
                    String month = readStringCell(r, 1);
                    String epf = readStringCell(r, 2);
                    if (epf == null || epf.isBlank()) throw new IllegalArgumentException("EPF_No is required");

                    // Validate employee exists
                    if (!employeeRepo.existsById(epf)) {
                        Map<String,Object> err = new HashMap<>(); err.put("row", displayRow); err.put("reason", "EPF_No not found: " + epf);
                        errors.add(err); continue;
                    }

                    String plantCode = readStringCell(r, 3);
                    LocalDate dayIn = readLocalDateCell(r, 4);
                    if (dayIn == null) throw new IllegalArgumentException("Day_In is required and must be yyyy-MM-dd or Excel date");

                    java.time.LocalTime timeIn = readLocalTimeCell(r, 5);
                    java.time.LocalDateTime dayOut = readLocalDateTimeCell(r, 6);
                    java.time.LocalTime timeOut = readLocalTimeCell(r, 7);
                    Double halfDay = readDoubleCell(r, 8);
                    String dayShift = readStringCell(r, 9);
                    String secondShift = readStringCell(r, 10);
                    String nightShift = readStringCell(r, 11);
                    String fullNight = readStringCell(r, 12);
                    String normalDay = readStringCell(r, 13);
                    String saturdayPoya = readStringCell(r, 14);
                    String specialDay = readStringCell(r, 15);
                    Double statutoryHolidays = readDoubleCell(r, 16);
                    Double noOfMeal = readDoubleCell(r, 17);
                    String bc = readStringCell(r, 18);
                    if (bc == null || bc.isBlank()) bc = businessCenter; // fallback

                    // Upsert by composite key
                    com.hsb.hris.entity.id.AttendanceId aid = new com.hsb.hris.entity.id.AttendanceId(year, month, epf, dayIn);
                    Optional<Attendance> existing = repo.findById(aid);
                    Attendance a = existing.orElseGet(Attendance::new);

                    a.setAttYear(year);
                    a.setAttMonth(month);
                    a.setEpfNo(epf);
                    a.setDayIn(dayIn);
                    a.setPlantCode(plantCode);
                    a.setTimeIn(timeIn);
                    a.setDayOut(dayOut);
                    a.setTimeOut(timeOut);
                    a.setHalfDay(halfDay);
                    a.setDayShift(asYn(dayShift));
                    a.setSecondShift(asYn(secondShift));
                    a.setNightShift(asYn(nightShift));
                    a.setFullNight(asYn(fullNight));
                    a.setNormalDay(asYn(normalDay));
                    a.setSaturdayPoya(asYn(saturdayPoya));
                    a.setSpecialDay(asYn(specialDay));
                    a.setStatutoryHolidays(statutoryHolidays);
                    a.setNoOfMeal(noOfMeal);
                    a.setBusinessCenter(bc);

                    if (existing.isPresent()) { repo.save(a); updated++; }
                    else { repo.save(a); inserted++; }

                } catch (Exception ex) {
                    Map<String,Object> err = new HashMap<>(); err.put("row", displayRow); err.put("reason", ex.getMessage());
                    errors.add(err);
                }
            }
        }

        result.put("totalRows", totalRows);
        result.put("inserted", inserted);
        result.put("updated", updated);
        result.put("errors", errors);
        return result;
    }

    // --- helper parsing methods ---
    private String readStringCell(Row r, int idx) {
        try {
            if (r.getCell(idx) == null) return null;
            switch (r.getCell(idx).getCellType()) {
                case STRING: return r.getCell(idx).getStringCellValue().trim();
                case NUMERIC: return String.valueOf(((double)r.getCell(idx).getNumericCellValue())).trim();
                case BOOLEAN: return String.valueOf(r.getCell(idx).getBooleanCellValue());
                default: return null;
            }
        } catch (Exception e) { return null; }
    }

    private Double readDoubleCell(Row r, int idx) {
        try {
            if (r.getCell(idx) == null) return null;
            switch (r.getCell(idx).getCellType()) {
                case NUMERIC: return r.getCell(idx).getNumericCellValue();
                case STRING: return Double.valueOf(r.getCell(idx).getStringCellValue());
                default: return null;
            }
        } catch (Exception e) { return null; }
    }

    private java.time.LocalDate readLocalDateCell(Row r, int idx) {
        try {
            if (r.getCell(idx) == null) return null;
            if (r.getCell(idx).getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC && org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(r.getCell(idx))) {
                java.util.Date d = r.getCell(idx).getDateCellValue();
                return d.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            }
            String s = r.getCell(idx).getStringCellValue();
            if (s == null || s.isBlank()) return null;
            return java.time.LocalDate.parse(s.trim());
        } catch (Exception e) { return null; }
    }

    private java.time.LocalTime readLocalTimeCell(Row r, int idx) {
        try {
            if (r.getCell(idx) == null) return null;
            if (r.getCell(idx).getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC && org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(r.getCell(idx))) {
                java.util.Date d = r.getCell(idx).getDateCellValue();
                return d.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalTime().withSecond(0).withNano(0);
            }
            String s = r.getCell(idx).getStringCellValue();
            if (s == null || s.isBlank()) return null;
            return java.time.LocalTime.parse(s.trim());
        } catch (Exception e) { return null; }
    }

    private java.time.LocalDateTime readLocalDateTimeCell(Row r, int idx) {
        try {
            if (r.getCell(idx) == null) return null;
            if (r.getCell(idx).getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC && org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(r.getCell(idx))) {
                java.util.Date d = r.getCell(idx).getDateCellValue();
                return d.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
            }
            String s = r.getCell(idx).getStringCellValue();
            if (s == null || s.isBlank()) return null;
            return java.time.LocalDateTime.parse(s.trim().replace(" ","T"));
        } catch (Exception e) { return null; }
    }

    private String asYn(String v) {
        if (v == null) return null;
        v = v.trim().toUpperCase();
        if (v.equals("Y") || v.equals("YES") || v.equals("1") ) return "Y";
        if (v.equals("N") || v.equals("NO") || v.equals("0")) return "N";
        return v;
    }

    @Override
    @Transactional
    public Map<String, Object> autoUpdate(String year, String month, String businessCenter) {
        // Very small placeholder: in the real system this should call business rules
        Map<String,Object> out = new HashMap<>();
        List<Attendance> records = repo.findByBusinessCenterAndAttYearAndAttMonth(businessCenter, year, month);
        out.put("processed", records.size());
        out.put("message", "Auto-update ran (placeholder)");
        return out;
    }

    @Override
    public List<Map<String, Object>> queryAnalyzer(Map<String, Object> filters) {
        StringBuilder jpql = new StringBuilder("SELECT a FROM Attendance a WHERE 1=1");
        Map<String,Object> params = new HashMap<>();
        if (filters.containsKey("businessCenter")) {
            jpql.append(" AND a.businessCenter = :bc");
            params.put("bc", filters.get("businessCenter"));
        }
        if (filters.containsKey("epfNo")) {
            jpql.append(" AND a.epfNo = :epf");
            params.put("epf", filters.get("epfNo"));
        }
        TypedQuery<Attendance> q = em.createQuery(jpql.toString(), Attendance.class);
        params.forEach(q::setParameter);
        List<Attendance> results = q.getResultList();
        List<Map<String,Object>> out = new ArrayList<>();
        for (Attendance a : results) {
            Map<String,Object> map = new HashMap<>();
            map.put("attYear", a.getAttYear());
            map.put("attMonth", a.getAttMonth());
            map.put("epfNo", a.getEpfNo());
            map.put("dayIn", a.getDayIn());
            map.put("timeIn", a.getTimeIn());
            map.put("timeOut", a.getTimeOut());
            out.add(map);
        }
        return out;
    }
}
