package com.hsb.hris.controller.transaction;

import com.hsb.hris.entity.Attendance;
import com.hsb.hris.repository.AttendanceRepository;
import com.hsb.hris.service.transaction.AttendanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/attendance", "/api/transaction/attendance"})
public class AttendanceController {

    private final AttendanceService service;
    private final AttendanceRepository attendanceRepository;

    public AttendanceController(AttendanceService service, AttendanceRepository attendanceRepository) {
        this.service = service;
        this.attendanceRepository = attendanceRepository;
    }

    @GetMapping
    public ResponseEntity<List<Attendance>> list(
            @RequestParam(value = "year", required = false) String year,
            @RequestParam(value = "month", required = false) String month,
            @RequestParam(value = "businessCenter", required = false) String businessCenter) {
        List<Attendance> records = attendanceRepository.findAll();
        if (year != null && !year.isBlank()) {
            records = records.stream().filter(r -> year.equals(r.getAttYear())).toList();
        }
        if (month != null && !month.isBlank()) {
            records = records.stream().filter(r -> month.equals(r.getAttMonth())).toList();
        }
        if (businessCenter != null && !businessCenter.isBlank()) {
            records = records.stream().filter(r -> businessCenter.equalsIgnoreCase(r.getBusinessCenter())).toList();
        }
        return ResponseEntity.ok(records);
    }

    @PostMapping
    public Attendance create(@RequestBody Attendance attendance) {
        return attendanceRepository.save(attendance);
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
        com.hsb.hris.entity.id.AttendanceId id = new com.hsb.hris.entity.id.AttendanceId(year, month, epfNo, dayIn);
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
