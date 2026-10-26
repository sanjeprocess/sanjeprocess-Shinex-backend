package com.hsb.hris.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "TBL_Deduction_Transaction")
public class DeductionTransaction {

    @Id
    @Column(name = "id", length = 50, nullable = false)
    private String id;

    @Column(name = "EPF_No", length = 10, nullable = false)
    private String epfNo;

    @Column(name = "Employee_Name", length = 200)
    private String employeeName;

    @Column(name = "Did_Code", length = 10, nullable = false)
    private String didCode;

    @Column(name = "Deduction_Name", length = 100)
    private String deductionName;

    @Column(name = "Is_Loan")
    private Boolean loan;

    @Column(name = "Is_Uniform")
    private Boolean uniform;

    @Column(name = "Business_Center", length = 100)
    private String businessCenter;

    @Column(name = "Start_Date", nullable = false)
    private LocalDate startDate;

    @Column(name = "End_Date", nullable = false)
    private LocalDate endDate;

    @Column(name = "Total_Amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "Monthly_Cut", precision = 18, scale = 2, nullable = false)
    private BigDecimal monthlyCut;

    @Column(name = "Status", length = 20, nullable = false)
    private String status;

    @Column(name = "Created_At")
    private LocalDateTime createdAt;

    @Column(name = "Notes", length = 500)
    private String notes;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getEpfNo() { return epfNo; }
    public void setEpfNo(String epfNo) { this.epfNo = epfNo; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getDidCode() { return didCode; }
    public void setDidCode(String didCode) { this.didCode = didCode; }
    public String getDeductionName() { return deductionName; }
    public void setDeductionName(String deductionName) { this.deductionName = deductionName; }
    public Boolean getLoan() { return loan; }
    public void setLoan(Boolean loan) { this.loan = loan; }
    public Boolean getUniform() { return uniform; }
    public void setUniform(Boolean uniform) { this.uniform = uniform; }
    public String getBusinessCenter() { return businessCenter; }
    public void setBusinessCenter(String businessCenter) { this.businessCenter = businessCenter; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public BigDecimal getMonthlyCut() { return monthlyCut; }
    public void setMonthlyCut(BigDecimal monthlyCut) { this.monthlyCut = monthlyCut; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
