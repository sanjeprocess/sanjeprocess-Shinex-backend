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
    private final AttendanceRepository attendanceRepo;
    private final PlantRepository plantRepo;

    public MonthlySummaryController(EmployeeRepository employees, TAdditionRepository additions,
                                    TDeductionRepository deductions, TLoanRepository loans,
                                    AttendanceSummaryRepository summaries, AdditionMasterRepository additionMasters,
                                    DeductionMasterRepository deductionMasters, AttendanceRepository attendanceRepo,
                                    PlantRepository plantRepo) {
        this.employees = employees; this.additions = additions; this.deductions = deductions;
        this.loans = loans; this.summaries = summaries; this.additionMasters = additionMasters;
        this.deductionMasters = deductionMasters; this.attendanceRepo = attendanceRepo;
        this.plantRepo = plantRepo;
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
        result.put("businessCenter", employee.getBusinessCenter() == null ? "" : employee.getBusinessCenter().trim());
        result.put("plantCode", employee.getPlantCode() == null ? "" : employee.getPlantCode().trim());
        result.put("sectionCode", employee.getSectionCode() == null ? "" : employee.getSectionCode().trim());

        double basicSalary = value(employee.getBasicSalary());
        result.put("basicSalary", basicSalary);

        // Allowances & Additions
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
                    row.put("code", a.getAddCode().trim());
                    row.put("name", master == null ? a.getAddCode().trim() : master.getAdditionName());
                    row.put("amount", value(a.getAddAmount()));
                    row.put("addEpf", master != null && "Y".equalsIgnoreCase(master.getAddToEpf()));
                    row.put("addToBasic", master != null && "Y".equalsIgnoreCase(master.getAddToBasic()));
                    return row;
                }).toList();
        result.put("additions", addRows);

        double totalAdditions = addRows.stream().mapToDouble(r -> (Double) r.get("amount")).sum();
        double epfQualifyingAdditions = addRows.stream()
                .filter(r -> Boolean.TRUE.equals(r.get("addEpf")))
                .mapToDouble(r -> (Double) r.get("amount")).sum();

        // Deductions
        Map<String, DeductionMaster> dedMasters = deductionMasters.findAll().stream()
                .filter(d -> d.getDudCode() != null)
                .collect(Collectors.toMap(d -> d.getDudCode().trim(), d -> d, (a, b) -> a));
        List<Map<String, Object>> dedRows = deductions.findAll().stream()
                .filter(d -> d.getEpfNo() != null && d.getAddYear() != null && d.getAddMonth() != null
                        && epf.equals(d.getEpfNo().trim()) && yr.equals(d.getAddYear().trim()) && mo.equals(d.getAddMonth().trim()))
                .map(d -> {
                    DeductionMaster master = dedMasters.get(d.getDidCode().trim());
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("code", d.getDidCode().trim());
                    row.put("name", master == null ? d.getDidCode().trim() : master.getDudName());
                    row.put("amount", value(d.getDidAmount()));
                    return row;
                }).toList();
        result.put("deductions", dedRows);

        // Loans / Advances
        List<Map<String, Object>> advances = loans.findAll().stream()
                .filter(l -> l.getEpfNo() != null && epf.equals(l.getEpfNo().trim()))
                .map(l -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("loanId", l.getLoanId().trim());
                    row.put("amount", value(l.getLoanAmount()));
                    return row;
                }).toList();
        result.put("advances", advances);
        double totalLoanAdvances = advances.stream().mapToDouble(l -> (Double) l.get("amount")).sum();

        // Attendance logs calculation
        List<Attendance> monthlyAttList = attendanceRepo.findByEpfNoAndAttYearAndAttMonth(epf, yr, mo);
        double attDaysCome = 0;
        double attNormalDays = 0;
        double attPoyaDays = 0;
        double attSpecialDays = 0;
        double attStatutoryDays = 0;
        double attNightDays = 0;
        double attOtHours = 0;
        double attDayAllowance = 0;
        double attNightAllowance = 0;
        double attPoyaExtra = 0;
        double attMealValue = 0;

        for (Attendance a : monthlyAttList) {
            double dayWeight = (a.getHalfDay() != null && (a.getHalfDay() == 1.0 || a.getHalfDay() == 0.5)) ? 0.5 : 1.0;
            attDaysCome += dayWeight;
            if ("Y".equalsIgnoreCase(a.getSaturdayPoya())) {
                attPoyaDays += dayWeight;
            } else if ("Y".equalsIgnoreCase(a.getSpecialDay())) {
                attSpecialDays += dayWeight;
            } else {
                attNormalDays += dayWeight;
            }

            if (a.getStatutoryHolidays() != null && a.getStatutoryHolidays() > 0) {
                attStatutoryDays += a.getStatutoryHolidays();
            }
            if ("Y".equalsIgnoreCase(a.getNightShift()) || "Y".equalsIgnoreCase(a.getFullNight())) {
                attNightDays += dayWeight;
            }

            if (a.getTotalOt() != null && a.getTotalOt() > 0) attOtHours += a.getTotalOt();
            if (a.getDayAllowance() != null && a.getDayAllowance() > 0) attDayAllowance += a.getDayAllowance();
            if (a.getNightAllowance() != null && a.getNightAllowance() > 0) attNightAllowance += a.getNightAllowance();
            if (a.getSundayPoyaExtra() != null && a.getSundayPoyaExtra() > 0) attPoyaExtra += a.getSundayPoyaExtra();
            if (a.getTotalMealValue() != null && a.getTotalMealValue() > 0) attMealValue += a.getTotalMealValue();
        }

        // Attendance summary
        AttendanceSummary attendance = summaries.findById(new AttSummaryId(yr, mo, epf)).orElse(null);
        Map<String, Object> attendanceData = new LinkedHashMap<>();
        double workingDays = 0, otHours = 0, dayAllowance = value(employee.getDayAllowance()),
                nightAllowance = value(employee.getNightAllowance()), poyaDayExtra = value(employee.getSundayPoyaExtra()),
                mealValue = 0, otAmount = 0;

        if (attendance != null) {
            workingDays = value(attendance.getNormalShift()) > 0 ? value(attendance.getNormalShift()) : (attDaysCome > 0 ? attDaysCome : 26);
            otHours = (value(attendance.getOt1Hours()) + value(attendance.getOt2Hours())) > 0 ?
                    (value(attendance.getOt1Hours()) + value(attendance.getOt2Hours())) : attOtHours;
            if (attendance.getDayRate() != null && attendance.getDayRate() > 0) dayAllowance = attendance.getDayRate();
            else if (attDayAllowance > 0) dayAllowance = attDayAllowance;
            if (attendance.getNightRate() != null && attendance.getNightRate() > 0) nightAllowance = attendance.getNightRate();
            else if (attNightAllowance > 0) nightAllowance = attNightAllowance;
            if (attendance.getSundayPoyaExtra() != null && attendance.getSundayPoyaExtra() > 0) poyaDayExtra = attendance.getSundayPoyaExtra();
            else if (attPoyaExtra > 0) poyaDayExtra = attPoyaExtra;
            mealValue = value(attendance.getTotalMealValue()) > 0 ? value(attendance.getTotalMealValue()) : attMealValue;
            otAmount = value(attendance.getOt1Total()) + value(attendance.getOt2Total());
        } else {
            workingDays = attDaysCome > 0 ? attDaysCome : 26;
            otHours = attOtHours;
            if (attDayAllowance > 0) dayAllowance = attDayAllowance;
            if (attNightAllowance > 0) nightAllowance = attNightAllowance;
            if (attPoyaExtra > 0) poyaDayExtra = attPoyaExtra;
            if (attMealValue > 0) mealValue = attMealValue;
        }

        // Day Type Rates & Amounts (Daily rate: basicSalary is the per-day rate)
        double normalDays = attNormalDays > 0 ? attNormalDays : workingDays;
        double normalRate = basicSalary > 0 ? basicSalary : 0;
        double normalAmount = Math.round(normalDays * normalRate * 100.0) / 100.0;

        double poyaDays = attPoyaDays;
        double poyaRate = Math.round(normalRate * 1.5 * 100.0) / 100.0;
        double poyaAmount = poyaDayExtra > 0 ? poyaDayExtra : Math.round(poyaDays * poyaRate * 100.0) / 100.0;

        double nightDays = attNightDays;
        double nightRate = nightAllowance > 0 ? nightAllowance : 540.0;
        double nightAmount = (attNightAllowance > 0 || nightAllowance > 0) ? (nightAllowance > 0 ? nightAllowance : attNightAllowance) : (nightDays * nightRate);

        double hourlyRate = normalRate > 0 ? (normalRate / 8.0) : 0;
        double otRate1_5 = Math.round(hourlyRate * 1.5 * 100.0) / 100.0;
        if (otAmount <= 0 && otHours > 0 && normalRate > 0) {
            otAmount = Math.round(otHours * otRate1_5 * 100.0) / 100.0;
        }

        double statutoryHolidayCount = attStatutoryDays;
        double statutoryHolidayAmount = Math.round(statutoryHolidayCount * normalRate * 100.0) / 100.0;

        attendanceData.put("workingDays", workingDays);
        attendanceData.put("normalDays", normalDays);
        attendanceData.put("normalRate", normalRate);
        attendanceData.put("normalAmount", normalAmount);
        attendanceData.put("poyaDays", poyaDays);
        attendanceData.put("poyaRate", poyaRate);
        attendanceData.put("poyaAmount", poyaAmount);
        attendanceData.put("nightDays", nightDays);
        attendanceData.put("nightRate", nightRate);
        attendanceData.put("nightAmount", nightAmount);
        attendanceData.put("otHours", otHours);
        attendanceData.put("otRate", otRate1_5);
        attendanceData.put("otAmount", otAmount);
        attendanceData.put("dayAllowance", dayAllowance);
        attendanceData.put("nightAllowance", nightAllowance);
        attendanceData.put("poyaDayExtra", poyaAmount);
        attendanceData.put("mealValue", mealValue);
        attendanceData.put("statutoryHolidayCount", statutoryHolidayCount);
        attendanceData.put("statutoryHolidayAmount", statutoryHolidayAmount);
        result.put("attendance", attendanceData);

        // Pay Advice components (Generated_Pay_Advice_v2 layout)
        double grossSalary = normalAmount + nightAmount + poyaAmount + otAmount + dayAllowance + totalAdditions + statutoryHolidayAmount;
        if (attendance != null && attendance.getGrossSalary() != null && attendance.getGrossSalary() > 0) {
            grossSalary = attendance.getGrossSalary();
        }

        // EPF 8%
        double epfBase = normalAmount + epfQualifyingAdditions;
        double epf8 = Math.round(epfBase * 0.08 * 100.0) / 100.0;
        double epf12 = Math.round(epfBase * 0.12 * 100.0) / 100.0;
        double etf3 = Math.round(epfBase * 0.03 * 100.0) / 100.0;

        // Specific deductions
        double advanceDeduction = 0;
        double telephoneDeduction = 0;
        double mealDeduction = mealValue;
        double absentDeduction = 0;
        double loanDeduction = totalLoanAdvances;
        double deathDonation = (employee.getDethDenotion() != null && employee.getDethDenotion()) ? 100.0 : 0.0;
        double otherDeductions = 0;

        for (Map<String, Object> d : dedRows) {
            String name = String.valueOf(d.get("name")).toLowerCase();
            double amt = (Double) d.get("amount");
            if (name.contains("advance")) advanceDeduction += amt;
            else if (name.contains("telephone") || name.contains("phone")) telephoneDeduction += amt;
            else if (name.contains("meal") || name.contains("food")) mealDeduction += amt;
            else if (name.contains("absent")) absentDeduction += amt;
            else if (name.contains("loan")) loanDeduction += amt;
            else if (name.contains("death") || name.contains("donation")) deathDonation += amt;
            else otherDeductions += amt;
        }

        double totalDeductions = Math.round((epf8 + advanceDeduction + telephoneDeduction + mealDeduction +
                absentDeduction + loanDeduction + deathDonation + otherDeductions) * 100.0) / 100.0;

        if (attendance != null && attendance.getTotalDeduction() != null && attendance.getTotalDeduction() > 0) {
            totalDeductions = attendance.getTotalDeduction();
        }

        double netSalary = Math.round((grossSalary - totalDeductions) * 100.0) / 100.0;
        if (attendance != null && attendance.getNetSalary() != null && attendance.getNetSalary() > 0) {
            netSalary = attendance.getNetSalary();
        }

        // Structure payslip totals
        result.put("normalDays", normalDays);
        result.put("normalRate", normalRate);
        result.put("normalAmount", normalAmount);
        result.put("poyaDays", poyaDays);
        result.put("poyaRate", poyaRate);
        result.put("poyaAmount", poyaAmount);
        result.put("nightDays", nightDays);
        result.put("nightRate", nightRate);
        result.put("nightAmount", nightAmount);
        result.put("nightAllowance", nightAmount);
        result.put("poyaDayExtra", poyaAmount);
        result.put("overtimeHours", otHours);
        result.put("overtimeRate", otRate1_5);
        result.put("overtimeAmount", otAmount);
        result.put("dayAllowance", dayAllowance);
        result.put("statutoryHolidayAmount", statutoryHolidayAmount);
        result.put("totalAdditions", totalAdditions);
        result.put("grossSalary", grossSalary);

        result.put("epf8", epf8);
        result.put("advanceDeduction", advanceDeduction);
        result.put("telephoneDeduction", telephoneDeduction);
        result.put("mealDeduction", mealDeduction);
        result.put("absentDeduction", absentDeduction);
        result.put("loanDeduction", loanDeduction);
        result.put("deathDonation", deathDonation);
        result.put("totalDeductions", totalDeductions);
        result.put("netSalary", netSalary);

        result.put("epf12", epf12);
        result.put("etf3", etf3);

        // Feature 7: Multi-plant split: query plants worked during the reporting month
        Map<String, Long> plantDaysMap = new LinkedHashMap<>();
        for (Attendance a : monthlyAttList) {
            String pCode = a.getPlantCode() == null ? "" : a.getPlantCode().trim();
            if (!pCode.isEmpty()) {
                plantDaysMap.put(pCode, plantDaysMap.getOrDefault(pCode, 0L) + 1);
            }
        }

        if (plantDaysMap.isEmpty()) {
            String defaultPlant = employee.getPlantCode() == null || employee.getPlantCode().isBlank() ? "PL001" : employee.getPlantCode().trim();
            plantDaysMap.put(defaultPlant, Math.round(workingDays));
        }

        Map<String, Plant> plantMasterMap = plantRepo.findAll().stream()
                .filter(p -> p.getCustCode() != null)
                .collect(Collectors.toMap(p -> p.getCustCode().trim(), p -> p, (a, b) -> a));

        List<Map<String, Object>> plantsWorked = new ArrayList<>();
        for (Map.Entry<String, Long> entry : plantDaysMap.entrySet()) {
            Map<String, Object> p = new LinkedHashMap<>();
            String pCode = entry.getKey();
            Plant plantObj = plantMasterMap.get(pCode);
            p.put("plantCode", pCode);
            p.put("plantName", plantObj != null && plantObj.getCustName() != null ? plantObj.getCustName() : pCode);
            p.put("daysWorked", entry.getValue());
            plantsWorked.add(p);
        }
        result.put("plantsWorked", plantsWorked);

        return result;
    }

    private double value(Double number) { return number == null ? 0d : number; }
}
