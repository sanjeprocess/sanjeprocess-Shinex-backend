package com.hsb.hris.controller;

import com.hsb.hris.controller.process.MonthlySummaryController;
import com.hsb.hris.entity.Attendance;
import com.hsb.hris.entity.Employee;
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
        Employee employee = new Employee();
        employee.setEpfNo("E1");
        employee.setFirstName("Test");
        employee.setBasicSalary(2500.0);
        employee.setSundayPoyaExtra(employeeExtra);

        EmployeeRepository employees = mock(EmployeeRepository.class);
        when(employees.findByTrimmedEpfNo("E1")).thenReturn(Optional.of(employee));
        AttendanceRepository attendance = mock(AttendanceRepository.class);
        when(attendance.findByEpfNoAndAttYearAndAttMonth("E1", "2026", "10")).thenReturn(rows);
        StatutoryHolidayService statutory = mock(StatutoryHolidayService.class);
        when(statutory.computeForEmployee(any(Employee.class), anyString(), anyString(), any())).thenReturn(new StatutoryHolidayService.EmployeePay());

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
    void otAmountIsSavedOtHoursTimesOneAndHalfHourlyRate() {
        Map<String, Object> result = summary(null, new ArrayList<>(List.of(
                row("2026-10-12", false, null, 1.0),
                row("2026-10-13", false, null, 0.5))));

        assertEquals(1.5, result.get("overtimeHours"));
        assertEquals(468.75, result.get("overtimeRate")); // 2,500 / 8 x 1.5
        assertEquals(703.13, result.get("overtimeAmount"));
    }
}
