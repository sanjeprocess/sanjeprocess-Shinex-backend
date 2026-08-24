package com.hsb.hris.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "TBL_M_Customer")
public class Plant {

    @Id
    @Column(name = "Cus_Code", length = 6, nullable = false)
    private String custCode;

    @Column(name = "Cus_Name", length = 100)
    private String custName;

    @Column(name = "Cus_Add1", length = 50)
    private String address1;

    @Column(name = "Cus_Add2", length = 50)
    private String address2;

    @Column(name = "Cus_Add3", length = 50)
    private String address3;

    @Column(name = "Cus_Email", length = 30)
    private String email;

    @Column(name = "Cus_Contact_No", length = 12)
    private String contactNo;

    @Column(name = "Cus_Contact_name", length = 50)
    private String contactName;

    @Column(name = "Cus_Working_das")
    private BigDecimal workingDays;

    @Column(name = "Cus_OT_Calcultion_Auto", length = 1)
    private String otCalculationAuto;

    @Column(name = "Cus_Min_Staff_Quy")
    private BigDecimal minStaffQty;

    @Column(name = "Attendence_Allowance", length = 10)
    private String attendanceAllowance;

    @Column(name = "days_to_work_for_Att_Allow")
    private BigDecimal daysToWorkForAttAllowance;

    public String getCustCode() { return custCode; }
    public void setCustCode(String custCode) { this.custCode = custCode; }
    public String getCustName() { return custName; }
    public void setCustName(String custName) { this.custName = custName; }
    public String getAddress1() { return address1; }
    public void setAddress1(String address1) { this.address1 = address1; }
    public String getAddress2() { return address2; }
    public void setAddress2(String address2) { this.address2 = address2; }
    public String getAddress3() { return address3; }
    public void setAddress3(String address3) { this.address3 = address3; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getContactNo() { return contactNo; }
    public void setContactNo(String contactNo) { this.contactNo = contactNo; }
    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    public BigDecimal getWorkingDays() { return workingDays; }
    public void setWorkingDays(BigDecimal workingDays) { this.workingDays = workingDays; }
    public String getOtCalculationAuto() { return otCalculationAuto; }
    public void setOtCalculationAuto(String otCalculationAuto) { this.otCalculationAuto = otCalculationAuto; }
    public BigDecimal getMinStaffQty() { return minStaffQty; }
    public void setMinStaffQty(BigDecimal minStaffQty) { this.minStaffQty = minStaffQty; }
    public String getAttendanceAllowance() { return attendanceAllowance; }
    public void setAttendanceAllowance(String attendanceAllowance) { this.attendanceAllowance = attendanceAllowance; }
    public BigDecimal getDaysToWorkForAttAllowance() { return daysToWorkForAttAllowance; }
    public void setDaysToWorkForAttAllowance(BigDecimal daysToWorkForAttAllowance) { this.daysToWorkForAttAllowance = daysToWorkForAttAllowance; }
}
