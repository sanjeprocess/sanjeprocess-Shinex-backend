package com.hsb.hris.service;

import com.hsb.hris.entity.SalaryCalculation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExcelStyleSalaryServiceTest {

    @Test
    @DisplayName("Employee 1: A MURALIDARAN - E263 net salary matches Excel sheet to 2 decimals")
    void testEmployee1_AMuralidaran() {
        SalaryCalculation calc = ExcelStyleSalaryService.calculateRow(
                "E263",
                "A MURALIDARAN",
                "PL001",
                "2025",
                "07",
                840.0,  // Daily Rate
                10.0,   // Shift 1 Days
                15.0,   // Shift 2 Days
                1.0,    // Sunday/Poya Days
                0.0,    // Statutory Holiday Days
                44.0,   // OT Hours
                6658.0, // Performance Allowance
                4941.0, // Other Additions
                0.0,    // Att Allowance
                5000.0, // Advance
                0.0,    // Telephone
                0.0,    // Meal
                0.0,    // Loan
                0.0     // Other Deductions
        );

        assertEquals(40789.00, calc.getGrossSalary(), 0.01, "Gross salary must match Excel");
        assertEquals(1780.80, calc.getEpf8(), 0.01, "EPF 8% must match Excel");
        assertEquals(6780.80, calc.getTotalDeductions(), 0.01, "Total deductions must match Excel");
        assertEquals(34008.20, calc.getNetSalary(), 0.01, "Net salary must match Excel to 2 decimals (34,008.20)");
    }

    @Test
    @DisplayName("Employee 2: P DARMALINGAM - E264 net salary matches Excel sheet to 2 decimals")
    void testEmployee2_PDarmalingam() {
        SalaryCalculation calc = ExcelStyleSalaryService.calculateRow(
                "E264",
                "P DARMALINGAM",
                "PL001",
                "2025",
                "07",
                840.0,   // Daily Rate
                15.0,    // Shift 1 Days
                11.0,    // Shift 2 Days
                0.0,     // Sunday/Poya Days
                0.0,     // Statutory Holiday Days
                10.0,    // OT Hours
                0.0,     // Performance Allowance
                4776.24, // Other Additions
                0.0,     // Att Allowance
                3000.0,  // Advance
                0.0,     // Telephone
                0.0,     // Meal
                0.0,     // Loan
                0.0      // Other Deductions
        );

        assertEquals(28191.24, calc.getGrossSalary(), 0.01, "Gross salary must match Excel");
        assertEquals(1747.20, calc.getEpf8(), 0.01, "EPF 8% must match Excel");
        assertEquals(4747.20, calc.getTotalDeductions(), 0.01, "Total deductions must match Excel");
        assertEquals(23444.04, calc.getNetSalary(), 0.01, "Net salary must match Excel to 2 decimals (23,444.04)");
    }

    @Test
    @DisplayName("Employee 3: V CHIRSTEEN - E265 net salary matches Excel sheet to 2 decimals")
    void testEmployee3_VChirsteen() {
        SalaryCalculation calc = ExcelStyleSalaryService.calculateRow(
                "E265",
                "V CHIRSTEEN",
                "PL001",
                "2025",
                "07",
                840.0,   // Daily Rate
                15.0,    // Shift 1 Days
                11.0,    // Shift 2 Days
                1.0,     // Sunday/Poya Days
                0.0,     // Statutory Holiday Days
                10.0,    // OT Hours
                1235.0,  // Performance Allowance
                4776.24, // Other Additions
                0.0,     // Att Allowance
                3000.0,  // Advance
                0.0,     // Telephone
                0.0,     // Meal
                0.0,     // Loan
                0.0      // Other Deductions
        );

        assertEquals(30686.24, calc.getGrossSalary(), 0.01, "Gross salary must match Excel");
        assertEquals(1848.00, calc.getEpf8(), 0.01, "EPF 8% must match Excel");
        assertEquals(4848.00, calc.getTotalDeductions(), 0.01, "Total deductions must match Excel");
        assertEquals(25838.24, calc.getNetSalary(), 0.01, "Net salary must match Excel to 2 decimals (25,838.24)");
    }
}
