package com.hsb.hris.controller;

import com.hsb.hris.controller.process.MonthlySummaryController;
import com.hsb.hris.entity.Attendance;
import com.hsb.hris.entity.Employee;
import com.hsb.hris.entity.StatutoryHoliday;
import com.hsb.hris.repository.*;
import com.hsb.hris.service.transaction.StatutoryHolidayService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Payslip values for Sunday Poya and OT, calculated from attendance rows. */
class MonthlySummaryPoyaOtTest {

    private static Attendance row(String day, boolean poya, Double extra, double ot) {
        Attendance a = new Attendance();
        a.setEpfNo("E1");
        a.setDayIn(LocalDate.parse(day));
        a.setSaturdayPoya(poya ? "Y" : "N");
        a.setSundayPoyaExtra(extra);
        a.setTotalOt(ot);
        return a;
    }

    private Map<String, Object> summary(Double employeeExtra, List<Attendance> rows) {
        return summary(2500.0, employeeExtra, rows);
    }

    private Map<String, Object> summary(double basicSalary, Double employeeExtra, List<Attendance> rows) {
        return summary(basicSalary, employeeExtra, rows, List.of(), true);
    }

    private Map<String, Object> summary(double basicSalary, Double employeeExtra, List<Attendance> rows,
                                        List<StatutoryHoliday> holidays, boolean eligible) {
        Employee employee = new Employee();
        employee.setEpfNo("E1");
        employee.setFirstName("Test");
        employee.setBasicSalary(basicSalary);
        employee.setSundayPoyaExtra(employeeExtra);
        employee.setStatutoryHolidayEligible(eligible);

        EmployeeRepository employees = mock(EmployeeRepository.class);
        when(employees.findByTrimmedEpfNo("E1")).thenReturn(Optional.of(employee));
        AttendanceRepository attendance = mock(AttendanceRepository.class);
        when(attendance.findByEpfNoAndAttYearAndAttMonth("E1", "2026", "10")).thenReturn(rows);
        StatutoryHolidayService statutory = mock(StatutoryHolidayService.class);
        when(statutory.computeForEmployee(any(Employee.class), anyString(), anyString(), any())).thenReturn(new StatutoryHolidayService.EmployeePay());
        when(statutory.holidaysForMonth("2026", "10")).thenReturn(holidays);

        return new MonthlySummaryController(employees, mock(TAdditionRepository.class), mock(TDeductionRepository.class),
                mock(TLoanRepository.class), mock(AttendanceSummaryRepository.class), mock(AdditionMasterRepository.class),
                mock(DeductionMasterRepository.class), attendance, mock(PlantRepository.class), mock(SectionRepository.class),
                statutory).summary("E1", "2026", "10");
    }

    @Test
    void twoSundayPoyaDaysPayEachRowsExtraOnce() {
        List<Attendance> rows = new ArrayList<>(List.of(
                row("2026-10-04", true, 3750.0, 0),
                row("2026-10-11", true, 3750.0, 0),
                row("2026-10-12", false, null, 0)));

        Map<String, Object> result = summary(null, rows);

        assertEquals(2.0, result.get("poyaDays"));
        assertEquals(7500.0, result.get("poyaAmount"));   // not 2 x 7,500 = 15,000
        assertEquals(3750.0, result.get("poyaRate"));
        assertEquals(1.0, result.get("normalDays"));
    }

    @Test
    void differentExtrasAreAddedExactly() {
        Map<String, Object> result = summary(null, new ArrayList<>(List.of(
                row("2026-10-04", true, 1000.0, 0),
                row("2026-10-11", true, 2500.0, 0),
                row("2026-10-12", false, null, 0))));

        assertEquals(3500.0, result.get("poyaAmount"));
    }

    @Test
    void rowWithoutExtraUsesEmployeeValueThenOneAndHalfRate() {
        List<Attendance> rows = List.of(row("2026-10-04", true, null, 0), row("2026-10-12", false, null, 0));

        assertEquals(1000.0, summary(1000.0, new ArrayList<>(rows)).get("poyaAmount"));
        assertEquals(3750.0, summary(null, new ArrayList<>(rows)).get("poyaAmount"));   // 2,500 x 1.5
    }

    @Test
    void extraOnANonPoyaRowIsNotPaid() {
        Map<String, Object> result = summary(null, new ArrayList<>(List.of(row("2026-10-12", false, 500.0, 0))));

        assertEquals(0.0, result.get("poyaAmount"));
    }

    @Test
    void monthOfOnlySundayPoyaIsNotAlsoPaidAsNormalDays() {
        Map<String, Object> result = summary(null, new ArrayList<>(List.of(
                row("2026-10-04", true, 3750.0, 0),
                row("2026-10-11", true, 3750.0, 0))));

        assertEquals(0.0, result.get("normalDays"));
        assertEquals(0.0, result.get("normalAmount"));
        assertEquals(7500.0, result.get("poyaAmount"));
        assertEquals(7500.0, result.get("grossSalary"));
    }

    @Test
    void epfIsChargedOnNormalAndSundayPoyaPay() {
        Map<String, Object> result = summary(null, new ArrayList<>(List.of(
                row("2026-10-04", true, 3750.0, 0),
                row("2026-10-12", false, null, 0))));

        assertEquals(6250.0 * 0.08, (Double) result.get("epf8"), 0.001);  // (2,500 + 3,750) x 8%
    }

    @Test
    void specialDaysArePaidAsNormalDays() {
        Attendance special = row("2026-10-13", false, null, 0);
        special.setSpecialDay("Y");
        Map<String, Object> result = summary(null, new ArrayList<>(List.of(row("2026-10-12", false, null, 0), special)));

        assertEquals(2.0, result.get("normalDays"));
        assertEquals(5000.0, result.get("normalAmount"));
    }

    @Test
    void savedAttendanceFillsEveryPaySheetField() {
        List<Attendance> rows = new ArrayList<>();
        for (int day = 1; day <= 20; day++) rows.add(row(String.format("2026-10-%02d", day), false, null, day <= 4 ? 1.0 : 0));

        Map<String, Object> result = summary(500.0, null, rows);

        assertEquals(20.0, result.get("normalDays"));
        assertEquals(500.0, result.get("normalRate"));
        assertEquals(10000.0, result.get("normalAmount"));
        assertEquals(4.0, result.get("overtimeHours"));
        assertEquals(93.75, result.get("overtimeRate"));
        assertEquals(375.0, result.get("overtimeAmount"));
        assertEquals(10375.0, result.get("grossSalary"));
        assertEquals(800.0, (Double) result.get("epf8"), 0.001);
        assertEquals(800.0, (Double) result.get("totalDeductions"), 0.001);
        assertEquals(9575.0, (Double) result.get("netSalary"), 0.001);
        assertEquals(1200.0, (Double) result.get("epf12"), 0.001);
        assertEquals(300.0, (Double) result.get("etf3"), 0.001);
    }

    @Test
    void monthWithNoSavedAttendanceIsZeroWithTheRatesStillShown() {
        Map<String, Object> result = summary(500.0, null, new ArrayList<>());

        assertEquals(0.0, result.get("normalDays"));
        assertEquals(500.0, result.get("normalRate"));
        assertEquals(93.75, result.get("overtimeRate"));
        assertEquals(0.0, result.get("grossSalary"));
        assertEquals(0.0, result.get("netSalary"));
    }

    @Test
    void otAmountIsSavedOtHoursTimesOneAndHalfHourlyRate() {
        Map<String, Object> result = summary(null, new ArrayList<>(List.of(
                row("2026-10-12", false, null, 1.0),
                row("2026-10-13", false, null, 0.5))));

        assertEquals(1.5, result.get("overtimeHours"));
        assertEquals(468.75, result.get("overtimeRate")); // 2,500 / 8 x 1.5
        assertEquals(703.13, result.get("overtimeAmount"));
    }
    private static StatutoryHoliday holiday(String day) {
        StatutoryHoliday h = new StatutoryHoliday();
        h.setHolidayDate(LocalDate.parse(day));
        return h;
    }

    @Test
    void otOnAStatutoryHolidayIsPaidOnTheStatutoryLine() {
        // Day Rate 1,800: 1,800 / 9 = 200 an hour, x 2 = 400; 5 OT hours = 2,000
        Map<String, Object> result = summary(1800.0, null, new ArrayList<>(List.of(
                row("2026-10-12", false, null, 2.0),
                row("2026-10-21", false, null, 5.0))), List.of(holiday("2026-10-21")), true);

        assertEquals(5.0, result.get("statutoryOtHours"));
        assertEquals(400.0, result.get("statutoryOtRate"));
        assertEquals(2000.0, result.get("statutoryOtAmount"));
        assertEquals(2000.0, result.get("statutoryHolidayAmount"));
        assertEquals(2.0, result.get("overtimeHours"));      // only the normal day's OT
        assertEquals(675.0, result.get("overtimeAmount"));   // 2 x 1,800 / 8 x 1.5
        assertEquals(3600.0 + 675.0 + 2000.0, result.get("grossSalary"));
        assertEquals(3600.0 * 0.08, (Double) result.get("epf8"), 0.001);  // OT is not in the EPF amount
    }

    @Test
    void holidayOtStaysNormalOtWhenNotEligibleOrNoHoliday() {
        List<Attendance> rows = List.of(row("2026-10-12", false, null, 2.0), row("2026-10-21", false, null, 5.0));

        Map<String, Object> notEligible = summary(1800.0, null, new ArrayList<>(rows), List.of(holiday("2026-10-21")), false);
        Map<String, Object> noHoliday = summary(1800.0, null, new ArrayList<>(rows), List.of(), true);

        for (Map<String, Object> result : List.of(notEligible, noHoliday)) {
            assertEquals(0.0, result.get("statutoryOtAmount"));
            assertEquals(7.0, result.get("overtimeHours"));
            assertEquals(2362.5, result.get("overtimeAmount"));   // 7 x 337.50
        }
    }

    @Test
    void nightShiftDaysAreCountedForTheNightLineAndTotalsStayTheSame() {
        Attendance night = row("2026-10-13", false, null, 0);
        night.setNightShift("Y");
        Attendance poyaNight = row("2026-10-04", true, 2250.0, 0);
        poyaNight.setNightShift("Y");
        Map<String, Object> result = summary(1500.0, null, new ArrayList<>(List.of(row("2026-10-12", false, null, 0), night, poyaNight)));

        assertEquals(2.0, result.get("normalDays"));
        assertEquals(1.0, result.get("nightNormalDays"));   // the Poya night stays on the Poya line
        assertEquals(2.0, result.get("nightDays"));
        assertEquals(3000.0, result.get("normalAmount"));
        assertEquals(5250.0, result.get("grossSalary"));   // 2 x 1,500 + 2,250, unchanged
    }
}
