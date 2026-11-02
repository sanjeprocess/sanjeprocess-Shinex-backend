package com.hsb.hris.service;

import com.hsb.hris.entity.Attendance;
import com.hsb.hris.entity.Employee;
import com.hsb.hris.entity.StatutoryHoliday;
import com.hsb.hris.service.transaction.StatutoryHolidayService;
import com.hsb.hris.service.transaction.StatutoryHolidayService.EmployeePay;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class StatutoryHolidayServiceTest {

    private final StatutoryHolidayService service = new StatutoryHolidayService(null, null, null, null);

    private static StatutoryHoliday holiday(String date, double working, double notWorking) {
        StatutoryHoliday h = new StatutoryHoliday();
        h.setHolidayDate(LocalDate.parse(date));
        h.setWorkingValue(working);
        h.setNotWorkingValue(notWorking);
        return h;
    }

    private static Employee employee(String epf, Boolean eligible) {
        Employee e = new Employee();
        e.setEpfNo(epf);
        e.setStatutoryHolidayEligible(eligible);
        return e;
    }

    private static Attendance attended(String epf, String date) {
        Attendance a = new Attendance();
        a.setEpfNo(epf);
        a.setDayIn(LocalDate.parse(date));
        return a;
    }

    private final List<StatutoryHoliday> october = List.of(
            holiday("2026-10-07", 2000, 1000),
            holiday("2026-10-21", 2000, 1000));

    @Test
    void eligibleEmployeeGetsWorkingOrNotWorkingValuePerHoliday() {
        // Worked on Oct 21 only -> 1,000 (not working) + 2,000 (working)
        EmployeePay pay = service.computeForEmployee(employee("E1", true), october, Map.of(),
                List.of(attended("E1", "2026-10-21"), attended("E1", "2026-10-22")));

        assertEquals(3000.0, pay.total);
        assertEquals(2, pay.count);
        assertEquals("N", pay.lines.get(0).status);
        assertEquals(1000.0, pay.lines.get(0).amount);
        assertEquals("W", pay.lines.get(1).status);
        assertEquals("ATTENDANCE", pay.lines.get(1).source);
    }

    @Test
    void manualSelectionOverridesAttendance() {
        EmployeePay pay = service.computeForEmployee(employee("E1", true), october,
                Map.of("2026-10-07|E1", "W", "2026-10-21|E1", "N"),
                List.of(attended("E1", "2026-10-21")));

        assertEquals(3000.0, pay.total);
        assertEquals("W", pay.lines.get(0).status);
        assertEquals("MANUAL", pay.lines.get(0).source);
        assertEquals("N", pay.lines.get(1).status);
    }

    @Test
    void ineligibleEmployeeGetsNothing() {
        EmployeePay pay = service.computeForEmployee(employee("E2", false), october, Map.of(),
                List.of(attended("E2", "2026-10-07")));

        assertEquals(0.0, pay.total);
        assertEquals(0, pay.count);
        assertTrue(pay.lines.stream().allMatch(l -> l.amount == 0.0));
    }

    @Test
    void noHolidaysMeansZero() {
        EmployeePay pay = service.computeForEmployee(employee("E1", true), List.of(), Map.of(), List.of());
        assertEquals(0.0, pay.total);
        assertTrue(pay.lines.isEmpty());
    }

    @Test
    void otherEmployeesAttendanceDoesNotCount() {
        EmployeePay pay = service.computeForEmployee(employee("E1", null), october, Map.of(),
                List.of(attended("E9", "2026-10-07")));
        // null eligibility = eligible (form default); E9's attendance must not make E1 "working"
        assertEquals(2000.0, pay.total);
    }
}
