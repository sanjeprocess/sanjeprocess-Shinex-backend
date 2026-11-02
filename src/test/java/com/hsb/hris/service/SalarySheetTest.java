package com.hsb.hris.service;

import com.hsb.hris.entity.*;
import com.hsb.hris.repository.*;
import com.hsb.hris.service.ExcelStyleSalaryService.SalarySheetRow;
import com.hsb.hris.service.transaction.StatutoryHolidayService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Salary sheet columns checked against hand-worked figures from the printed sheet (rate 840). */
class SalarySheetTest {

    private final AttendanceRepository attendance = mock(AttendanceRepository.class);
    private final EmployeeRepository employees = mock(EmployeeRepository.class);
    private final TDeductionRepository deductions = mock(TDeductionRepository.class);
    private final TLoanRepository loans = mock(TLoanRepository.class);
    private final TAdditionRepository additions = mock(TAdditionRepository.class);
    private final AdditionMasterRepository additionMasters = mock(AdditionMasterRepository.class);
    private final DeductionMasterRepository deductionMasters = mock(DeductionMasterRepository.class);
    private final DeductionTransactionRepository transactions = mock(DeductionTransactionRepository.class);
    private final StatutoryHolidayService statutory = mock(StatutoryHolidayService.class);
    private final ExcelStyleSalaryService service = new ExcelStyleSalaryService(attendance, employees, deductions, loans, additions,
            additionMasters, deductionMasters, mock(SalaryCalculationRepository.class), transactions, statutory);
    private final List<Attendance> rows = new ArrayList<>();

    @BeforeEach
    void setUp() {
        Employee employee = new Employee();
        employee.setEpfNo("E72");
        employee.setFirstName("U D RAMYALATHA");
        employee.setPlantCode("P01");
        employee.setNicNo("196884504816");
        employee.setBasicSalary(840.0);
        when(employees.findAll()).thenReturn(List.of(employee));
        when(attendance.findByAttYearAndAttMonth("2024", "12")).thenReturn(rows);
        AdditionMaster performance = new AdditionMaster();
        performance.setAdditionCode("A1");
        performance.setAdditionName("Performance Allowance");
        AdditionMaster transport = new AdditionMaster();
        transport.setAdditionCode("A2");
        transport.setAdditionName("Transport");
        AdditionMaster other = new AdditionMaster();
        other.setAdditionCode("A3");
        other.setAdditionName("Bonus");
        when(additionMasters.findAll()).thenReturn(List.of(performance, transport, other));
        DeductionMaster advance = new DeductionMaster();
        advance.setDudCode("05");
        advance.setDudName("Salary Advance");
        DeductionMaster phone = new DeductionMaster();
        phone.setDudCode("23");
        phone.setDudName("Telephone");
        DeductionMaster uniform = new DeductionMaster();
        uniform.setDudCode("31");
        uniform.setDudName("Staff kit");
        uniform.setIfUniform("Y");
        when(deductionMasters.findAll()).thenReturn(List.of(advance, phone, uniform));
    }

    private void worked(String plant, int fromDay, int days, boolean secondShift, boolean poya, Double extra, double otEach) {
        for (int i = 0; i < days; i++) {
            Attendance a = new Attendance();
            a.setEpfNo("E72");
            a.setPlantCode(plant);
            a.setDayIn(LocalDate.of(2024, 12, fromDay + i));
            a.setSecondShift(secondShift ? "Y" : "N");
            a.setSaturdayPoya(poya ? "Y" : "N");
            a.setSundayPoyaExtra(extra);
            a.setTotalOt(otEach);
            a.setNoOfMeal(1.0);
            a.setTotalMealValue(100.0);
            rows.add(a);
        }
    }

    private void addition(String code, double amount) {
        TAddition add = new TAddition();
        add.setEpfNo("E72");
        add.setAddCode(code);
        add.setAddAmount(amount);
        add.setAddYear("2024");
        add.setAddMonth(BigDecimal.valueOf(12));
        List<TAddition> all = new ArrayList<>(additions.findAll());
        all.add(add);
        when(additions.findAll()).thenReturn(all);
    }

    private void deduction(String code, double amount) {
        TDeduction d = new TDeduction();
        d.setEpfNo("E72");
        d.setDidCode(code);
        d.setDidAmount(amount);
        d.setAddYear("2024");
        d.setAddMonth("12");
        List<TDeduction> all = new ArrayList<>(deductions.findAll());
        all.add(d);
        when(deductions.findAll()).thenReturn(all);
    }

    private List<SalarySheetRow> sheet() {
        return service.buildSalarySheet("2024", "12", null, "ALL");
    }

    @Test
    void columnsMatchThePrintedSheet() {
        worked("P01", 1, 20, false, false, null, 2.4375);  // 20 x 2.4375 = 48.75 OT hours
        worked("P01", 22, 1, false, true, null, 0);
        addition("A1", 3039);
        addition("A2", 500);
        addition("A3", 2657.60);

        SalarySheetRow r = sheet().get(0);

        assertEquals(20.0, r.shift1Days);
        assertEquals(840.0, r.dayRate);
        assertEquals(16800.0, r.shift1Total);
        assertEquals(1.0, r.poyaDays);
        assertEquals(1260.0, r.poyaRate);
        assertEquals(1260.0, r.poyaTotal);
        assertEquals(48.75, r.otHours);
        assertEquals(157.5, r.otRate);
        assertEquals(7678.13, r.otTotal);
        assertEquals(3039.0, r.performanceAllowance);
        assertEquals(500.0, r.transportAllowance);
        assertEquals(2657.6, r.otherAdditions);
        // 16,800 + 1,260 + 7,678.13 + 3,039 + 500 + 2,657.60
        assertEquals(31934.73, r.grossSalary, 0.001);
        // EPF is on 18,060 only, as printed: 1,444.80 / 2,167.20 / 541.80
        assertEquals(18060.0, r.epfBase);
        assertEquals(1444.8, r.epf8);
        assertEquals(2167.2, r.epf12);
        assertEquals(541.8, r.etf3);
        assertEquals(21.0, r.mealCount);
        assertEquals(2100.0, r.mealValue);
        // EPF 1,444.80 + meals 2,100
        assertEquals(3544.8, r.totalDeductions, 0.001);
        assertEquals(28389.93, r.netSalary, 0.001);
    }

    @Test
    void chandralathaLineFromThePrintedSheet() {
        // D M CHANDRALATHA - E73, December 2024: her 21 attendance rows and OT from the attendance report
        double[] ot = {2, 3.25, 2, 2, 2, 2, 6.25, 3, 2, 2, 2, 2, 5.25, 2, 2, 0, 2, 2, 2, 2, 2};
        for (int i = 0; i < ot.length; i++) {
            worked("P01", i + 1, 1, false, false, null, ot[i]);
            rows.get(i).setDayAllowance(152.95);
            rows.get(i).setNoOfMeal(0.0);
            rows.get(i).setTotalMealValue(0.0);
        }
        addition("A3", 2701.84);

        SalarySheetRow r = sheet().get(0);

        assertEquals(21.0, r.shift1Days);
        assertEquals(17640.0, r.shift1Total);
        assertEquals(49.75, r.otHours);
        assertEquals(157.5, r.otRate);
        assertEquals(7835.63, r.otTotal);            // printed 7,835.64
        assertEquals(3211.95, r.performanceAllowance);
        assertEquals(31389.42, r.grossSalary, 0.001); // printed 31,389.43
        assertEquals(17640.0, r.epfBase);            // C Form Total Earning 17,640.00
        assertEquals(1411.2, r.epf8);
        assertEquals(2116.8, r.epf12);
        assertEquals(29978.22, r.netSalary, 0.001);   // printed 29,978.23
    }

    @Test
    void sundayPoyaDayDoesNotEarnDayAllowance() {
        worked("P01", 1, 20, false, false, null, 0);
        worked("P01", 22, 1, false, true, null, 0);
        rows.forEach(a -> a.setDayAllowance(152.95));

        assertEquals(3059.0, sheet().get(0).performanceAllowance); // 20 x 152.95, as printed for E72
    }

    @Test
    void employeeWithDeductionButNoAttendanceIsMarkedAndAttendedOneIsNot() {
        deduction("23", 300);
        assertEquals(false, sheet().get(0).hasAttendance);

        worked("P01", 1, 1, false, false, null, 0);
        assertEquals(true, sheet().get(0).hasAttendance);
    }

    @Test
    void businessCenterUsesTheSameMatchingAsTheAttendanceReport() {
        List<Employee> centerEmployees = employees.findAll();
        when(employees.findByBusinessCenterSmart("B001")).thenReturn(centerEmployees);
        worked("P01", 1, 1, false, false, null, 0);
        rows.get(0).setAttYear("2024");
        rows.get(0).setAttMonth("12");
        rows.get(0).setBusinessCenter("B001 / Shinex");
        Attendance otherMonth = new Attendance();
        otherMonth.setEpfNo("E72");
        otherMonth.setPlantCode("P01");
        otherMonth.setAttYear("2024");
        otherMonth.setAttMonth("11");
        when(attendance.findByBusinessCenterSmart("B001")).thenReturn(List.of(rows.get(0), otherMonth));

        SalarySheetRow r = service.buildSalarySheet("2024", "12", null, "B001").get(0);

        assertEquals(1.0, r.shift1Days);
        assertEquals(true, r.hasAttendance);
    }

    @Test
    void numericDeductionCodesGoToTheRightColumns() {
        worked("P01", 1, 1, false, false, null, 0);
        deduction("05", 5000);
        deduction("23", 300);
        deduction("31", 750);
        deduction("99", 40);

        SalarySheetRow r = sheet().get(0);

        assertEquals(5000.0, r.advance);
        assertEquals(300.0, r.telephone);
        assertEquals(750.0, r.uniform);
        assertEquals(40.0, r.otherDeductions);
        // EPF 67.20 + meal 100 + 5,000 + 300 + 750 + 40
        assertEquals(6257.2, r.totalDeductions, 0.001);
    }

    @Test
    void advanceTransactionIsNotCountedTwiceWithMonthlyEntry() {
        worked("P01", 1, 1, false, false, null, 0);
        deduction("05", 5000);
        DeductionTransaction dt = new DeductionTransaction();
        dt.setEpfNo("E72");
        dt.setDidCode("05");
        dt.setStatus("ACTIVE");
        dt.setMonthlyCut(BigDecimal.valueOf(2000));
        when(transactions.findAll()).thenReturn(List.of(dt));

        assertEquals(2000.0, sheet().get(0).advance);
    }

    @Test
    void twoPlantsPayAdditionsAndDeductionsOnceOnHomePlant() {
        worked("P02", 1, 2, false, false, null, 0);
        worked("P01", 5, 3, false, false, null, 0);
        addition("A1", 1000);
        deduction("23", 300);

        List<SalarySheetRow> sheet = sheet();

        assertEquals(2, sheet.size());
        SalarySheetRow home = sheet.get(0);
        SalarySheetRow other = sheet.get(1);
        assertEquals("P01", home.plantCode);
        assertEquals(3.0, home.shift1Days);
        assertEquals(1000.0, home.performanceAllowance);
        assertEquals(300.0, home.telephone);
        assertEquals("P02", other.plantCode);
        assertEquals(2.0, other.shift1Days);
        assertEquals(0.0, other.performanceAllowance);
        assertEquals(0.0, other.telephone);
    }

    @Test
    void secondShiftAndSundayPoyaExtraAreSeparate() {
        worked("P01", 1, 2, true, false, null, 0);
        worked("P01", 8, 2, false, true, 3000.0, 0);

        SalarySheetRow r = sheet().get(0);

        assertEquals(0.0, r.shift1Days);
        assertEquals(2.0, r.shift2Days);
        assertEquals(1680.0, r.shift2Total);
        assertEquals(2.0, r.poyaDays);
        assertEquals(6000.0, r.poyaTotal);      // each day's extra once, not 2 x 6,000
        assertEquals(3000.0, r.poyaRate);
        assertEquals(7680.0, r.epfBase);
    }

    @Test
    void statutoryHolidayPayFromCalendarIsInOtherAdditionsOnce() {
        worked("P01", 1, 1, false, false, null, 0);
        worked("P02", 2, 1, false, false, null, 0);
        StatutoryHoliday holiday = new StatutoryHoliday();
        holiday.setHolidayDate(LocalDate.of(2024, 12, 25));
        when(statutory.holidaysForMonth("2024", "12")).thenReturn(List.of(holiday));
        StatutoryHolidayService.EmployeePay pay = new StatutoryHolidayService.EmployeePay();
        pay.total = 3000;
        when(statutory.computeForEmployee(any(Employee.class), anyList(), anyMap(), anyList())).thenReturn(pay);

        List<SalarySheetRow> sheet = sheet();

        assertEquals(3000.0, sheet.get(0).otherAdditions);
        assertEquals(0.0, sheet.get(1).otherAdditions);
        assertEquals(840.0, sheet.get(0).epfBase);  // not part of the EPF amount
    }
}
