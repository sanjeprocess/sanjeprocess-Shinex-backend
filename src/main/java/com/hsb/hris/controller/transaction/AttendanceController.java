package com.hsb.hris.controller.transaction;

import com.hsb.hris.entity.Attendance;
import com.hsb.hris.repository.AttendanceRepository;
import com.hsb.hris.service.transaction.AttendanceService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/attendance", "/api/transaction/attendance", "/attendance"})
public class AttendanceController {

    private final AttendanceService service;
    private final AttendanceRepository attendanceRepository;
    private final com.hsb.hris.repository.EmployeeRepository employeeRepository;

    public AttendanceController(AttendanceService service, AttendanceRepository attendanceRepository, com.hsb.hris.repository.EmployeeRepository employeeRepository) {
        this.service = service;
        this.attendanceRepository = attendanceRepository;
        this.employeeRepository = employeeRepository;
    }

    @GetMapping
    public ResponseEntity<List<Attendance>> list(
            @RequestParam(value = "year", required = false) String year,
            @RequestParam(value = "month", required = false) String month,
            @RequestParam(value = "businessCenter", required = false) String businessCenter) {
        List<Attendance> records;
        if (businessCenter != null && !businessCenter.isBlank() && !"ALL".equalsIgnoreCase(businessCenter.trim())) {
            records = attendanceRepository.findByBusinessCenterSmart(businessCenter.trim());
        } else {
            records = attendanceRepository.findAll();
        }
        if (year != null && !year.isBlank()) {
            records = records.stream().filter(r -> year.equals(r.getAttYear())).toList();
        }
        if (month != null && !month.isBlank()) {
            records = records.stream().filter(r -> month.equals(r.getAttMonth())).toList();
        }
        return ResponseEntity.ok(records);
    }

    @PostMapping
    public Attendance create(@RequestBody Attendance attendance) {
        try {
            if (attendance.getDayIn() != null) {
                if (attendance.getAttYear() == null || attendance.getAttYear().isBlank()) {
                    attendance.setAttYear(String.valueOf(attendance.getDayIn().getYear()));
                }
                if (attendance.getAttMonth() == null || attendance.getAttMonth().isBlank()) {
                    attendance.setAttMonth(String.format("%02d", attendance.getDayIn().getMonthValue()));
                }
            }
            if (attendance.getAttMonth() != null && attendance.getAttMonth().trim().length() == 1) {
                attendance.setAttMonth(String.format("%02d", Integer.parseInt(attendance.getAttMonth().trim())));
            }
            if ((attendance.getEmployeeName() == null || attendance.getEmployeeName().isBlank()) && attendance.getEpfNo() != null) {
                employeeRepository.findByTrimmedEpfNo(attendance.getEpfNo().trim()).ifPresent(emp -> {
                    String fullName = ((emp.getFirstName() == null ? "" : emp.getFirstName()) + " " +
                                       (emp.getLastName() == null ? "" : emp.getLastName())).trim();
                    attendance.setEmployeeName(fullName);
                });
            }
            return attendanceRepository.save(attendance);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Attendance already exists for this employee and date."
            );
        }
    }

    @GetMapping("/{year}/{month}/{epfNo}/{dayIn}")
    public ResponseEntity<Attendance> get(@PathVariable String year,
                                        @PathVariable String month,
                                        @PathVariable String epfNo,
                                        @PathVariable LocalDate dayIn) {
        return attendanceRepository.findById(new com.hsb.hris.entity.id.AttendanceId(year, month, epfNo, dayIn))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{year}/{month}/{epfNo}/{dayIn}")
    public ResponseEntity<Attendance> update(@PathVariable String year,
                                           @PathVariable String month,
                                           @PathVariable String epfNo,
                                           @PathVariable LocalDate dayIn,
                                           @RequestBody Attendance attendance) {
        com.hsb.hris.entity.id.AttendanceId id = new com.hsb.hris.entity.id.AttendanceId(year.trim(), month.trim(), epfNo.trim(), dayIn);
        return attendanceRepository.findById(id).map(existing -> {
            existing.setPlantCode(attendance.getPlantCode());
            existing.setWorkingDays(attendance.getWorkingDays());
            existing.setOtCalAuto(attendance.getOtCalAuto());
            existing.setNoOfStaff(attendance.getNoOfStaff());
            existing.setAttAllowance(attendance.getAttAllowance());
            existing.setDaysForAttAllowance(attendance.getDaysForAttAllowance());
            existing.setPoyaSaturdayNormal(attendance.getPoyaSaturdayNormal());
            existing.setBasicSalary(attendance.getBasicSalary());
            existing.setDayAllowance(attendance.getDayAllowance());
            existing.setNightAllowance(attendance.getNightAllowance());
            existing.setTimeIn(attendance.getTimeIn());
            existing.setDayOut(attendance.getDayOut());
            existing.setTimeOut(attendance.getTimeOut());
            existing.setHalfDay(attendance.getHalfDay());
            existing.setTotalWorkingHours(attendance.getTotalWorkingHours());
            existing.setTotalOt(attendance.getTotalOt());
            existing.setNormalDay(attendance.getNormalDay());
            existing.setSaturdayPoya(attendance.getSaturdayPoya());
            existing.setSpecialDay(attendance.getSpecialDay());
            existing.setLateAllowNo(attendance.getLateAllowNo());
            existing.setDayShift(attendance.getDayShift());
            existing.setSecondShift(attendance.getSecondShift());
            existing.setNightShift(attendance.getNightShift());
            existing.setFullNight(attendance.getFullNight());
            existing.setNoOfMeal(attendance.getNoOfMeal());
            existing.setTotalMealValue(attendance.getTotalMealValue());
            existing.setStatutoryHolidays(attendance.getStatutoryHolidays());
            existing.setSundayPoyaExtra(attendance.getSundayPoyaExtra());
            existing.setBusinessCenter(attendance.getBusinessCenter());
            if (attendance.getEmployeeName() != null && !attendance.getEmployeeName().isBlank()) {
                existing.setEmployeeName(attendance.getEmployeeName());
            }
            return ResponseEntity.ok(attendanceRepository.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{year}/{month}/{epfNo}/{dayIn}")
    public ResponseEntity<Void> delete(@PathVariable String year,
                                      @PathVariable String month,
                                      @PathVariable String epfNo,
                                      @PathVariable LocalDate dayIn) {
        com.hsb.hris.entity.id.AttendanceId id = new com.hsb.hris.entity.id.AttendanceId(year, month, epfNo, dayIn);
        if (!attendanceRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        attendanceRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-employee/{epfNo}")
    public ResponseEntity<List<Attendance>> byEmployee(
            @PathVariable String epfNo,
            @RequestParam(value = "year", required = false) String year,
            @RequestParam(value = "month", required = false) String month) {
        List<Attendance> records = new java.util.ArrayList<>();
        if (year != null && !year.isBlank() && month != null && !month.isBlank()) {
            records = attendanceRepository.findByEpfNoAndAttYearAndAttMonth(epfNo, year, month);
        } else {
            records = attendanceRepository.findAll().stream()
                    .filter(r -> epfNo.equals(r.getEpfNo()))
                    .toList();
        }
        return ResponseEntity.ok(records);
    }

    @PostMapping("/excel-upload")
    public ResponseEntity<Map<String, Object>> upload(@RequestParam("file") MultipartFile file,
                                                     @RequestParam(value = "businessCenter", required = false) String businessCenter) throws Exception {
        Map<String, Object> res = service.uploadExcel(file, businessCenter);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/bulk")
    public ResponseEntity<?> bulkSave(@RequestBody List<Attendance> records) {
        if (records == null || records.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "No attendance records provided for saving."));
        }

        // Cache employee names for instant resolution
        Map<String, String> empNameMap = new java.util.HashMap<>();
        try {
            for (com.hsb.hris.entity.Employee emp : employeeRepository.findAll()) {
                if (emp.getEpfNo() != null) {
                    String fullName = ((emp.getFirstName() == null ? "" : emp.getFirstName()) + " " +
                                       (emp.getLastName() == null ? "" : emp.getLastName())).trim();
                    empNameMap.put(emp.getEpfNo().trim().toUpperCase(), fullName);
                }
            }
        } catch (Exception ignored) {}

        // Group rows by Plant, Year, and Month
        // Key: PlantCode|Year|Month
        Map<String, List<Attendance>> grouped = new java.util.LinkedHashMap<>();
        for (Attendance a : records) {
            if (a.getDayIn() != null) {
                if (a.getAttYear() == null || a.getAttYear().isBlank()) {
                    a.setAttYear(String.valueOf(a.getDayIn().getYear()));
                }
                if (a.getAttMonth() == null || a.getAttMonth().isBlank()) {
                    a.setAttMonth(String.format("%02d", a.getDayIn().getMonthValue()));
                }
            }
            if (a.getAttMonth() != null && a.getAttMonth().trim().length() == 1) {
                a.setAttMonth(String.format("%02d", Integer.parseInt(a.getAttMonth().trim())));
            }

            // Populate Employee Name
            if ((a.getEmployeeName() == null || a.getEmployeeName().isBlank()) && a.getEpfNo() != null) {
                String resolvedName = empNameMap.get(a.getEpfNo().trim().toUpperCase());
                if (resolvedName != null && !resolvedName.isBlank()) {
                    a.setEmployeeName(resolvedName);
                }
            }

            String plant = (a.getPlantCode() != null && !a.getPlantCode().isBlank()) ? a.getPlantCode().trim() : "DEFAULT";
            String yr = (a.getAttYear() != null && !a.getAttYear().isBlank()) ? a.getAttYear().trim() : "2026";
            String mo = (a.getAttMonth() != null && !a.getAttMonth().isBlank()) ? a.getAttMonth().trim() : "01";
            String groupKey = plant + "|" + yr + "|" + mo;

            grouped.computeIfAbsent(groupKey, k -> new java.util.ArrayList<>()).add(a);
        }

        List<Map<String, Object>> summaries = new java.util.ArrayList<>();
        List<Attendance> allSaved = new java.util.ArrayList<>();
        int grandInserted = 0;
        int grandUpdated = 0;
        int grandFailed = 0;

        for (Map.Entry<String, List<Attendance>> entry : grouped.entrySet()) {
            String[] parts = entry.getKey().split("\\|");
            String plantCode = parts[0];
            String yr = parts[1];
            String mo = parts[2];
            List<Attendance> groupRows = entry.getValue();

            int inserted = 0;
            int updated = 0;
            int failed = 0;
            java.util.Set<String> distinctEpfs = new java.util.HashSet<>();

            for (Attendance a : groupRows) {
                if (a.getEpfNo() != null) distinctEpfs.add(a.getEpfNo().trim().toUpperCase());
                try {
                    com.hsb.hris.entity.id.AttendanceId id = new com.hsb.hris.entity.id.AttendanceId(
                        a.getAttYear().trim(), a.getAttMonth().trim(), a.getEpfNo().trim(), a.getDayIn()
                    );
                    Attendance existing = attendanceRepository.findById(id).orElse(null);
                    if (existing != null) {
                        // Update existing row
                        existing.setPlantCode(a.getPlantCode());
                        existing.setWorkingDays(a.getWorkingDays());
                        existing.setOtCalAuto(a.getOtCalAuto());
                        existing.setNoOfStaff(a.getNoOfStaff());
                        existing.setAttAllowance(a.getAttAllowance());
                        existing.setDaysForAttAllowance(a.getDaysForAttAllowance());
                        existing.setPoyaSaturdayNormal(a.getPoyaSaturdayNormal());
                        existing.setBasicSalary(a.getBasicSalary());
                        existing.setDayAllowance(a.getDayAllowance());
                        existing.setNightAllowance(a.getNightAllowance());
                        existing.setTimeIn(a.getTimeIn());
                        existing.setDayOut(a.getDayOut());
                        existing.setTimeOut(a.getTimeOut());
                        existing.setHalfDay(a.getHalfDay());
                        existing.setTotalWorkingHours(a.getTotalWorkingHours());
                        existing.setTotalOt(a.getTotalOt());
                        existing.setNormalDay(a.getNormalDay());
                        existing.setSaturdayPoya(a.getSaturdayPoya());
                        existing.setSpecialDay(a.getSpecialDay());
                        existing.setLateAllowNo(a.getLateAllowNo());
                        existing.setDayShift(a.getDayShift());
                        existing.setSecondShift(a.getSecondShift());
                        existing.setNightShift(a.getNightShift());
                        existing.setFullNight(a.getFullNight());
                        existing.setNoOfMeal(a.getNoOfMeal());
                        existing.setTotalMealValue(a.getTotalMealValue());
                        existing.setStatutoryHolidays(a.getStatutoryHolidays());
                        existing.setSundayPoyaExtra(a.getSundayPoyaExtra());
                        existing.setBusinessCenter(a.getBusinessCenter());
                        if (a.getEmployeeName() != null && !a.getEmployeeName().isBlank()) {
                            existing.setEmployeeName(a.getEmployeeName());
                        }
                        Attendance saved = attendanceRepository.save(existing);
                        allSaved.add(saved);
                        updated++;
                    } else {
                        // Insert new row
                        Attendance saved = attendanceRepository.save(a);
                        allSaved.add(saved);
                        inserted++;
                    }
                } catch (Exception ex) {
                    failed++;
                }
            }

            grandInserted += inserted;
            grandUpdated += updated;
            grandFailed += failed;

            int totalGroupSaved = inserted + updated;
            String summaryMsg = String.format("Plant %s – %s/%s – %d employees – %d rows saved (inserted %d, updated %d, failed %d)",
                    plantCode, yr, mo, distinctEpfs.size(), totalGroupSaved, inserted, updated, failed);

            Map<String, Object> groupSummary = new java.util.LinkedHashMap<>();
            groupSummary.put("plantCode", plantCode);
            groupSummary.put("year", yr);
            groupSummary.put("month", mo);
            groupSummary.put("employeeCount", distinctEpfs.size());
            groupSummary.put("totalRows", totalGroupSaved);
            groupSummary.put("inserted", inserted);
            groupSummary.put("updated", updated);
            groupSummary.put("failed", failed);
            groupSummary.put("message", summaryMsg);
            summaries.add(groupSummary);
        }

        Map<String, Object> response = new java.util.LinkedHashMap<>();
        response.put("status", "success");
        response.put("totalSaved", grandInserted + grandUpdated);
        response.put("inserted", grandInserted);
        response.put("updated", grandUpdated);
        response.put("failed", grandFailed);
        response.put("summaries", summaries);
        if (!summaries.isEmpty()) {
            response.put("message", summaries.get(0).get("message"));
        } else {
            response.put("message", String.format("%d rows saved (inserted %d, updated %d, failed %d)",
                    grandInserted + grandUpdated, grandInserted, grandUpdated, grandFailed));
        }
        response.put("records", allSaved);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/auto-update")
    public ResponseEntity<Map<String, Object>> autoUpdate(@RequestParam String year, @RequestParam String month,
                                                          @RequestParam(required = false) String businessCenter) {
        Map<String, Object> res = service.autoUpdate(year, month, businessCenter);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/query-analyzer")
    public ResponseEntity<List<Map<String, Object>>> queryAnalyzer(@RequestBody Map<String, Object> filters) {
        List<Map<String, Object>> res = service.queryAnalyzer(filters);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/excel-template")
    public ResponseEntity<org.springframework.core.io.ByteArrayResource> downloadTemplate() throws Exception {
        String[] headers = new String[]{
                "Att_Year", "Att_Month", "EPF_No", "Plant_Code", "Day_In (yyyy-MM-dd)", "Time_In (HH:mm)",
                "Day_Out (yyyy-MM-dd HH:mm)", "Time_Out (HH:mm)", "Half_Day (0/1)", "Day_Shift (Y/N)",
                "Second_Shift (Y/N)", "Night_Shift (Y/N)", "Full_Night (Y/N)", "Normal_Day (Y/N)",
                "Saturday_Poya (Y/N)", "Special_Day (Y/N)", "Statutory_Holidays (0/1)", "No_Of_Meal", "Business_Center"
        };

        try (org.apache.poi.ss.usermodel.Workbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook(); java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream()) {
            org.apache.poi.ss.usermodel.Sheet sheet = wb.createSheet("AttendanceTemplate");
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
                sheet.setColumnWidth(i, 20 * 256);
            }
            wb.write(out);
            byte[] bytes = out.toByteArray();
            org.springframework.core.io.ByteArrayResource resource = new org.springframework.core.io.ByteArrayResource(bytes);
            return ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=attendance-template.xlsx")
                    .contentLength(bytes.length)
                    .contentType(org.springframework.http.MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);
        }
    }
}
