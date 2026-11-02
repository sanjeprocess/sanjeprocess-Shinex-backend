package com.hsb.hris.entity;

import com.hsb.hris.entity.id.StatutoryHolidayStatusId;
import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Manual Working / Not Working selection for one employee on one statutory holiday.
 * When no row exists, the status is derived from attendance.
 */
@Entity
@Table(name = "TBL_Statutory_Holiday_Emp")
@IdClass(StatutoryHolidayStatusId.class)
public class StatutoryHolidayStatus {

    @Id
    @Column(name = "Holiday_Date", nullable = false)
    private LocalDate holidayDate;

    @Id
    @Column(name = "EPF_No", length = 10, nullable = false)
    private String epfNo;

    // 'W' = Working, 'N' = Not Working
    @Column(name = "Status", length = 1, nullable = false)
    private String status;

    public LocalDate getHolidayDate() { return holidayDate; }
    public void setHolidayDate(LocalDate holidayDate) { this.holidayDate = holidayDate; }
    public String getEpfNo() { return epfNo; }
    public void setEpfNo(String epfNo) { this.epfNo = epfNo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
