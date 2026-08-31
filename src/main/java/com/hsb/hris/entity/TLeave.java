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
    public String getLeaveYear() { return leaveYear; }
    public void setLeaveYear(String leaveYear) { this.leaveYear = leaveYear; }
    public String getLeaveMonth() { return leaveMonth; }
    public void setLeaveMonth(String leaveMonth) { this.leaveMonth = leaveMonth; }
    public String getEmpNo() { return empNo; }
    public void setEmpNo(String empNo) { this.empNo = empNo; }
    public String getLeaveType() { return leaveType; }
    public void setLeaveType(String leaveType) { this.leaveType = leaveType; }
    public Double getLeaveDays() { return leaveDays; }
    public void setLeaveDays(Double leaveDays) { this.leaveDays = leaveDays; }
    public LocalDate getLeaveStartDate() { return leaveStartDate; }
    public void setLeaveStartDate(LocalDate leaveStartDate) { this.leaveStartDate = leaveStartDate; }
    public LocalDate getLeaveEndDate() { return leaveEndDate; }
    public void setLeaveEndDate(LocalDate leaveEndDate) { this.leaveEndDate = leaveEndDate; }
    public String getBusinessCenter() { return businessCenter; }
    public void setBusinessCenter(String businessCenter) { this.businessCenter = businessCenter; }
}
