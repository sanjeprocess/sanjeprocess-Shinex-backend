package com.hsb.hris.controller;

import com.hsb.hris.entity.Attendance;
import com.hsb.hris.entity.BusinessCenter;
import com.hsb.hris.entity.DeductionTransaction;
import com.hsb.hris.entity.DeductionMaster;
import com.hsb.hris.entity.Employee;
import com.hsb.hris.entity.Plant;
import com.hsb.hris.entity.SalaryCalculation;
import com.hsb.hris.entity.TDeduction;
import com.hsb.hris.repository.AttendanceRepository;
import com.hsb.hris.repository.BusinessCenterRepository;
import com.hsb.hris.repository.DeductionMasterRepository;
import com.hsb.hris.repository.DeductionTransactionRepository;
import com.hsb.hris.repository.EmployeeRepository;
import com.hsb.hris.repository.PlantRepository;
import com.hsb.hris.repository.SalaryCalculationRepository;
import com.hsb.hris.repository.TDeductionRepository;
import com.hsb.hris.service.ExcelStyleSalaryService;
import com.hsb.hris.service.ExcelStyleSalaryService.SalarySheetRow;
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
    private final TDeductionRepository tDeductionRepository;
    private final SalaryCalculationRepository salaryRepository;
    private final EmployeeRepository employeeRepository;
    private final BusinessCenterRepository businessCenterRepository;
    private final DeductionMasterRepository deductionMasterRepository;
    private final PlantRepository plantRepository;
    private final ExcelStyleSalaryService salaryService;

    public ReportController(AttendanceRepository attendanceRepository,
                            DeductionTransactionRepository deductionRepository,
                            TDeductionRepository tDeductionRepository,
                            SalaryCalculationRepository salaryRepository,
                            EmployeeRepository employeeRepository,
                            BusinessCenterRepository businessCenterRepository,
                            DeductionMasterRepository deductionMasterRepository,
                            PlantRepository plantRepository,
                            ExcelStyleSalaryService salaryService) {
        this.attendanceRepository = attendanceRepository;
        this.deductionRepository = deductionRepository;
        this.tDeductionRepository = tDeductionRepository;
        this.salaryRepository = salaryRepository;
        this.employeeRepository = employeeRepository;
        this.businessCenterRepository = businessCenterRepository;
        this.deductionMasterRepository = deductionMasterRepository;
        this.plantRepository = plantRepository;
        this.salaryService = salaryService;
    }

    @GetMapping("/attendance")
    public List<Attendance> attendance(
            @RequestParam String businessCenter,
            @RequestParam String year,
            @RequestParam String month,
            @RequestParam(required = false) String plant) {
        String cleanMonth = normalizeMonth(month);
        List<Attendance> rows = "ALL".equalsIgnoreCase(businessCenter.trim())
                ? attendanceRepository.findByAttYearAndAttMonth(year.trim(), cleanMonth)
                : attendanceRepository.findByBusinessCenterSmart(businessCenter.trim());
        return rows.stream()
                .filter(row -> year.trim().equals(trim(row.getAttYear())))
                .filter(row -> cleanMonth.equals(trim(row.getAttMonth())))
                .filter(row -> matches(row.getPlantCode() == null || row.getPlantCode().isBlank() ? row.getCustomerCode() : row.getPlantCode(), plant))
                .toList();
    }

    /**
     * Plant-wise attendance totals for the month (one row per company / plant).
     */
    @GetMapping("/attendance-total")
    public List<Map<String, Object>> attendanceTotal(
            @RequestParam String businessCenter,
            @RequestParam String year,
            @RequestParam String month) {
        Map<String, String> plantNames = plantNameMap();
        Map<String, Map<String, Object>> totals = new LinkedHashMap<>();
        Map<String, java.util.Set<String>> employeesByPlant = new LinkedHashMap<>();
        for (Attendance row : attendance(businessCenter, year, month, null)) {
            String code = trim(row.getPlantCode());
            if (code.isEmpty()) code = trim(row.getCustomerCode());
            String key = code.toUpperCase();
            Map<String, Object> total = totals.computeIfAbsent(key, k -> {
                Map<String, Object> r = new LinkedHashMap<>();
                r.put("plantCode", k);
                r.put("companyName", plantNames.getOrDefault(k, k));
                r.put("employees", 0);
                r.put("daysWorked", 0.0);
                r.put("halfDays", 0);
                r.put("totalHours", 0.0);
                r.put("otHours", 0.0);
                return r;
            });
            boolean half = isHalfDay(row.getHalfDay());
            employeesByPlant.computeIfAbsent(key, k -> new java.util.HashSet<>()).add(trim(row.getEpfNo()).toUpperCase());
            total.put("daysWorked", (Double) total.get("daysWorked") + (half ? 0.5 : 1.0));
            if (half) total.put("halfDays", (Integer) total.get("halfDays") + 1);
            total.put("totalHours", (Double) total.get("totalHours") + (row.getTotalWorkingHours() == null ? 0.0 : row.getTotalWorkingHours()));
            total.put("otHours", (Double) total.get("otHours") + (row.getTotalOt() == null ? 0.0 : row.getTotalOt()));
        }
        totals.forEach((key, total) -> total.put("employees", employeesByPlant.getOrDefault(key, java.util.Set.of()).size()));
        return totals.values().stream()
                .sorted(java.util.Comparator.comparing(r -> String.valueOf(r.get("companyName")), String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @GetMapping("/salary-advance")
    public List<Map<String, Object>> salaryAdvance(
            @RequestParam String businessCenter,
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(required = false) String plant) {
        Map<String, String> empPlantMap = new LinkedHashMap<>();
        Map<String, String> empNameMap = new LinkedHashMap<>();
        Map<String, Employee> empMap = new LinkedHashMap<>();
        java.util.Set<String> advanceCodes = advanceDeductionCodes();
        for (Employee emp : employeeRepository.findAll()) {
            if (emp.getEpfNo() != null) {
                String epfUpper = emp.getEpfNo().trim().toUpperCase();
                empMap.put(epfUpper, emp);
                empPlantMap.put(epfUpper, codeOf(emp.getPlantCode()));
                String fullName = ((emp.getFirstName() == null ? "" : emp.getFirstName()) + " " +
                                   (emp.getLastName() == null ? "" : emp.getLastName())).trim();
                empNameMap.put(epfUpper, fullName);
            }
        }

        List<Map<String, Object>> advances = new ArrayList<>();
        java.util.Set<String> seenEpfs = new java.util.HashSet<>();

        // 1. Check DeductionTransaction records (multi-month advance cuts)
        // A monthly entry counts only when the employee has no advance transaction, same as the salary sheet
        for (DeductionTransaction row : deductionsForMonth(businessCenter, year, month)) {
            if (isAdvance(row.getDidCode(), row.getDeductionName(), advanceCodes)) {
                String epf = row.getEpfNo() == null ? "" : row.getEpfNo().trim();
                seenEpfs.add(epf.toUpperCase());
                String plantCode = empPlantMap.getOrDefault(epf.toUpperCase(), "");
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id", row.getId());
                map.put("epfNo", epf);
                map.put("employeeName", row.getEmployeeName() != null ? row.getEmployeeName() : empNameMap.getOrDefault(epf.toUpperCase(), epf));
                map.put("plantCode", plantCode);
                map.put("businessCenter", row.getBusinessCenter());
                map.put("totalAmount", row.getTotalAmount());
                map.put("monthlyCut", row.getMonthlyCut());
                map.put("startDate", row.getStartDate());
                map.put("endDate", row.getEndDate());
                map.put("status", row.getStatus());
                putBankDetails(map, empMap.get(epf.toUpperCase()));
                advances.add(map);
            }
        }

        // 2. Also check monthly TDeduction records (TBL_T_Diduction) for any didCode containing 'adv'
        try {
            String strYear = String.valueOf(year);
            String strMonth = String.format("%02d", month);
            List<TDeduction> tDeds = (businessCenter == null || businessCenter.isBlank() || "ALL".equalsIgnoreCase(businessCenter.trim()))
                    ? tDeductionRepository.findAll()
                    : tDeductionRepository.findByBusinessCenterSmart(businessCenter.trim());
            for (TDeduction td : tDeds) {
                if (isAdvance(td.getDidCode(), null, advanceCodes)) {
                    boolean monthMatch = (strMonth.equals(trim(td.getAddMonth())) && strYear.equals(trim(td.getAddYear())))
                            || "Y".equalsIgnoreCase(td.getEveryMonth()) || "true".equalsIgnoreCase(td.getEveryMonth());
                    if (monthMatch) {
                        String epf = td.getEpfNo() == null ? "" : td.getEpfNo().trim();
                        if (!seenEpfs.contains(epf.toUpperCase())) {
                            String plantCode = empPlantMap.getOrDefault(epf.toUpperCase(), "");
                            String empName = empNameMap.getOrDefault(epf.toUpperCase(), epf);
                            BigDecimal amount = td.getDidAmount() != null ? BigDecimal.valueOf(td.getDidAmount()) : BigDecimal.ZERO;
                            Map<String, Object> map = new LinkedHashMap<>();
                            map.put("id", epf + "_" + td.getDidCode() + "_" + strYear + strMonth);
                            map.put("epfNo", epf);
                            map.put("employeeName", empName);
                            map.put("plantCode", plantCode);
                            map.put("businessCenter", td.getBusinessCenter());
                            map.put("totalAmount", amount);
                            map.put("monthlyCut", amount);
                            map.put("startDate", LocalDate.of(year, month, 1));
                            map.put("endDate", LocalDate.of(year, month, 1).withDayOfMonth(LocalDate.of(year, month, 1).lengthOfMonth()));
                            map.put("status", "Active");
                            putBankDetails(map, empMap.get(epf.toUpperCase()));
                            advances.add(map);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            // TDeduction query failure fallback
        }

        return advances.stream()
                .filter(row -> matches((String) row.get("plantCode"), plant))
                .toList();
    }

    @GetMapping("/salary-advance-total")
    public List<Map<String, Object>> salaryAdvanceTotal(
            @RequestParam String businessCenter,
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(required = false) String plant) {
        // Group by plant wise or business center wise
        Map<String, String> plantNames = plantNameMap();
        Map<String, BigDecimal> plantTotals = new LinkedHashMap<>();
        Map<String, BigDecimal> totalAdvanceMap = new LinkedHashMap<>();
        for (Map<String, Object> item : salaryAdvance(businessCenter, year, month, plant)) {
            String p = (String) item.get("plantCode");
            if (p == null || p.isBlank()) p = (String) item.get("businessCenter");
            p = p == null ? "" : p.trim().toUpperCase();
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
            row.put("companyName", plantNames.getOrDefault(code, code));
            row.put("amount", monthlyCut);
            row.put("totalAdvance", totalAdvanceMap.getOrDefault(code, BigDecimal.ZERO));
            result.add(row);
        });
        result.sort(java.util.Comparator.comparing(r -> String.valueOf(r.get("companyName")), String.CASE_INSENSITIVE_ORDER));
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
        List<DeductionTransaction> allTransactions;
        try {
            allTransactions = deductionRepository.findAll();
        } catch (Exception ex) {
            allTransactions = new ArrayList<>();
        }
        java.util.Set<String> advanceCodes = advanceDeductionCodes();
        Map<String, Map<String, Object>> empMap = new LinkedHashMap<>();
        for (DeductionTransaction dt : allTransactions) {
            if (!"ACTIVE".equalsIgnoreCase(trim(dt.getStatus()))) continue;
            if (!isAdvance(dt.getDidCode(), dt.getDeductionName(), advanceCodes)) continue;
            if (!matches(dt.getBusinessCenter(), businessCenter)) continue;
            String epf = dt.getEpfNo() == null ? "" : dt.getEpfNo().trim();
            String plantCode = empPlantMap.getOrDefault(epf.toUpperCase(), "");
            if (!matches(plantCode, plant)) continue;
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
        return deductionRowsOfType(businessCenter, year, month, (code, name, master, row) ->
                contains(code, "tel") || contains(name, "tel") || contains(name, "phone"));
    }

    @GetMapping("/uniform")
    public List<DeductionTransaction> uniform(
            @RequestParam String businessCenter,
            @RequestParam int year,
            @RequestParam int month) {
        return deductionRowsOfType(businessCenter, year, month, (code, name, master, row) ->
                contains(name, "uniform") || (row != null && Boolean.TRUE.equals(row.getUniform()))
                        || (master != null && isYes(master.getIfUniform())));
    }

    /** Salary sheet of the month: every column, one row per employee and plant. */
    @GetMapping("/salary-sheet")
    public List<SalarySheetRow> salarySheet(
            @RequestParam String businessCenter,
            @RequestParam String year,
            @RequestParam String month,
            @RequestParam(required = false) String plant) {
        Map<String, String> plantNames = plantNameMap();
        return salaryService.buildSalarySheet(year, month, plant, businessCenter).stream()
                .filter(row -> row.hasAttendance)
                .peek(row -> row.plantName = plantNames.getOrDefault(row.plantCode.toUpperCase(), row.plantCode))
                .toList();
    }

    /** Company-wise totals of the salary sheet. */
    @GetMapping("/salary-sheet-total")
    public List<Map<String, Object>> salarySheetTotal(
            @RequestParam String businessCenter,
            @RequestParam String year,
            @RequestParam String month) {
        Map<String, Map<String, Object>> totals = new LinkedHashMap<>();
        for (SalarySheetRow row : salarySheet(businessCenter, year, month, null)) {
            Map<String, Object> total = totals.computeIfAbsent(row.plantCode.toUpperCase(), key -> {
                Map<String, Object> r = new LinkedHashMap<>();
                r.put("plantCode", row.plantCode);
                r.put("companyName", row.plantName);
                r.put("netSalary", 0.0);
                r.put("epf12", 0.0);
                r.put("etf3", 0.0);
                return r;
            });
            total.put("netSalary", (Double) total.get("netSalary") + row.netSalary);
            total.put("epf12", (Double) total.get("epf12") + row.epf12);
            total.put("etf3", (Double) total.get("etf3") + row.etf3);
        }
        return totals.values().stream()
                .sorted(java.util.Comparator.comparing(r -> String.valueOf(r.get("companyName")), String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @GetMapping("/epf-c-form")
    public List<Map<String, Object>> epfCForm(
            @RequestParam String businessCenter,
            @RequestParam String year,
            @RequestParam String month) {
        // One line per employee; Total Earning is the amount EPF is charged on, not gross salary
        Map<String, Map<String, Object>> byEmployee = new LinkedHashMap<>();
        for (SalarySheetRow row : salaryService.buildSalarySheet(year, normalizeMonth(month), null, businessCenter)) {
            if (row.epfBase <= 0) continue;
            Map<String, Object> result = byEmployee.computeIfAbsent(row.epfNo.toUpperCase(), key -> {
                Map<String, Object> r = new LinkedHashMap<>();
                r.put("epfNo", row.epfNo);
                r.put("employeeName", row.employeeName);
                r.put("nicNo", row.nicNo);
                r.put("earning", 0.0);
                r.put("businessCenter", row.businessCenter);
                return r;
            });
            result.put("earning", Math.round(((Double) result.get("earning") + row.epfBase) * 100.0) / 100.0);
        }
        return byEmployee.values().stream()
                .sorted(java.util.Comparator
                        .comparing((Map<String, Object> r) -> String.valueOf(r.get("epfNo")).length())
                        .thenComparing(r -> String.valueOf(r.get("epfNo"))))
                .toList();
    }

    @FunctionalInterface
    private interface DeductionMatcher {
        boolean test(String code, String name, DeductionMaster master, DeductionTransaction row);
    }

    // Deduction transactions active in the month plus monthly entries (TBL_T_Diduction) of the same kind
    private List<DeductionTransaction> deductionRowsOfType(String businessCenter, int year, int month, DeductionMatcher matcher) {
        Map<String, DeductionMaster> masters = new LinkedHashMap<>();
        try {
            for (DeductionMaster master : deductionMasterRepository.findAll()) {
                if (master.getDudCode() != null) masters.put(master.getDudCode().trim().toUpperCase(), master);
            }
        } catch (Exception ex) {
            // fall back to code / name matching only
        }
        List<DeductionTransaction> rows = new ArrayList<>();
        java.util.Set<String> seen = new java.util.HashSet<>();
        for (DeductionTransaction row : deductionsForMonth(businessCenter, year, month)) {
            DeductionMaster master = masters.get(trim(row.getDidCode()).toUpperCase());
            String name = row.getDeductionName() != null ? row.getDeductionName() : (master == null ? null : master.getDudName());
            if (matcher.test(row.getDidCode(), name, master, row)) {
                rows.add(row);
                seen.add(trim(row.getEpfNo()).toUpperCase() + "|" + trim(row.getDidCode()).toUpperCase());
            }
        }
        String strYear = String.valueOf(year);
        String strMonth = String.format("%02d", month);
        List<TDeduction> monthly = "ALL".equalsIgnoreCase(businessCenter.trim())
                ? tDeductionRepository.findAll()
                : tDeductionRepository.findByBusinessCenterSmart(businessCenter.trim());
        for (TDeduction entry : monthly) {
            if (!strYear.equals(trim(entry.getAddYear())) || !strMonth.equals(trim(entry.getAddMonth()))) continue;
            DeductionMaster master = masters.get(trim(entry.getDidCode()).toUpperCase());
            String name = master == null ? null : master.getDudName();
            if (!matcher.test(entry.getDidCode(), name, master, null)) continue;
            if (seen.contains(trim(entry.getEpfNo()).toUpperCase() + "|" + trim(entry.getDidCode()).toUpperCase())) continue;
            BigDecimal amount = entry.getDidAmount() != null ? BigDecimal.valueOf(entry.getDidAmount()) : BigDecimal.ZERO;
            DeductionTransaction row = new DeductionTransaction();
            row.setEpfNo(trim(entry.getEpfNo()));
            row.setDidCode(trim(entry.getDidCode()));
            row.setDeductionName(name);
            row.setBusinessCenter(entry.getBusinessCenter());
            row.setTotalAmount(amount);
            row.setMonthlyCut(amount);
            rows.add(row);
        }
        return rows;
    }

    private boolean isYes(String value) {
        return value != null && ("Y".equalsIgnoreCase(value.trim()) || "YES".equalsIgnoreCase(value.trim()) || "1".equals(value.trim()) || "TRUE".equalsIgnoreCase(value.trim()));
    }

    private List<DeductionTransaction> deductionsForMonth(String businessCenter, int year, int month) {
        if (month < 1 || month > 12) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Month must be between 1 and 12.");
        }
        LocalDate from = LocalDate.of(year, month, 1);
        LocalDate to = from.withDayOfMonth(from.lengthOfMonth());
        try {
            Map<String, String> employeeCenters = new LinkedHashMap<>();
            employeeRepository.findAll().forEach(employee -> {
                if (employee.getEpfNo() != null) employeeCenters.put(employee.getEpfNo().trim().toUpperCase(), employee.getBusinessCenter());
            });
            return deductionRepository.findAll().stream()
                    // A transaction saved for "ALL" belongs to the employee's business center
                    .filter(row -> matches("ALL".equalsIgnoreCase(trim(row.getBusinessCenter()))
                            ? employeeCenters.get(trim(row.getEpfNo()).toUpperCase())
                            : row.getBusinessCenter(), businessCenter))
                    .filter(row -> "ACTIVE".equalsIgnoreCase(trim(row.getStatus())))
                    .filter(row -> (row.getStartDate() == null || !row.getStartDate().isAfter(to))
                            && (row.getEndDate() == null || !row.getEndDate().isBefore(from)))
                    .toList();
        } catch (Exception ex) {
            return new ArrayList<>();
        }
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
                || (value != null && codeOf(value).equalsIgnoreCase(codeOf(filter)));
    }

    // "P01 / Plant Name" to "P01"; codes may contain spaces ("BA POL")
    private String codeOf(String value) {
        return value == null ? "" : value.split("/", 2)[0].trim();
    }

    private boolean contains(String value, String term) {
        return value != null && value.toLowerCase().contains(term);
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isHalfDay(Double halfDay) {
        return halfDay != null && (halfDay == 0.5 || halfDay == 1.0);
    }

    /** Deduction codes whose master name is an advance (e.g. "Advance", "Salary Advance"). */
    private java.util.Set<String> advanceDeductionCodes() {
        java.util.Set<String> codes = new java.util.HashSet<>();
        try {
            for (DeductionMaster master : deductionMasterRepository.findAll()) {
                if (master.getDudCode() != null && (contains(master.getDudName(), "advance") || contains(master.getDudCode(), "adv"))) {
                    codes.add(master.getDudCode().trim().toUpperCase());
                }
            }
        } catch (Exception ex) {
            // fall back to code / name matching only
        }
        return codes;
    }

    private boolean isAdvance(String didCode, String deductionName, java.util.Set<String> advanceCodes) {
        return advanceCodes.contains(trim(didCode).toUpperCase()) || contains(didCode, "adv") || contains(deductionName, "advance");
    }

    private void putBankDetails(Map<String, Object> map, Employee employee) {
        map.put("bankAccountNumber", employee == null ? null : trim(employee.getBankAccountNumber()));
        map.put("bankName", employee == null ? null : trim(employee.getBankName()));
        map.put("bankBranchName", employee == null ? null : trim(employee.getBankBranchName()));
    }

    /** Plant / customer code (upper case) to full company name. */
    private Map<String, String> plantNameMap() {
        Map<String, String> names = new LinkedHashMap<>();
        try {
            for (Plant plant : plantRepository.findAll()) {
                String code = trim(plant.getCustCode()).toUpperCase();
                String name = trim(plant.getCustName());
                if (!code.isEmpty()) names.put(code, name.isEmpty() ? code : name.toUpperCase());
            }
        } catch (Exception ex) {
            // names fall back to codes
        }
        return names;
    }
}
