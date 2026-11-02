package com.hsb.hris.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "TBL_Statutory_Holiday")
public class StatutoryHoliday {

    @Id
    @Column(name = "Holiday_Date", nullable = false)
    private LocalDate holidayDate;

    @Column(name = "Holiday_Name", length = 100)
    private String holidayName;

    // Paid to an eligible employee who works on the holiday
    @Column(name = "Working_Value")
    private Double workingValue;

    // Paid to an eligible employee who does not work on the holiday
    @Column(name = "Not_Working_Value")
    private Double notWorkingValue;

    public LocalDate getHolidayDate() { return holidayDate; }
    public void setHolidayDate(LocalDate holidayDate) { this.holidayDate = holidayDate; }
    public String getHolidayName() { return holidayName; }
    public void setHolidayName(String holidayName) { this.holidayName = holidayName; }
    public Double getWorkingValue() { return workingValue; }
    public void setWorkingValue(Double workingValue) { this.workingValue = workingValue; }
    public Double getNotWorkingValue() { return notWorkingValue; }
    public void setNotWorkingValue(Double notWorkingValue) { this.notWorkingValue = notWorkingValue; }
}
