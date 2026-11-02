package com.hsb.hris.entity.id;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class StatutoryHolidayStatusId implements Serializable {
    private LocalDate holidayDate;
    private String epfNo;

    public StatutoryHolidayStatusId() {}
    public StatutoryHolidayStatusId(LocalDate holidayDate, String epfNo) {
        this.holidayDate = holidayDate;
        this.epfNo = epfNo;
    }

    public LocalDate getHolidayDate() { return holidayDate; }
    public void setHolidayDate(LocalDate holidayDate) { this.holidayDate = holidayDate; }
    public String getEpfNo() { return epfNo; }
    public void setEpfNo(String epfNo) { this.epfNo = epfNo; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StatutoryHolidayStatusId)) return false;
        StatutoryHolidayStatusId that = (StatutoryHolidayStatusId) o;
        return Objects.equals(holidayDate, that.holidayDate) && Objects.equals(epfNo, that.epfNo);
    }

    @Override
    public int hashCode() { return Objects.hash(holidayDate, epfNo); }
}
