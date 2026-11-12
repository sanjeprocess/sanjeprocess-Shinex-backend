package com.hsb.hris.controller;

import com.hsb.hris.controller.transaction.AttendanceController;
import com.hsb.hris.entity.Attendance;
import com.hsb.hris.repository.AttendanceRepository;
import com.hsb.hris.repository.EmployeeRepository;
import com.hsb.hris.service.transaction.AttendanceService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Bulk attendance save: one lookup for saved rows, plain inserts for new rows, updates for saved ones. */
class AttendanceBulkSaveTest {

    private final AttendanceRepository attendance = mock(AttendanceRepository.class);
    private final EmployeeRepository employees = mock(EmployeeRepository.class);
    private final EntityManager entityManager = mock(EntityManager.class);
    private final AttendanceController controller;

    AttendanceBulkSaveTest() {
        PlatformTransactionManager tx = mock(PlatformTransactionManager.class);
        when(tx.getTransaction(any())).thenReturn(mock(TransactionStatus.class));
        controller = new AttendanceController(mock(AttendanceService.class), attendance, employees, mock(JdbcTemplate.class), tx);
        ReflectionTestUtils.setField(controller, "entityManager", entityManager);
        List<Object[]> lookup = new ArrayList<>();
        lookup.add(new Object[] {"E73", "D M", "CHANDRALATHA", "130013 / Ridigama", "J01", "B001"});
        lookup.add(new Object[] {"E74", "W A", "PEMALATHA", "130013", "J01", "B001"});
        when(employees.findAttendanceLookup()).thenReturn(lookup);
    }

    private static Attendance row(String epf, String day, double ot) {
        Attendance a = new Attendance();
        a.setEpfNo(epf);
        a.setDayIn(LocalDate.parse(day));
        a.setTotalOt(ot);
        return a;
    }

    @Test
    @SuppressWarnings("unchecked")
    void newRowsAreInsertedAndSavedRowsUpdated() {
        Attendance saved = row("E73 ", "2026-10-02", 1.0);   // as stored, with a padded EPF
        saved.setAttYear("2026");
        saved.setAttMonth("10");
        when(attendance.findByAttYearAndAttMonthAndEpfNoInAndDayInIn(eq("2026"), eq("10"), anyCollection(), anyCollection()))
                .thenReturn(List.of(saved));
        when(attendance.save(any(Attendance.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> result = (Map<String, Object>) controller.bulkSave(new ArrayList<>(List.of(
                row("E73", "2026-10-01", 2.0),
                row("e73", "2026-10-02", 3.5),
                row("E74", "2026-10-01", 0)))).getBody();

        assertEquals(2, result.get("inserted"));
        assertEquals(1, result.get("updated"));
        assertEquals(0, result.get("failed"));
        assertEquals(3.5, saved.getTotalOt());              // the saved row took the new values
        assertEquals("130013", saved.getPlantCode());        // plant from the employee master, code only
        verify(entityManager, times(2)).persist(any(Attendance.class));
        verify(attendance, times(1)).save(saved);
        verify(attendance, times(1)).findByAttYearAndAttMonthAndEpfNoInAndDayInIn(any(), any(), anyCollection(), anyCollection());
        verify(attendance, never()).findById(any());
        verify(employees, never()).findAll();
    }

    @Test
    @SuppressWarnings("unchecked")
    void employeeFieldsFillTheRowAndAFailedInsertIsCounted() {
        doThrow(new RuntimeException("duplicate key")).when(entityManager).persist(argThat(a -> "E74".equals(((Attendance) a).getEpfNo())));
        List<Attendance> rows = new ArrayList<>(List.of(row("E73", "2026-10-05", 0), row("E74", "2026-10-05", 0)));

        Map<String, Object> result = (Map<String, Object>) controller.bulkSave(rows).getBody();

        assertEquals(1, result.get("inserted"));
        assertEquals(1, result.get("failed"));
        Attendance first = rows.get(0);
        assertEquals("D M CHANDRALATHA", first.getEmployeeName());
        assertEquals("J01", first.getSectionCode());
        assertEquals("B001", first.getBusinessCenter());
        assertEquals("2026", first.getAttYear());
        assertEquals("10", first.getAttMonth());
    }
}
