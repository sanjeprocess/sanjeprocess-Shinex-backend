package com.hsb.hris.controller;

import com.hsb.hris.entity.DeductionMaster;
import com.hsb.hris.entity.DeductionTransaction;
import com.hsb.hris.entity.Employee;
import com.hsb.hris.entity.TDeduction;
import com.hsb.hris.repository.*;
import com.hsb.hris.service.ExcelStyleSalaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** The total advance of a plant must equal the sum of the advances of its employees. */
class SalaryAdvanceTotalTest {

    private final DeductionTransactionRepository transactions = mock(DeductionTransactionRepository.class);
    private final TDeductionRepository monthly = mock(TDeductionRepository.class);
    private final EmployeeRepository employees = mock(EmployeeRepository.class);
    private final DeductionMasterRepository masters = mock(DeductionMasterRepository.class);
    private final PlantRepository plants = mock(PlantRepository.class);
    private final ReportController controller = new ReportController(mock(AttendanceRepository.class), transactions, monthly,
            mock(SalaryCalculationRepository.class), employees, mock(BusinessCenterRepository.class), masters, plants,
            mock(ExcelStyleSalaryService.class));
    private final List<DeductionTransaction> txRows = new ArrayList<>();
    private final List<TDeduction> monthlyRows = new ArrayList<>();
    private final List<Employee> empRows = new ArrayList<>();

    @BeforeEach
    void setUp() {
        DeductionMaster advance = new DeductionMaster();
        advance.setDudCode("05");
        advance.setDudName("Salary Advance");
        when(masters.findAll()).thenReturn(List.of(advance));
        when(transactions.findAll()).thenReturn(txRows);
        when(monthly.findAll()).thenReturn(monthlyRows);
        when(monthly.findByBusinessCenterSmart("B001")).thenReturn(monthlyRows);
        when(employees.findAll()).thenReturn(empRows);
    }

    private void employee(String epf, String plant) {
        Employee e = new Employee();
        e.setEpfNo(epf);
        e.setFirstName(epf);
        e.setPlantCode(plant);
        e.setBusinessCenter("B001");
        empRows.add(e);
    }

    private void transaction(String epf, double cut, String businessCenter) {
        DeductionTransaction t = new DeductionTransaction();
        t.setId(epf);
        t.setEpfNo(epf);
        t.setDidCode("05");
        t.setBusinessCenter(businessCenter);
        t.setStatus("ACTIVE");
        t.setStartDate(LocalDate.of(2026, 1, 1));
        t.setEndDate(LocalDate.of(2026, 12, 31));
        t.setMonthlyCut(BigDecimal.valueOf(cut));
        t.setTotalAmount(BigDecimal.valueOf(cut));
        txRows.add(t);
    }

    private void monthlyEntry(String epf, String code, double amount) {
        TDeduction d = new TDeduction();
        d.setEpfNo(epf);
        d.setDidCode(code);
        d.setDidAmount(amount);
        d.setAddYear("2026");
        d.setAddMonth("10");
        monthlyRows.add(d);
    }

    private double total(String businessCenter, String plantCode) {
        return controller.salaryAdvanceTotal(businessCenter, 2026, 10, null).stream()
                .filter(r -> plantCode.equals(r.get("plantCode")))
                .mapToDouble(r -> ((BigDecimal) r.get("amount")).doubleValue())
                .sum();
    }

    private double sumOfEmployees(String businessCenter, String plantCode) {
        return controller.salaryAdvance(businessCenter, 2026, 10, plantCode).stream()
                .mapToDouble(r -> ((BigDecimal) r.get("monthlyCut")).doubleValue())
                .sum();
    }

    @Test
    void plantTotalIsTharinduPlusSanuka() {
        employee("E1", "T");
        employee("E2", "T");
        employee("E3", "X");
        transaction("E1", 1900, "B001");
        transaction("E2", 100, "B001");
        transaction("E3", 500, "B001");

        assertEquals(2000.0, total("B001", "T"));
        assertEquals(2000.0, sumOfEmployees("B001", "T"));
        assertEquals(500.0, total("B001", "X"));
        assertEquals(2, controller.salaryAdvance("B001", 2026, 10, "T").size());
    }

    @Test
    void transactionAndMonthlyEntriesAddUp() {
        employee("E1", "T");
        employee("E2", "T");
        transaction("E1", 1900, "B001");
        monthlyEntry("E2", "05", 100);

        assertEquals(2000.0, total("ALL", "T"));
        assertEquals(2000.0, sumOfEmployees("ALL", "T"));
    }

    @Test
    void twoMonthlyAdvancesOfOneEmployeeAreBothCounted() {
        employee("E1", "T");
        monthlyEntry("E1", "05", 1500);
        monthlyEntry("E1", "ADV2", 400);

        assertEquals(1900.0, total("ALL", "T"));
        assertEquals(1900.0, sumOfEmployees("ALL", "T"));
    }

    @Test
    void employeePlantSavedAsCodeAndNameIsTheSamePlant() {
        employee("E1", "T");
        employee("E2", "T / Plant T");
        transaction("E1", 1900, "B001");
        transaction("E2", 100, "B001");

        assertEquals(2000.0, total("B001", "T"));
        assertEquals(2000.0, sumOfEmployees("B001", "T"));
    }

    @Test
    void plantsWhoseCodesStartAlikeStaySeparate() {
        employee("E1", "BA POL");
        employee("E2", "BA RID");
        transaction("E1", 1900, "B001");
        transaction("E2", 100, "B001");

        assertEquals(1900.0, total("B001", "BA POL"));
        assertEquals(100.0, total("B001", "BA RID"));
        assertEquals(1900.0, sumOfEmployees("B001", "BA POL"));
    }

    @Test
    void businessCenterSavedAsCodeAndNameOrAllIsIncluded() {
        employee("E1", "T");
        employee("E2", "T");
        transaction("E1", 1900, "B001 / Shinex");
        transaction("E2", 100, "ALL");

        assertEquals(2000.0, total("B001", "T"));
        assertEquals(2000.0, sumOfEmployees("B001", "T"));
    }

    @Test
    void otherBusinessCenterIsNotCounted() {
        employee("E1", "T");
        transaction("E1", 1900, "B001");
        transaction("E1", 700, "B002");

        Map<String, Object> row = controller.salaryAdvanceTotal("B001", 2026, 10, null).get(0);
        assertEquals(1900.0, ((BigDecimal) row.get("amount")).doubleValue());
    }
}
