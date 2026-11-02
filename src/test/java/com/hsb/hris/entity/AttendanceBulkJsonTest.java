package com.hsb.hris.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/** The payload the frontend sends to POST /api/attendance/bulk must deserialize without a 400. */
class AttendanceBulkJsonTest {

    private final ObjectMapper mapper = JsonMapper.builder().findAndAddModules().build();

    @Test
    void bulkRowFromExcelDeserializes() throws Exception {
        String json = """
            {"attMonth":"10","epfNo":"E1","plantCode":"P01","dayIn":"2026-10-07","timeIn":"07:30:00",
             "businessCenter":"BC1","customerCode":"P01","sectionCode":"","workingDays":1,"halfDay":0,"noOfMeal":1,
             "employeeName":"Ann Perera","timeOut":"17:30:00","dayShift":"N","secondShift":"N","nightShift":"Y",
             "fullNight":"N","normalDay":"N","saturdayPoya":"Y","specialDay":"N","otCalAuto":"Y","basicSalary":2500,
             "dayAllowance":0,"nightAllowance":300,"noOfStaff":5,"attAllowance":"N","poyaSaturdayNormal":"N",
             "lateAllowNo":"N","mealType":"Standard","totalMealValue":200,"totalWorkingHours":10,"totalOt":2,
             "attYear":"2026","dayOut":"2026-10-07T17:30:00","daysForAttAllowance":20,"statutoryHolidays":1}
            """;
        Attendance a = mapper.readValue(json, Attendance.class);

        assertEquals("2026", a.getAttYear());
        assertEquals(LocalDate.of(2026, 10, 7), a.getDayIn());
        assertEquals(LocalTime.of(7, 30), a.getTimeIn());
        assertEquals(LocalDateTime.of(2026, 10, 7, 17, 30), a.getDayOut());
        assertEquals(20.0, a.getDaysForAttAllowance());
        assertEquals("Ann Perera", a.getEmployeeName());
        assertEquals("Y", a.getOtCalAuto());
        assertEquals(1.0, a.getStatutoryHolidays());
    }
}
