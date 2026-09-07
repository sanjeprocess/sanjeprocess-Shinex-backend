package com.hsb.hris.controller.process;

import com.hsb.hris.entity.*;
import com.hsb.hris.entity.id.AttSummaryId;
import com.hsb.hris.repository.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/process/monthly-summary")
public class MonthlySummaryController {
    private final EmployeeRepository employees;
    private final TAdditionRepository additions;
    private final TDeductionRepository deductions;
    private final TLoanRepository loans;
    private final AttendanceSummaryRepository summaries;
    private final AdditionMasterRepository additionMasters;
    private final DeductionMasterRepository deductionMasters;

    public MonthlySummaryController(EmployeeRepository employees, TAdditionRepository additions,
                                    TDeductionRepository deductions, TLoanRepository loans,
                                    AttendanceSummaryRepository summaries, AdditionMasterRepository additionMasters,
                                    DeductionMasterRepository deductionMasters) {
        this.employees = employees; this.additions = additions; this.deductions = deductions;
        this.loans = loans; this.summaries = summaries; this.additionMasters = additionMasters;
        this.deductionMasters = deductionMasters;
    }

    @GetMapping
    public Map<String, Object> summary(@RequestParam String epfNo, @RequestParam String year,
                                       @RequestParam String month) {
        String epf = epfNo.trim(), yr = year.trim(), mo = String.format("%02d", Integer.parseInt(month.trim()));
        Employee employee = employees.findByTrimmedEpfNo(epf).orElseThrow();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("epfNo", epf);
        result.put("empName", ((employee.getFirstName() == null ? "" : employee.getFirstName()) + " " +
                (employee.getLastName() == null ? "" : employee.getLastName())).trim());
        result.put("basicSalary", value(employee.getBasicSalary()));

        Map<String, AdditionMaster> addMasters = additionMasters.findAll().stream()
                .filter(a -> a.getAdditionCode() != null)
                .collect(Collectors.toMap(a -> a.getAdditionCode().trim(), a -> a, (a, b) -> a));
        List<Map<String, Object>> addRows = additions.findAll().stream()
                .filter(a -> a.getEpfNo() != null && a.getAddYear() != null
                        && epf.equals(a.getEpfNo().trim()) && yr.equals(a.getAddYear().trim())
                        && a.getAddMonth() != null && mo.equals(String.format("%02d", a.getAddMonth().intValue())))
                .map(a -> {
                    AdditionMaster master = addMasters.get(a.getAddCode().trim());
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("code", a.getAddCode().trim()); row.put("name", master == null ? a.getAddCode().trim() : master.getAdditionName());
                    row.put("amount", value(a.getAddAmount())); row.put("addEpf", master != null && "Y".equalsIgnoreCase(master.getAddToEpf()));
                    row.put("addToBasic", master != null && "Y".equalsIgnoreCase(master.getAddToBasic()));
                    return row;
                }).toList();
        result.put("additions", addRows);

        Map<String, DeductionMaster> dedMasters = deductionMasters.findAll().stream()
                .filter(d -> d.getDudCode() != null)
                .collect(Collectors.toMap(d -> d.getDudCode().trim(), d -> d, (a, b) -> a));
        List<Map<String, Object>> dedRows = deductions.findAll().stream()
                .filter(d -> d.getEpfNo() != null && d.getAddYear() != null && d.getAddMonth() != null
                        && epf.equals(d.getEpfNo().trim()) && yr.equals(d.getAddYear().trim()) && mo.equals(d.getAddMonth().trim()))
                .map(d -> {
                    DeductionMaster master = dedMasters.get(d.getDidCode().trim());
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("code", d.getDidCode().trim()); row.put("name", master == null ? d.getDidCode().trim() : master.getDudName());
                    row.put("amount", value(d.getDidAmount())); return row;
                }).toList();
        result.put("deductions", dedRows);

        result.put("advances", loans.findAll().stream().filter(l -> l.getEpfNo() != null && epf.equals(l.getEpfNo().trim())).map(l -> {
            Map<String, Object> row = new LinkedHashMap<>(); row.put("loanId", l.getLoanId().trim()); row.put("amount", value(l.getLoanAmount())); return row;
        }).toList());

        AttendanceSummary attendance = summaries.findById(new AttSummaryId(yr, mo, epf)).orElse(null);
        Map<String, Object> attendanceData = new LinkedHashMap<>();
        if (attendance != null) {
            attendanceData.put("workingDays", value(attendance.getNormalShift()));
            attendanceData.put("otHours", value(attendance.getOt1Hours()) + value(attendance.getOt2Hours()));
            attendanceData.put("dayAllowance", value(attendance.getDayRate()));
            attendanceData.put("nightAllowance", value(attendance.getNightRate()));
            attendanceData.put("mealValue", value(attendance.getTotalMealValue()));
            result.put("netSalary", value(attendance.getNetSalary()));
            result.put("totalAdditions", value(attendance.getTotalAddition()));
            result.put("totalDeductions", value(attendance.getTotalDeduction()));
        } else {
            result.put("netSalary", value(employee.getBasicSalary()) + addRows.stream().mapToDouble(r -> (Double) r.get("amount")).sum()
                    - dedRows.stream().mapToDouble(r -> (Double) r.get("amount")).sum());
            result.put("totalAdditions", addRows.stream().mapToDouble(r -> (Double) r.get("amount")).sum());
            result.put("totalDeductions", dedRows.stream().mapToDouble(r -> (Double) r.get("amount")).sum());
        }
        result.put("attendance", attendanceData);
        return result;
    }

    private double value(Double number) { return number == null ? 0d : number; }
}
