package com.hsb.hris.service.transaction;

import com.hsb.hris.entity.Attendance;
import com.hsb.hris.entity.Employee;
import com.hsb.hris.entity.StatutoryHoliday;
import com.hsb.hris.entity.StatutoryHolidayStatus;
import com.hsb.hris.entity.id.StatutoryHolidayStatusId;
import com.hsb.hris.repository.AttendanceRepository;
import com.hsb.hris.repository.StatutoryHolidayRepository;
import com.hsb.hris.repository.StatutoryHolidayStatusRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

/**
 * Statutory holiday pay:
 *  - Admin defines each holiday date with a Working Value and a Not Working Value.
 *  - Only employees ticked "Statutory Holiday Eligible" are paid.
 *  - Per holiday an eligible employee is Working or Not Working: a manual selection wins,
 *    otherwise an attendance record on that date means Working.
 *  - Monthly pay = sum of the matching value for every holiday in the month.
 */
@Service
public class StatutoryHolidayService {

    public static final String WORKING = "W";
    public static final String NOT_WORKING = "N";

    private static final Logger log = LoggerFactory.getLogger(StatutoryHolidayService.class);

    private final StatutoryHolidayRepository holidays;
    private final StatutoryHolidayStatusRepository statuses;
    private final AttendanceRepository attendanceRepo;
    private final JdbcTemplate jdbc;

    public StatutoryHolidayService(StatutoryHolidayRepository holidays, StatutoryHolidayStatusRepository statuses,
                                   AttendanceRepository attendanceRepo, JdbcTemplate jdbc) {
        this.holidays = holidays; this.statuses = statuses; this.attendanceRepo = attendanceRepo; this.jdbc = jdbc;
    }

    /** Additive, idempotent schema setup so the feature works on databases that have not run db/changes.sql. */
    @EventListener(ApplicationReadyEvent.class)
    public void ensureSchema() {
        String[] ddl = {
            "IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID(N'dbo.TBL_Emp_Master') AND name = 'Statutory_Holiday_Eligible') " +
                "ALTER TABLE dbo.TBL_Emp_Master ADD Statutory_Holiday_Eligible BIT NULL CONSTRAINT DF_Emp_Statutory_Eligible DEFAULT 1 WITH VALUES",
            "IF OBJECT_ID(N'dbo.TBL_Statutory_Holiday', N'U') IS NULL " +
                "CREATE TABLE dbo.TBL_Statutory_Holiday (Holiday_Date DATE NOT NULL PRIMARY KEY, Holiday_Name NVARCHAR(100) NULL, " +
                "Working_Value DECIMAL(18,2) NULL, Not_Working_Value DECIMAL(18,2) NULL)",
            "IF OBJECT_ID(N'dbo.TBL_Statutory_Holiday_Emp', N'U') IS NULL " +
                "CREATE TABLE dbo.TBL_Statutory_Holiday_Emp (Holiday_Date DATE NOT NULL, EPF_No VARCHAR(10) NOT NULL, " +
                "Status CHAR(1) NOT NULL, CONSTRAINT PK_Statutory_Holiday_Emp PRIMARY KEY (Holiday_Date, EPF_No))"
        };
        for (String sql : ddl) {
            try {
                jdbc.execute(sql);
            } catch (Exception ex) {
                log.warn("Statutory holiday schema check failed: {}", ex.getMessage());
            }
        }
    }

    public List<StatutoryHoliday> holidaysForMonth(String year, String month) {
        YearMonth ym = YearMonth.of(Integer.parseInt(year.trim()), Integer.parseInt(month.trim()));
        return holidays.findByHolidayDateBetweenOrderByHolidayDateAsc(ym.atDay(1), ym.atEndOfMonth());
    }

    /** Manual selections for the month keyed by "yyyy-MM-dd|EPF". */
    public Map<String, String> overridesForMonth(String year, String month) {
        YearMonth ym = YearMonth.of(Integer.parseInt(year.trim()), Integer.parseInt(month.trim()));
        Map<String, String> map = new HashMap<>();
        for (StatutoryHolidayStatus s : statuses.findByHolidayDateBetween(ym.atDay(1), ym.atEndOfMonth())) {
            map.put(key(s.getHolidayDate(), s.getEpfNo()), s.getStatus());
        }
        return map;
    }

    public static boolean isEligible(Employee employee) {
        return employee != null && !Boolean.FALSE.equals(employee.getStatutoryHolidayEligible());
    }

    /** Statutory holiday lines and total for one employee in one month. */
    public EmployeePay computeForEmployee(Employee employee, String year, String month, List<Attendance> monthAttendance) {
        return computeForEmployee(employee, holidaysForMonth(year, month), overridesForMonth(year, month), monthAttendance);
    }

    public EmployeePay computeForEmployee(Employee employee, List<StatutoryHoliday> monthHolidays,
                                          Map<String, String> overrides, List<Attendance> monthAttendance) {
        String epf = employee.getEpfNo() == null ? "" : employee.getEpfNo().trim();
        boolean eligible = isEligible(employee);

        Set<LocalDate> attendedDates = new HashSet<>();
        if (monthAttendance != null) {
            for (Attendance a : monthAttendance) {
                if (a.getDayIn() != null && a.getEpfNo() != null && epf.equalsIgnoreCase(a.getEpfNo().trim())) {
                    attendedDates.add(a.getDayIn());
                }
            }
        }

        EmployeePay pay = new EmployeePay();
        pay.epfNo = epf;
        pay.eligible = eligible;
        for (StatutoryHoliday h : monthHolidays) {
            HolidayLine line = new HolidayLine();
            line.date = h.getHolidayDate();
            line.holidayName = h.getHolidayName();
            String manual = overrides.get(key(h.getHolidayDate(), epf));
            if (WORKING.equals(manual) || NOT_WORKING.equals(manual)) {
                line.status = manual;
                line.source = "MANUAL";
            } else {
                line.status = attendedDates.contains(h.getHolidayDate()) ? WORKING : NOT_WORKING;
                line.source = "ATTENDANCE";
            }
            double value = WORKING.equals(line.status) ? nz(h.getWorkingValue()) : nz(h.getNotWorkingValue());
            line.amount = eligible ? round(value) : 0.0;
            pay.lines.add(line);
            pay.total += line.amount;
        }
        pay.total = round(pay.total);
        pay.count = eligible ? monthHolidays.size() : 0;
        return pay;
    }

    public StatutoryHoliday saveHoliday(StatutoryHoliday holiday) {
        if (holiday.getHolidayName() != null) {
            String name = holiday.getHolidayName().trim();
            holiday.setHolidayName(name.substring(0, Math.min(100, name.length())));
        }
        holiday.setWorkingValue(round(Math.max(0, nz(holiday.getWorkingValue()))));
        holiday.setNotWorkingValue(round(Math.max(0, nz(holiday.getNotWorkingValue()))));
        return holidays.save(holiday);
    }

    public boolean deleteHoliday(LocalDate date) {
        if (!holidays.existsById(date)) return false;
        statuses.deleteByHolidayDate(date);
        holidays.deleteById(date);
        return true;
    }

    /** status = "W" / "N" stores a manual selection; anything else reverts to the attendance-based status. */
    public void setStatus(LocalDate date, String epfNo, String status) {
        String epf = epfNo.trim();
        StatutoryHolidayStatusId id = new StatutoryHolidayStatusId(date, epf);
        if (WORKING.equals(status) || NOT_WORKING.equals(status)) {
            StatutoryHolidayStatus s = new StatutoryHolidayStatus();
            s.setHolidayDate(date);
            s.setEpfNo(epf);
            s.setStatus(status);
            statuses.save(s);
        } else if (statuses.existsById(id)) {
            statuses.deleteById(id);
        }
    }

    public boolean holidayExists(LocalDate date) {
        return holidays.existsById(date);
    }

    public List<Attendance> attendanceForMonth(String epf, String year, String month) {
        return attendanceRepo.findByEpfNoAndAttYearAndAttMonth(epf, year, month);
    }

    private static String key(LocalDate date, String epf) {
        return date + "|" + (epf == null ? "" : epf.trim().toUpperCase());
    }

    private static double nz(Double v) { return v == null ? 0.0 : v; }

    private static double round(double v) { return Math.round(v * 100.0) / 100.0; }

    public static class HolidayLine {
        public LocalDate date;
        public String holidayName;
        public String status;
        public String source;
        public double amount;
    }

    public static class EmployeePay {
        public String epfNo;
        public boolean eligible;
        public int count;
        public double total;
        public List<HolidayLine> lines = new ArrayList<>();
    }
}
