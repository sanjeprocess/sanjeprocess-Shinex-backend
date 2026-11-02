package com.hsb.hris.service;

import com.hsb.hris.entity.*;
import com.hsb.hris.repository.*;
import com.hsb.hris.service.transaction.StatutoryHolidayService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private final DeductionTransactionRepository deductionTransactionRepo;
    private final StatutoryHolidayService statutoryHolidays;

    public ExcelStyleSalaryService(
            AttendanceRepository attendanceRepo,
            EmployeeRepository employeeRepo,
            TDeductionRepository deductionRepo,
            TLoanRepository loanRepo,
            TAdditionRepository additionRepo,
            AdditionMasterRepository additionMasterRepo,
            DeductionMasterRepository deductionMasterRepo,
            SalaryCalculationRepository salaryCalcRepo,
            DeductionTransactionRepository deductionTransactionRepo,
            StatutoryHolidayService statutoryHolidays) {
        this.attendanceRepo = attendanceRepo;
        this.employeeRepo = employeeRepo;
        this.deductionRepo = deductionRepo;
        this.loanRepo = loanRepo;
        this.additionRepo = additionRepo;
        this.additionMasterRepo = additionMasterRepo;
        this.deductionMasterRepo = deductionMasterRepo;
        this.salaryCalcRepo = salaryCalcRepo;
        this.deductionTransactionRepo = deductionTransactionRepo;
        this.statutoryHolidays = statutoryHolidays;
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
        return calculateRow(epfNo, employeeName, plant, year, month, dailyRate, shift1Days, shift2Days, poyaDays,
                statutoryDays, otHours, performanceAllowance, otherAdditions, attendanceAllowance, advanceDeduction,
                telephoneDeduction, mealDeduction, loanDeduction, otherDeductions,
                Math.round(poyaDays * Math.round(dailyRate * 1.5 * 100.0) / 100.0 * 100.0) / 100.0);
    }

    /** Same formula with the Sunday/Poya total already worked out from each day's extra payment. */
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
            double otherDeductions,
            double poyaTotal
    ) {
        // Col D: Shift 1 Total: =B*C (Shift1_Days * Daily_Rate)
        double shift1Total = Math.round(shift1Days * dailyRate * 100.0) / 100.0;

        // Col G: Shift 2 Total: =E*F (Shift2_Days * Daily_Rate)
        double shift2Total = Math.round(shift2Days * dailyRate * 100.0) / 100.0;

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
    public List<SalaryCalculation> calculateSalary(String year, String month, String plantFilter, String businessCenterFilter) {
        List<SalaryCalculation> results = new ArrayList<>();
        for (SalarySheetRow sheetRow : buildSalarySheet(year, month, plantFilter, businessCenterFilter)) {
            SalaryCalculation row = sheetRow.calculation;
            row.setBusinessCenter(sheetRow.businessCenter);

            // Upsert into TBL_Salary_Calc
            Optional<SalaryCalculation> existing = salaryCalcRepo.findByEpfNoAndPlantAndYearAndMonthAndBusinessCenter(
                    row.getEpfNo(), row.getPlant(), row.getYear(), row.getMonth(), sheetRow.businessCenter);
            if (existing.isPresent()) {
                SalaryCalculation old = existing.get();
                old.setEmployeeName(row.getEmployeeName());
                old.setBusinessCenter(row.getBusinessCenter());
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

    /**
     * Salary sheet rows for the month, one per employee and plant. An employee who worked at several
     * plants gets additions, deductions and statutory holiday pay once, on the home plant row
     * (or the first plant worked when the home plant has no attendance).
     */
    public List<SalarySheetRow> buildSalarySheet(String year, String month, String plantFilter, String businessCenterFilter) {
        String yr = year.trim();
        String mo = String.format("%02d", Integer.parseInt(month.trim()));
        boolean allCenters = businessCenterFilter == null || businessCenterFilter.isBlank() || "ALL".equalsIgnoreCase(businessCenterFilter.trim());

        List<Attendance> attendanceRecords = attendanceRepo.findByAttYearAndAttMonth(yr, mo).stream()
                .filter(a -> allCenters || (a.getBusinessCenter() != null && a.getBusinessCenter().trim().equalsIgnoreCase(businessCenterFilter.trim())))
                .toList();

        // Load ALL employees for the selected business center (fix: include employees with 0 attendance)
        List<Employee> filteredEmployees = allCenters ? employeeRepo.findAll() : employeeRepo.findByBusinessCenterSmart(businessCenterFilter.trim());
        Map<String, Employee> employeeMap = filteredEmployees.stream()
                .filter(e -> e.getEpfNo() != null)
                .collect(Collectors.toMap(e -> e.getEpfNo().trim().toUpperCase(), e -> e, (a, b) -> a));

        // Group attendance by (EPF_No, Plant, BusinessCenter)
        Map<String, List<Attendance>> empPlantGroup = new LinkedHashMap<>();
        Map<String, List<Attendance>> attendanceByEpf = new HashMap<>();
        for (Attendance a : attendanceRecords) {
            if (a.getEpfNo() == null) continue;
            String epf = a.getEpfNo().trim().toUpperCase();
            String plant = (a.getPlantCode() != null && !a.getPlantCode().isBlank()) ? a.getPlantCode().trim() : "DEFAULT";
            String businessCenter = a.getBusinessCenter() == null ? "" : a.getBusinessCenter().trim();
            empPlantGroup.computeIfAbsent(epf + "|" + plant + "|" + businessCenter, k -> new ArrayList<>()).add(a);
            attendanceByEpf.computeIfAbsent(epf, k -> new ArrayList<>()).add(a);
        }

        // Employees of the business center with no attendance this month still get a row
        for (Employee emp : filteredEmployees) {
            if (emp.getEpfNo() == null) continue;
            String epf = emp.getEpfNo().trim().toUpperCase();
            if (attendanceByEpf.containsKey(epf)) continue;
            String plant = (emp.getPlantCode() != null && !emp.getPlantCode().isBlank()) ? emp.getPlantCode().trim() : "DEFAULT";
            String bc = (emp.getBusinessCenter() != null) ? emp.getBusinessCenter().trim() : "";
            empPlantGroup.computeIfAbsent(epf + "|" + plant + "|" + bc, k -> new ArrayList<>());
        }

        // The row that carries each employee's additions and deductions
        Map<String, String> primaryKeyByEpf = new HashMap<>();
        for (String key : empPlantGroup.keySet()) {
            String[] parts = key.split("\\|");
            Employee emp = employeeMap.get(parts[0]);
            String home = emp == null || emp.getPlantCode() == null ? "" : emp.getPlantCode().trim();
            primaryKeyByEpf.putIfAbsent(parts[0], key);
            if (parts[1].equalsIgnoreCase(home)) primaryKeyByEpf.put(parts[0], key);
        }

        Map<String, AdditionMaster> additionMasters = additionMasterRepo.findAll().stream()
                .filter(m -> m.getAdditionCode() != null)
                .collect(Collectors.toMap(m -> m.getAdditionCode().trim().toUpperCase(), m -> m, (a, b) -> a));
        Map<String, DeductionMaster> deductionMasters = deductionMasterRepo.findAll().stream()
                .filter(m -> m.getDudCode() != null)
                .collect(Collectors.toMap(m -> m.getDudCode().trim().toUpperCase(), m -> m, (a, b) -> a));

        // Deduction transactions active in this month, per employee
        java.time.LocalDate monthStart = java.time.LocalDate.of(Integer.parseInt(yr), Integer.parseInt(mo), 1);
        java.time.LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());
        Map<String, double[]> transactionCuts = new HashMap<>();
        for (DeductionTransaction dt : deductionTransactionRepo.findAll()) {
            if (dt.getEpfNo() == null || dt.getMonthlyCut() == null || !"ACTIVE".equalsIgnoreCase(trim(dt.getStatus()))) continue;
            if ((dt.getStartDate() != null && dt.getStartDate().isAfter(monthEnd)) || (dt.getEndDate() != null && dt.getEndDate().isBefore(monthStart))) continue;
            DeductionMaster master = dt.getDidCode() == null ? null : deductionMasters.get(dt.getDidCode().trim().toUpperCase());
            int type = deductionType(dt.getDidCode(), dt.getDeductionName(), master, dt.getLoan(), dt.getUniform());
            transactionCuts.computeIfAbsent(dt.getEpfNo().trim().toUpperCase(), k -> new double[5])[type] += dt.getMonthlyCut().doubleValue();
        }

        List<TDeduction> allDeductions = deductionRepo.findAll();
        List<TLoan> allLoans = loanRepo.findAll();
        List<TAddition> allAdditions = additionRepo.findAll();
        List<StatutoryHoliday> monthHolidays = statutoryHolidays.holidaysForMonth(yr, mo);
        Map<String, String> holidayOverrides = statutoryHolidays.overridesForMonth(yr, mo);

        List<SalarySheetRow> results = new ArrayList<>();

        for (Map.Entry<String, List<Attendance>> entry : empPlantGroup.entrySet()) {
            String[] parts = entry.getKey().split("\\|");
            String epf = parts[0];
            String plant = parts[1];
            String businessCenter = parts.length > 2 ? parts[2] : "";
            List<Attendance> attList = entry.getValue();
            boolean primary = entry.getKey().equals(primaryKeyByEpf.get(epf));

            Employee emp = employeeMap.get(epf);
            Attendance firstRow = attList.isEmpty() ? null : attList.get(0);
            String empName = emp != null ?
                    ((emp.getFirstName() == null ? "" : emp.getFirstName()) + " " + (emp.getLastName() == null ? "" : emp.getLastName())).trim() :
                    (firstRow != null && firstRow.getEmployeeName() != null ? firstRow.getEmployeeName() : epf);

            double dailyRate = (emp != null && emp.getBasicSalary() != null && emp.getBasicSalary() > 0) ?
                    emp.getBasicSalary() : (firstRow != null && firstRow.getBasicSalary() != null ? firstRow.getBasicSalary() : 840.0);
            double poyaFallback = (emp != null && emp.getSundayPoyaExtra() != null && emp.getSundayPoyaExtra() > 0)
                    ? emp.getSundayPoyaExtra() : Math.round(dailyRate * 1.5 * 100.0) / 100.0;

            double shift1Days = 0;
            double shift2Days = 0;
            double poyaDays = 0;
            double poyaAmount = 0;
            double otHours = 0;
            double mealCount = 0;
            double mealValue = 0;
            double dayAllowanceTotal = 0;
            double employeeDayAllowance = emp != null && emp.getDayAllowance() != null ? emp.getDayAllowance() : 0;

            for (Attendance a : attList) {
                double weight = (a.getHalfDay() != null && (a.getHalfDay() == 1.0 || a.getHalfDay() == 0.5)) ? 0.5 : 1.0;
                if ("Y".equalsIgnoreCase(a.getSaturdayPoya())) {
                    poyaDays += weight;
                    // The row's Sunday Poya Extra is that day's amount
                    poyaAmount += (a.getSundayPoyaExtra() != null && a.getSundayPoyaExtra() > 0) ? a.getSundayPoyaExtra() : weight * poyaFallback;
                } else {
                    if ("Y".equalsIgnoreCase(a.getSecondShift())) shift2Days += weight;
                    else shift1Days += weight;
                    // Rate Value on the attendance report; Sunday Poya days do not earn it
                    dayAllowanceTotal += weight * (a.getDayAllowance() != null && a.getDayAllowance() > 0 ? a.getDayAllowance() : employeeDayAllowance);
                }
                if (a.getTotalOt() != null && a.getTotalOt() > 0) otHours += a.getTotalOt();
                if (a.getNoOfMeal() != null && a.getNoOfMeal() > 0) mealCount += a.getNoOfMeal();
                if (a.getTotalMealValue() != null && a.getTotalMealValue() > 0) mealValue += a.getTotalMealValue();
            }

            // Additions for this month/year, split into the salary sheet columns
            double performance = dayAllowanceTotal;
            double transport = 0;
            double attendanceAllowance = 0;
            double otherAdditions = 0;
            double statutoryPay = 0;
            double telephone = 0;
            double advance = 0;
            double uniform = 0;
            double loanCut = 0;
            double otherDeduct = 0;

            if (primary) {
                for (TAddition add : allAdditions) {
                    if (add.getEpfNo() == null || !epf.equalsIgnoreCase(add.getEpfNo().trim()) || add.getAddMonth() == null) continue;
                    if (!yr.equals(trim(add.getAddYear())) || !mo.equals(String.format("%02d", add.getAddMonth().intValue()))) continue;
                    double amt = add.getAddAmount() != null ? add.getAddAmount() : 0;
                    AdditionMaster master = add.getAddCode() == null ? null : additionMasters.get(add.getAddCode().trim().toUpperCase());
                    String name = master != null && master.getAdditionName() != null ? master.getAdditionName().toLowerCase() : "";
                    if (name.contains("performance")) performance += amt;
                    else if (name.contains("transport")) transport += amt;
                    else if (name.contains("attendance")) attendanceAllowance += amt;
                    else otherAdditions += amt;
                }

                // Deductions from TBL_T_Diduction; an advance here counts only if there is no advance transaction
                double[] cuts = transactionCuts.getOrDefault(epf, new double[5]);
                double[] monthly = new double[5];
                for (TDeduction d : allDeductions) {
                    if (d.getEpfNo() == null || !epf.equalsIgnoreCase(d.getEpfNo().trim())) continue;
                    if (!yr.equals(trim(d.getAddYear())) || !mo.equals(trim(d.getAddMonth()))) continue;
                    DeductionMaster master = d.getDidCode() == null ? null : deductionMasters.get(d.getDidCode().trim().toUpperCase());
                    monthly[deductionType(d.getDidCode(), null, master, null, null)] += d.getDidAmount() != null ? d.getDidAmount() : 0;
                }
                advance = cuts[TYPE_ADVANCE] > 0 ? cuts[TYPE_ADVANCE] : monthly[TYPE_ADVANCE];
                telephone = cuts[TYPE_TELEPHONE] + monthly[TYPE_TELEPHONE];
                uniform = cuts[TYPE_UNIFORM] + monthly[TYPE_UNIFORM];
                otherDeduct = cuts[TYPE_OTHER] + monthly[TYPE_OTHER];
                loanCut = cuts[TYPE_LOAN] + monthly[TYPE_LOAN];
                for (TLoan l : allLoans) {
                    if (l.getEpfNo() != null && epf.equalsIgnoreCase(l.getEpfNo().trim()) && l.getLoanAmount() != null && l.getLoanAmount() > 0) {
                        loanCut += (l.getLoanAmount() / 12.0);
                    }
                }
                loanCut = Math.round(loanCut * 100.0) / 100.0;

                // Statutory holiday pay comes from the holiday calendar; shown inside Other Ad
                if (emp != null && !monthHolidays.isEmpty()) {
                    statutoryPay = statutoryHolidays.computeForEmployee(emp, monthHolidays, holidayOverrides,
                            attendanceByEpf.getOrDefault(epf, List.of())).total;
                }
            }

            SalaryCalculation calc = calculateRow(
                    epf, empName, plant, yr, mo,
                    dailyRate, shift1Days, shift2Days, poyaDays, 0.0, otHours,
                    performance, transport + otherAdditions + statutoryPay, attendanceAllowance,
                    advance, telephone, mealValue, loanCut, uniform + otherDeduct,
                    Math.round(poyaAmount * 100.0) / 100.0
            );
            calc.setBusinessCenter(businessCenter);

            SalarySheetRow row = new SalarySheetRow();
            row.calculation = calc;
            row.epfNo = emp != null ? emp.getEpfNo().trim() : epf;
            row.employeeName = empName;
            row.nicNo = emp != null ? trim(emp.getNicNo()) : "";
            row.plantCode = plant;
            row.businessCenter = businessCenter;
            row.dayRate = dailyRate;
            row.shift1Days = shift1Days;
            row.shift1Total = round2(shift1Days * dailyRate);
            row.shift2Days = shift2Days;
            row.shift2Total = round2(shift2Days * dailyRate);
            row.poyaDays = poyaDays;
            row.poyaTotal = round2(poyaAmount);
            row.poyaRate = poyaDays > 0 ? round2(poyaAmount / poyaDays) : round2(dailyRate * 1.5);
            row.otHours = otHours;
            row.otRate = round2(dailyRate / 8.0 * 1.5);
            row.otTotal = round2(otHours * row.otRate);
            row.performanceAllowance = round2(performance);
            row.transportAllowance = transport;
            row.attendanceAllowance = attendanceAllowance;
            row.otherAdditions = round2(otherAdditions + statutoryPay);
            row.statutoryHolidayPay = statutoryPay;
            row.grossSalary = calc.getGrossSalary();
            row.epfBase = round2(row.shift1Total + row.shift2Total + row.poyaTotal);
            row.epf8 = calc.getEpf8();
            row.epf12 = calc.getEpf12();
            row.etf3 = calc.getEtf3();
            row.otherDeductions = round2(otherDeduct);
            row.uniform = round2(uniform);
            row.advance = round2(advance);
            row.telephone = round2(telephone);
            row.mealCount = mealCount;
            row.mealValue = round2(mealValue);
            row.loan = loanCut;
            row.totalDeductions = calc.getTotalDeductions();
            row.netSalary = calc.getNetSalary();
            results.add(row);
        }

        boolean allPlants = plantFilter == null || plantFilter.isBlank() || "ALL".equalsIgnoreCase(plantFilter.trim());
        return results.stream()
                .filter(r -> allPlants || r.plantCode.equalsIgnoreCase(plantFilter.trim()))
                .sorted(Comparator.comparing((SalarySheetRow r) -> r.plantCode, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(r -> r.epfNo.replaceAll("\\D", "").length())
                        .thenComparing(r -> r.epfNo, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private static final int TYPE_OTHER = 0;
    private static final int TYPE_ADVANCE = 1;
    private static final int TYPE_TELEPHONE = 2;
    private static final int TYPE_UNIFORM = 3;
    private static final int TYPE_LOAN = 4;

    // Deduction codes can be plain numbers ("05"), so the master name and flags decide the column
    private static int deductionType(String code, String name, DeductionMaster master, Boolean loanFlag, Boolean uniformFlag) {
        String text = ((code == null ? "" : code) + " " + (name == null ? "" : name) + " "
                + (master != null && master.getDudName() != null ? master.getDudName() : "")).toLowerCase();
        if (text.contains("tel") || text.contains("phone")) return TYPE_TELEPHONE;
        if (text.contains("adv")) return TYPE_ADVANCE;
        if (Boolean.TRUE.equals(uniformFlag) || text.contains("uniform") || (master != null && isYes(master.getIfUniform()))) return TYPE_UNIFORM;
        if (Boolean.TRUE.equals(loanFlag) || text.contains("loan") || (master != null && isYes(master.getIfLoan()))) return TYPE_LOAN;
        return TYPE_OTHER;
    }

    private static boolean isYes(String value) {
        return value != null && ("Y".equalsIgnoreCase(value.trim()) || "YES".equalsIgnoreCase(value.trim()) || "1".equals(value.trim()) || "TRUE".equalsIgnoreCase(value.trim()));
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    /** One line of the salary sheet: every column of the printed report. */
    public static class SalarySheetRow {
        @com.fasterxml.jackson.annotation.JsonIgnore
        public SalaryCalculation calculation;
        public String epfNo;
        public String employeeName;
        public String nicNo;
        public String plantCode;
        public String plantName;
        public String businessCenter;
        public double dayRate;
        public double shift1Days;
        public double shift1Total;
        public double shift2Days;
        public double shift2Total;
        public double poyaDays;
        public double poyaRate;
        public double poyaTotal;
        public double otHours;
        public double otRate;
        public double otTotal;
        public double performanceAllowance;
        public double transportAllowance;
        public double attendanceAllowance;
        public double otherAdditions;
        public double statutoryHolidayPay;
        public double grossSalary;
        public double epfBase;
        public double epf8;
        public double epf12;
        public double etf3;
        public double otherDeductions;
        public double uniform;
        public double advance;
        public double telephone;
        public double mealCount;
        public double mealValue;
        public double loan;
        public double totalDeductions;
        public double netSalary;

        @com.fasterxml.jackson.annotation.JsonIgnore
        public boolean hasValues() {
            return grossSalary != 0 || totalDeductions != 0;
        }
    }

    public List<SalaryCalculation> getCalculatedSalary(String year, String month, String plant, String businessCenter) {
        List<SalaryCalculation> records;
        if (year != null && !year.isBlank() && month != null && !month.isBlank()) {
            String mo = String.format("%02d", Integer.parseInt(month.trim()));
            if (plant != null && !plant.isBlank() && !"ALL".equalsIgnoreCase(plant.trim())) {
                records = salaryCalcRepo.findByYearAndMonthAndPlant(year.trim(), mo, plant.trim());
            } else {
                records = salaryCalcRepo.findByYearAndMonth(year.trim(), mo);
            }
        } else if (plant != null && !plant.isBlank() && !"ALL".equalsIgnoreCase(plant.trim())) {
            records = salaryCalcRepo.findByPlant(plant.trim());
        } else {
            records = salaryCalcRepo.findAll();
        }
        if (businessCenter == null || businessCenter.isBlank() || "ALL".equalsIgnoreCase(businessCenter.trim())) {
            return records;
        }
        return records.stream().filter(record -> businessCenter.equalsIgnoreCase(
                record.getBusinessCenter() == null ? "" : record.getBusinessCenter().trim())).toList();
    }
}
