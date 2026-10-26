package com.hsb.hris.controller;

import com.hsb.hris.entity.Attendance;
import com.hsb.hris.entity.BusinessCenter;
import com.hsb.hris.entity.DeductionTransaction;
import com.hsb.hris.entity.Employee;
import com.hsb.hris.entity.SalaryCalculation;
import com.hsb.hris.repository.AttendanceRepository;
import com.hsb.hris.repository.BusinessCenterRepository;
import com.hsb.hris.repository.DeductionTransactionRepository;
import com.hsb.hris.repository.EmployeeRepository;
import com.hsb.hris.repository.SalaryCalculationRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final AttendanceRepository attendanceRepository;
    private final DeductionTransactionRepository deductionRepository;
    private final SalaryCalculationRepository salaryRepository;
    private final EmployeeRepository employeeRepository;
    private final BusinessCenterRepository businessCenterRepository;

    public ReportController(AttendanceRepository attendanceRepository,
                            DeductionTransactionRepository deductionRepository,
                            SalaryCalculationRepository salaryRepository,
                            EmployeeRepository employeeRepository,
                            BusinessCenterRepository businessCenterRepository) {
        this.attendanceRepository = attendanceRepository;
        this.deductionRepository = deductionRepository;
        this.salaryRepository = salaryRepository;
        this.employeeRepository = employeeRepository;
        this.businessCenterRepository = businessCenterRepository;
    }

    @GetMapping("/attendance")
    public List<Attendance> attendance(
            @RequestParam String businessCenter,
            @RequestParam String year,
            @RequestParam String month,
            @RequestParam(required = false) String plant) {
        String cleanMonth = normalizeMonth(month);
        List<Attendance> rows = "ALL".equalsIgnoreCase(businessCenter.trim())
                ? attendanceRepository.findAll()
                : attendanceRepository.findByBusinessCenterSmart(businessCenter.trim());
        return rows.stream()
                .filter(row -> year.equals(row.getAttYear()))
                .filter(row -> cleanMonth.equals(row.getAttMonth()))
                .filter(row -> plant == null || plant.isBlank() || "ALL".equalsIgnoreCase(plant)
                        || plant.trim().equalsIgnoreCase(row.getPlantCode()))
                .toList();
    }

    @GetMapping("/salary-advance")
    public List<Map<String, Object>> salaryAdvance(
            @RequestParam String businessCenter,
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(required = false) String plant) {
        Map<String, String> empPlantMap = new LinkedHashMap<>();
        for (Employee emp : employeeRepository.findAll()) {
            if (emp.getEpfNo() != null) {
                empPlantMap.put(emp.getEpfNo().trim().toUpperCase(), emp.getPlantCode() == null ? "" : emp.getPlantCode().trim());
            }
        }
        return deductionsForMonth(businessCenter, year, month).stream()
                .filter(row -> contains(row.getDidCode(), "adv") || contains(row.getDeductionName(), "advance"))
                .map(row -> {
                    String epf = row.getEpfNo() == null ? "" : row.getEpfNo().trim();
                    String plantCode = empPlantMap.getOrDefault(epf.toUpperCase(), "");
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", row.getId());
                    map.put("epfNo", epf);
                    map.put("employeeName", row.getEmployeeName());
                    map.put("plantCode", plantCode);
                    map.put("businessCenter", row.getBusinessCenter());
                    map.put("totalAmount", row.getTotalAmount());
                    map.put("monthlyCut", row.getMonthlyCut());
                    map.put("startDate", row.getStartDate());
                    map.put("endDate", row.getEndDate());
                    map.put("status", row.getStatus());
                    return map;
                })
                .filter(row -> plant == null || plant.isBlank() || "ALL".equalsIgnoreCase(plant.trim())
                        || plant.trim().equalsIgnoreCase((String) row.get("plantCode")))
                .toList();
    }

    @GetMapping("/salary-advance-total")
    public List<Map<String, Object>> salaryAdvanceTotal(
            @RequestParam String businessCenter,
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(required = false) String plant) {
        // Group by plant wise or business center wise
        Map<String, BigDecimal> plantTotals = new LinkedHashMap<>();
        Map<String, BigDecimal> totalAdvanceMap = new LinkedHashMap<>();
        for (Map<String, Object> item : salaryAdvance(businessCenter, year, month, plant)) {
            String p = (String) item.get("plantCode");
            if (p == null || p.isBlank()) p = (String) item.get("businessCenter");
            BigDecimal cut = (BigDecimal) item.get("monthlyCut");
            BigDecimal tot = (BigDecimal) item.get("totalAmount");
            if (cut != null) {
                plantTotals.merge(p, cut, BigDecimal::add);
            }
            if (tot != null) {
                totalAdvanceMap.merge(p, tot, BigDecimal::add);
            }
        }
        List<Map<String, Object>> result = new ArrayList<>();
        plantTotals.forEach((code, monthlyCut) -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("plantCode", code);
            row.put("companyName", code);
            row.put("amount", monthlyCut);
            row.put("totalAdvance", totalAdvanceMap.getOrDefault(code, BigDecimal.ZERO));
            result.add(row);
        });
        return result;
    }

    /**
     * Returns total outstanding advance per employee (sum of totalAmount from all active advance
     * DeductionTransaction records) for the selected business center and optional plant.
     */
    @GetMapping("/advance-total-per-employee")
    public List<Map<String, Object>> advanceTotalPerEmployee(
            @RequestParam String businessCenter,
            @RequestParam(required = false) String plant) {
        Map<String, String> empPlantMap = new LinkedHashMap<>();
        for (Employee emp : employeeRepository.findAll()) {
            if (emp.getEpfNo() != null) {
                empPlantMap.put(emp.getEpfNo().trim().toUpperCase(), emp.getPlantCode() == null ? "" : emp.getPlantCode().trim());
            }
        }
        List<DeductionTransaction> allTransactions = deductionRepository.findAll();
        Map<String, Map<String, Object>> empMap = new LinkedHashMap<>();
        for (DeductionTransaction dt : allTransactions) {
            if (!"ACTIVE".equalsIgnoreCase(dt.getStatus())) continue;
            boolean isAdvance = (dt.getDidCode() != null && dt.getDidCode().toLowerCase().contains("adv"))
                    || (dt.getDeductionName() != null && dt.getDeductionName().toLowerCase().contains("advance"));
            if (!isAdvance) continue;
            if (!matches(dt.getBusinessCenter(), businessCenter)) continue;
            String epf = dt.getEpfNo() == null ? "" : dt.getEpfNo().trim();
            String plantCode = empPlantMap.getOrDefault(epf.toUpperCase(), "");
            if (plant != null && !plant.isBlank() && !"ALL".equalsIgnoreCase(plant.trim())
                    && !plant.trim().equalsIgnoreCase(plantCode)) {
                continue;
            }
            Map<String, Object> row = empMap.computeIfAbsent(epf, k -> {
                Map<String, Object> r = new LinkedHashMap<>();
                r.put("epfNo", epf);
                r.put("employeeName", dt.getEmployeeName());
                r.put("plantCode", plantCode);
                r.put("businessCenter", dt.getBusinessCenter());
                r.put("totalAdvance", BigDecimal.ZERO);
                r.put("monthlyCut", BigDecimal.ZERO);
                return r;
            });
            if (dt.getTotalAmount() != null) {
                row.put("totalAdvance", ((BigDecimal) row.get("totalAdvance")).add(dt.getTotalAmount()));
            }
            if (dt.getMonthlyCut() != null) {
                row.put("monthlyCut", ((BigDecimal) row.get("monthlyCut")).add(dt.getMonthlyCut()));
            }
        }
        return new ArrayList<>(empMap.values());
    }

    @GetMapping("/loan")
    public List<DeductionTransaction> loan(
            @RequestParam String businessCenter,
            @RequestParam int year,
            @RequestParam int month) {
        return deductionsForMonth(businessCenter, year, month).stream()
                .filter(row -> Boolean.TRUE.equals(row.getLoan()))
                .toList();
    }

    @GetMapping("/telephone-deduct")
    public List<DeductionTransaction> telephoneDeduct(
            @RequestParam String businessCenter,
            @RequestParam int year,
            @RequestParam int month) {
        return deductionsForMonth(businessCenter, year, month).stream()
                .filter(row -> contains(row.getDidCode(), "tel")
                        || contains(row.getDeductionName(), "telephone")
                        || contains(row.getDeductionName(), "phone"))
                .toList();
    }

    @GetMapping("/epf-c-form")
    public List<Map<String, Object>> epfCForm(
            @RequestParam String businessCenter,
            @RequestParam String year,
            @RequestParam String month) {
        String cleanMonth = normalizeMonth(month);
        Map<String, Employee> employees = new LinkedHashMap<>();
        employeeRepository.findAll().forEach(employee -> {
            if (employee.getEpfNo() != null) {
                employees.putIfAbsent(employee.getEpfNo().trim().toUpperCase(), employee);
            }
        });
        List<SalaryCalculation> calculations = salaryRepository.findByYearAndMonth(year.trim(), cleanMonth);
        return calculations.stream()
                .filter(row -> matches(row.getBusinessCenter(), businessCenter))
                .map(row -> {
                    Employee employee = employees.get(row.getEpfNo().trim().toUpperCase());
                    Map<String, Object> result = new LinkedHashMap<>();
                    result.put("epfNo", row.getEpfNo());
                    result.put("employeeName", row.getEmployeeName());
                    result.put("nicNo", employee == null ? null : employee.getNicNo());
                    result.put("earning", row.getGrossSalary());
                    result.put("businessCenter", row.getBusinessCenter());
                    return result;
                })
                .toList();
    }

    private List<DeductionTransaction> deductionsForMonth(String businessCenter, int year, int month) {
        if (month < 1 || month > 12) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Month must be between 1 and 12.");
        }
        LocalDate from = LocalDate.of(year, month, 1);
        LocalDate to = from.withDayOfMonth(from.lengthOfMonth());
        return deductionRepository.findAll().stream()
                .filter(row -> matches(row.getBusinessCenter(), businessCenter))
                .filter(row -> "ACTIVE".equalsIgnoreCase(row.getStatus()))
                .filter(row -> !row.getStartDate().isAfter(to) && !row.getEndDate().isBefore(from))
                .toList();
    }

    private String normalizeMonth(String month) {
        int value;
        try {
            value = Integer.parseInt(month.trim());
        } catch (NumberFormatException exception) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Month must be numeric.");
        }
        if (value < 1 || value > 12) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Month must be between 1 and 12.");
        }
        return String.format("%02d", value);
    }

    private boolean matches(String value, String filter) {
        return filter == null || filter.isBlank() || "ALL".equalsIgnoreCase(filter.trim())
                || (value != null && value.trim().equalsIgnoreCase(filter.trim()));
    }

    private boolean contains(String value, String term) {
        return value != null && value.toLowerCase().contains(term);
    }
}
