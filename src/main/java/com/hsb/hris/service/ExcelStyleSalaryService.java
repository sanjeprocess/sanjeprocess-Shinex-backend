package com.hsb.hris.service;

import com.hsb.hris.entity.*;
import com.hsb.hris.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExcelStyleSalaryService {

    private final AttendanceRepository attendanceRepo;
    private final EmployeeRepository employeeRepo;
    private final TDeductionRepository deductionRepo;
    private final TLoanRepository loanRepo;
    private final TAdditionRepository additionRepo;
    private final AdditionMasterRepository additionMasterRepo;
    private final DeductionMasterRepository deductionMasterRepo;
    private final SalaryCalculationRepository salaryCalcRepo;

    public ExcelStyleSalaryService(
            AttendanceRepository attendanceRepo,
            EmployeeRepository employeeRepo,
            TDeductionRepository deductionRepo,
            TLoanRepository loanRepo,
            TAdditionRepository additionRepo,
            AdditionMasterRepository additionMasterRepo,
            DeductionMasterRepository deductionMasterRepo,
            SalaryCalculationRepository salaryCalcRepo) {
        this.attendanceRepo = attendanceRepo;
        this.employeeRepo = employeeRepo;
        this.deductionRepo = deductionRepo;
        this.loanRepo = loanRepo;
        this.additionRepo = additionRepo;
        this.additionMasterRepo = additionMasterRepo;
        this.deductionMasterRepo = deductionMasterRepo;
        this.salaryCalcRepo = salaryCalcRepo;
    }

    /**
     * Exact Excel formula calculation matching Recovered_Sheet1 from the official Excel salary sheet.
     */
    public static SalaryCalculation calculateRow(
            String epfNo,
            String employeeName,
            String plant,
            String year,
            String month,
            double dailyRate,
            double shift1Days,
            double shift2Days,
            double poyaDays,
            double statutoryDays,
            double otHours,
            double performanceAllowance,
            double otherAdditions,
            double attendanceAllowance,
            double advanceDeduction,
            double telephoneDeduction,
            double mealDeduction,
            double loanDeduction,
            double otherDeductions
    ) {
        // Col D: Shift 1 Total: =B*C (Shift1_Days * Daily_Rate)
        double shift1Total = Math.round(shift1Days * dailyRate * 100.0) / 100.0;

        // Col G: Shift 2 Total: =E*F (Shift2_Days * Daily_Rate)
        double shift2Total = Math.round(shift2Days * dailyRate * 100.0) / 100.0;

        // Col J: Sunday/Poya Total: =H*I (Poya_Days * (Daily_Rate * 1.5))
        double poyaRate = Math.round(dailyRate * 1.5 * 100.0) / 100.0;
        double poyaTotal = Math.round(poyaDays * poyaRate * 100.0) / 100.0;

        // Col M: Statutory Holiday Total: =K*L (Days * Daily_Rate)
        double statutoryTotal = Math.round(statutoryDays * dailyRate * 100.0) / 100.0;

        // Col P: Over Time Total: =N*O (OT_Hours * ((Daily_Rate / 8) * 1.5))
        double hourlyRate = dailyRate > 0 ? (dailyRate / 8.0) : 0;
        double otRate = Math.round(hourlyRate * 1.5 * 100.0) / 100.0;
        double otTotal = Math.round(otHours * otRate * 100.0) / 100.0;

        // Col T: Gross Salary = S+R+Q+P+M+J+G+D
        double grossSalary = Math.round((shift1Total + shift2Total + poyaTotal + statutoryTotal + otTotal +
                performanceAllowance + otherAdditions + attendanceAllowance) * 100.0) / 100.0;

        // Col U: EPF 8% = (D+G+J+M)/100*8
        double epfBase = shift1Total + shift2Total + poyaTotal + statutoryTotal;
        double epf8 = Math.round(epfBase * 0.08 * 100.0) / 100.0;
        double epf12 = Math.round(epfBase * 0.12 * 100.0) / 100.0;
        double etf3 = Math.round(epfBase * 0.03 * 100.0) / 100.0;

        // Col AB: Total Deductions = Loan + Meal_Value + Telephone + Advance + Other_Deduct + EPF_8%
        double totalDeductions = Math.round((epf8 + advanceDeduction + telephoneDeduction + mealDeduction +
                loanDeduction + otherDeductions) * 100.0) / 100.0;

        // Col AC: Net Salary = Gross_Salary - Total_Deductions
        double netSalary = Math.round((grossSalary - totalDeductions) * 100.0) / 100.0;

        SalaryCalculation calc = new SalaryCalculation();
        calc.setEpfNo(epfNo);
        calc.setEmployeeName(employeeName);
        calc.setPlant(plant);
        calc.setYear(year);
        calc.setMonth(month);
        calc.setDaysWorked(shift1Days + shift2Days + poyaDays + statutoryDays);
        calc.setOtHours(otHours);
        calc.setBasicSalary(dailyRate);
        calc.setGrossSalary(grossSalary);
        calc.setEpf8(epf8);
        calc.setEpf12(epf12);
        calc.setEtf3(etf3);
        calc.setAdvanceDeduction(advanceDeduction);
        calc.setTelephoneDeduction(telephoneDeduction);
        calc.setMealDeduction(mealDeduction);
        calc.setLoanDeduction(loanDeduction);
        calc.setOtherDeductions(otherDeductions);
        calc.setTotalDeductions(totalDeductions);
        calc.setNetSalary(netSalary);
        calc.setCalculatedAt(LocalDateTime.now());
        return calc;
    }

    @Transactional
    public List<SalaryCalculation> calculateSalary(String year, String month, String plantFilter) {
        String yr = year.trim();
        String mo = String.format("%02d", Integer.parseInt(month.trim()));

        // Query attendance records for the given year and month
        List<Attendance> attendanceRecords = attendanceRepo.findAll().stream()
                .filter(a -> yr.equals(a.getAttYear()) && mo.equals(a.getAttMonth()))
                .filter(a -> plantFilter == null || plantFilter.isBlank() || "ALL".equalsIgnoreCase(plantFilter.trim()) ||
                        (a.getPlantCode() != null && a.getPlantCode().trim().equalsIgnoreCase(plantFilter.trim())))
                .toList();

        // Load employees
        Map<String, Employee> employeeMap = employeeRepo.findAll().stream()
                .filter(e -> e.getEpfNo() != null)
                .collect(Collectors.toMap(e -> e.getEpfNo().trim().toUpperCase(), e -> e, (a, b) -> a));

        // Group attendance by (EPF_No, Plant)
        Map<String, List<Attendance>> empPlantGroup = new LinkedHashMap<>();
        for (Attendance a : attendanceRecords) {
            if (a.getEpfNo() == null) continue;
            String epf = a.getEpfNo().trim().toUpperCase();
            String plant = (a.getPlantCode() != null && !a.getPlantCode().isBlank()) ? a.getPlantCode().trim() : "DEFAULT";
            String key = epf + "|" + plant;
            empPlantGroup.computeIfAbsent(key, k -> new ArrayList<>()).add(a);
        }

        // Deductions & Loans
        List<TDeduction> allDeductions = deductionRepo.findAll();
        List<TLoan> allLoans = loanRepo.findAll();
        List<TAddition> allAdditions = additionRepo.findAll();

        List<SalaryCalculation> results = new ArrayList<>();

        for (Map.Entry<String, List<Attendance>> entry : empPlantGroup.entrySet()) {
            String[] parts = entry.getKey().split("\\|");
            String epf = parts[0];
            String plant = parts[1];
            List<Attendance> attList = entry.getValue();

            Employee emp = employeeMap.get(epf);
            String empName = emp != null ?
                    ((emp.getFirstName() == null ? "" : emp.getFirstName()) + " " + (emp.getLastName() == null ? "" : emp.getLastName())).trim() :
                    (attList.get(0).getEmployeeName() != null ? attList.get(0).getEmployeeName() : epf);

            double dailyRate = (emp != null && emp.getBasicSalary() != null && emp.getBasicSalary() > 0) ?
                    emp.getBasicSalary() : (attList.get(0).getBasicSalary() != null ? attList.get(0).getBasicSalary() : 840.0);

            double shift1Days = 0;
            double shift2Days = 0;
            double poyaDays = 0;
            double statutoryDays = 0;
            double otHours = 0;
            double mealValue = 0;

            for (Attendance a : attList) {
                double weight = (a.getHalfDay() != null && (a.getHalfDay() == 1.0 || a.getHalfDay() == 0.5)) ? 0.5 : 1.0;
                if ("Y".equalsIgnoreCase(a.getSaturdayPoya())) {
                    poyaDays += weight;
                } else if ("Y".equalsIgnoreCase(a.getSecondShift())) {
                    shift2Days += weight;
                } else {
                    shift1Days += weight;
                }
                if (a.getStatutoryHolidays() != null && a.getStatutoryHolidays() > 0) {
                    statutoryDays += a.getStatutoryHolidays();
                }
                if (a.getTotalOt() != null && a.getTotalOt() > 0) {
                    otHours += a.getTotalOt();
                }
                if (a.getTotalMealValue() != null && a.getTotalMealValue() > 0) {
                    mealValue += a.getTotalMealValue();
                }
            }

            // Employee additions for this month/year
            double performanceAllowance = 0;
            double otherAdditions = 0;
            for (TAddition add : allAdditions) {
                if (add.getEpfNo() != null && epf.equalsIgnoreCase(add.getEpfNo().trim())
                        && yr.equals(add.getAddYear()) && mo.equals(String.format("%02d", add.getAddMonth().intValue()))) {
                    double amt = add.getAddAmount() != null ? add.getAddAmount() : 0;
                    otherAdditions += amt;
                }
            }

            // Deductions for this employee
            double advance = 0;
            double telephone = 0;
            double otherDeduct = 0;
            for (TDeduction d : allDeductions) {
                if (d.getEpfNo() != null && epf.equalsIgnoreCase(d.getEpfNo().trim())
                        && yr.equals(d.getAddYear()) && mo.equals(d.getAddMonth())) {
                    double amt = d.getDidAmount() != null ? d.getDidAmount() : 0;
                    String code = (d.getDidCode() != null) ? d.getDidCode().toLowerCase() : "";
                    if (code.contains("adv")) advance += amt;
                    else if (code.contains("tel") || code.contains("phone")) telephone += amt;
                    else otherDeduct += amt;
                }
            }

            // Loan deduction
            double loanCut = 0;
            for (TLoan l : allLoans) {
                if (l.getEpfNo() != null && epf.equalsIgnoreCase(l.getEpfNo().trim())) {
                    if (l.getLoanAmount() != null && l.getLoanAmount() > 0) {
                        loanCut += (l.getLoanAmount() / 12.0);
                    }
                }
            }
            loanCut = Math.round(loanCut * 100.0) / 100.0;

            SalaryCalculation row = calculateRow(
                    epf, empName, plant, yr, mo,
                    dailyRate, shift1Days, shift2Days, poyaDays, statutoryDays, otHours,
                    performanceAllowance, otherAdditions, 0.0,
                    advance, telephone, mealValue, loanCut, otherDeduct
            );

            // Upsert into TBL_Salary_Calc
            Optional<SalaryCalculation> existing = salaryCalcRepo.findByEpfNoAndPlantAndYearAndMonth(epf, plant, yr, mo);
            if (existing.isPresent()) {
                SalaryCalculation old = existing.get();
                old.setEmployeeName(row.getEmployeeName());
                old.setDaysWorked(row.getDaysWorked());
                old.setOtHours(row.getOtHours());
                old.setBasicSalary(row.getBasicSalary());
                old.setGrossSalary(row.getGrossSalary());
                old.setEpf8(row.getEpf8());
                old.setEpf12(row.getEpf12());
                old.setEtf3(row.getEtf3());
                old.setAdvanceDeduction(row.getAdvanceDeduction());
                old.setTelephoneDeduction(row.getTelephoneDeduction());
                old.setMealDeduction(row.getMealDeduction());
                old.setLoanDeduction(row.getLoanDeduction());
                old.setOtherDeductions(row.getOtherDeductions());
                old.setTotalDeductions(row.getTotalDeductions());
                old.setNetSalary(row.getNetSalary());
                old.setCalculatedAt(LocalDateTime.now());
                results.add(salaryCalcRepo.save(old));
            } else {
                results.add(salaryCalcRepo.save(row));
            }
        }

        return results;
    }

    public List<SalaryCalculation> getCalculatedSalary(String year, String month, String plant) {
        if (year != null && !year.isBlank() && month != null && !month.isBlank()) {
            String mo = String.format("%02d", Integer.parseInt(month.trim()));
            if (plant != null && !plant.isBlank() && !"ALL".equalsIgnoreCase(plant.trim())) {
                return salaryCalcRepo.findByYearAndMonthAndPlant(year.trim(), mo, plant.trim());
            }
            return salaryCalcRepo.findByYearAndMonth(year.trim(), mo);
        } else if (plant != null && !plant.isBlank() && !"ALL".equalsIgnoreCase(plant.trim())) {
            return salaryCalcRepo.findByPlant(plant.trim());
        }
        return salaryCalcRepo.findAll();
    }
}
