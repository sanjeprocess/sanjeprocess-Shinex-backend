package com.hsb.hris.controller.transaction;

import com.hsb.hris.entity.Attendance;
import com.hsb.hris.entity.Employee;
import com.hsb.hris.entity.StatutoryHoliday;
import com.hsb.hris.repository.AttendanceRepository;
import com.hsb.hris.service.master.EmployeeService;
import com.hsb.hris.service.transaction.StatutoryHolidayService;
import com.hsb.hris.service.transaction.StatutoryHolidayService.EmployeePay;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

@RestController
@RequestMapping("/api/statutory-holidays")
public class StatutoryHolidayController {

    private final StatutoryHolidayService service;
    private final EmployeeService employees;
    private final AttendanceRepository attendanceRepo;

    public StatutoryHolidayController(StatutoryHolidayService service, EmployeeService employees,
                                      AttendanceRepository attendanceRepo) {
        this.service = service; this.employees = employees; this.attendanceRepo = attendanceRepo;
    }

    /** Holidays of the month plus every employee's Working / Not Working status and pay. */
    @GetMapping
    public Map<String, Object> month(@RequestParam String year, @RequestParam String month,
                                     @RequestParam(value = "businessCenter", required = false) String businessCenter) {
        String yr = year.trim(), mo = String.format("%02d", Integer.parseInt(month.trim()));
        List<StatutoryHoliday> monthHolidays = service.holidaysForMonth(yr, mo);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("year", yr);
        result.put("month", mo);
        result.put("holidays", monthHolidays);

        List<Map<String, Object>> rows = new ArrayList<>();
        if (!monthHolidays.isEmpty()) {
            String bc = businessCenter == null ? "" : businessCenter.split(" / ", 2)[0].trim();
            List<Employee> empList = bc.isEmpty() || "ALL".equalsIgnoreCase(bc)
                    ? employees.findAll() : employees.findByBusinessCenter(bc);
            Map<String, String> overrides = service.overridesForMonth(yr, mo);
            List<Attendance> monthAttendance = attendanceRepo.findByAttYearAndAttMonth(yr, mo);

            for (Employee e : empList) {
                if (e.getEpfNo() == null || e.getEpfNo().isBlank()) continue;
                EmployeePay pay = service.computeForEmployee(e, monthHolidays, overrides, monthAttendance);
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("epfNo", pay.epfNo);
                row.put("employeeName", ((e.getFirstName() == null ? "" : e.getFirstName()) + " " +
                        (e.getLastName() == null ? "" : e.getLastName())).trim());
                row.put("plantCode", e.getPlantCode());
                row.put("eligible", pay.eligible);
                row.put("total", pay.total);
                row.put("lines", pay.lines);
                rows.add(row);
            }
            rows.sort(Comparator.comparing(r -> String.valueOf(r.get("epfNo"))));
        }
        result.put("employees", rows);
        return result;
    }

    @PutMapping("/{date}")
    public ResponseEntity<?> save(@PathVariable String date, @RequestBody StatutoryHoliday body) {
        LocalDate d = parseDate(date);
        if (d == null) return ResponseEntity.badRequest().body("Invalid holiday date: " + date);
        body.setHolidayDate(d);
        return ResponseEntity.ok(service.saveHoliday(body));
    }

    @DeleteMapping("/{date}")
    public ResponseEntity<Void> delete(@PathVariable String date) {
        LocalDate d = parseDate(date);
        if (d == null) return ResponseEntity.badRequest().build();
        return service.deleteHoliday(d) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    /** Body: { "epfNo": "...", "status": "W" | "N" | "AUTO" } - AUTO clears the manual selection. */
    @PutMapping("/{date}/status")
    public ResponseEntity<?> setStatus(@PathVariable String date, @RequestBody Map<String, String> body) {
        LocalDate d = parseDate(date);
        if (d == null) return ResponseEntity.badRequest().body("Invalid holiday date: " + date);
        if (!service.holidayExists(d)) return ResponseEntity.badRequest().body(d + " is not a statutory holiday");
        String epf = body.get("epfNo");
        if (epf == null || epf.isBlank()) return ResponseEntity.badRequest().body("epfNo is required");
        String status = body.get("status") == null ? "" : body.get("status").trim().toUpperCase();
        service.setStatus(d, epf, status);
        return ResponseEntity.ok(Map.of("holidayDate", d.toString(), "epfNo", epf.trim(), "status", status));
    }

    private LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException | NullPointerException ex) {
            return null;
        }
    }
}
