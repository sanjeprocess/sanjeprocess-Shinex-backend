package com.hsb.hris.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "TBL_T_Leave")
public class TLeave {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Leave_ID")
    private Integer id;

    @Column(name = "Leave_Year", length = 4)
    private String leaveYear;

    @Column(name = "Leave_Month", length = 2)
    private String leaveMonth;

    @Column(name = "Emp_No", length = 10)
    private String empNo;

    @Column(name = "Leave_type", length = 3)
    private String leaveType;

    @Column(name = "Leave_Days")
    private Double leaveDays;

    @Column(name = "Leave_Start_date")
    private LocalDate leaveStartDate;

    @Column(name = "Leave_End_date")
    private LocalDate leaveEndDate;

    @Column(name = "Business_Center", length = 100)
    private String businessCenter;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getLeaveYear() { return leaveYear == null ? null : leaveYear.trim(); }
    public void setLeaveYear(String leaveYear) { this.leaveYear = trimToLength(leaveYear, 4); }
    public String getLeaveMonth() { return leaveMonth == null ? null : leaveMonth.trim(); }
    public void setLeaveMonth(String leaveMonth) { this.leaveMonth = trimToLength(leaveMonth, 2); }
    public String getEmpNo() { return empNo == null ? null : empNo.trim(); }
    public void setEmpNo(String empNo) { this.empNo = trimToLength(empNo, 10); }
    public String getLeaveType() { return leaveType == null ? null : leaveType.trim(); }
    public void setLeaveType(String leaveType) { this.leaveType = trimToLength(leaveType, 3); }
    public Double getLeaveDays() { return leaveDays; }
    public void setLeaveDays(Double leaveDays) { this.leaveDays = leaveDays; }
    public LocalDate getLeaveStartDate() { return leaveStartDate; }
    public void setLeaveStartDate(LocalDate leaveStartDate) { this.leaveStartDate = leaveStartDate; }
    public LocalDate getLeaveEndDate() { return leaveEndDate; }
    public void setLeaveEndDate(LocalDate leaveEndDate) { this.leaveEndDate = leaveEndDate; }
    public String getBusinessCenter() { return businessCenter == null ? null : businessCenter.trim(); }
    public void setBusinessCenter(String businessCenter) { this.businessCenter = trimToLength(businessCenter, 100); }

    private String trimToLength(String value, int length) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.substring(0, Math.min(trimmed.length(), length));
    }
}
