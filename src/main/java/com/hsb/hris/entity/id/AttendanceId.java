package com.hsb.hris.entity.id;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Composite key for TBL_T_Attendance.
 * Matches the natural key used by the legacy attendance transaction screen:
 * Year + Month + Employee + Day-In date.
 */
public class AttendanceId implements Serializable {

    private String attYear;
    private String attMonth;
    private String epfNo;
    private LocalDate dayIn;

    public AttendanceId() {}

    public AttendanceId(String attYear, String attMonth, String epfNo, LocalDate dayIn) {
        this.attYear = attYear;
        this.attMonth = attMonth;
        this.epfNo = epfNo;
        this.dayIn = dayIn;
    }

    public String getAttYear() { return attYear; }
    public void setAttYear(String attYear) { this.attYear = attYear; }
    public String getAttMonth() { return attMonth; }
    public void setAttMonth(String attMonth) { this.attMonth = attMonth; }
    public String getEpfNo() { return epfNo; }
    public void setEpfNo(String epfNo) { this.epfNo = epfNo; }
    public LocalDate getDayIn() { return dayIn; }
    public void setDayIn(LocalDate dayIn) { this.dayIn = dayIn; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AttendanceId)) return false;
        AttendanceId that = (AttendanceId) o;
        return Objects.equals(attYear, that.attYear)
                && Objects.equals(attMonth, that.attMonth)
                && Objects.equals(epfNo, that.epfNo)
                && Objects.equals(dayIn, that.dayIn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attYear, attMonth, epfNo, dayIn);
    }
}
