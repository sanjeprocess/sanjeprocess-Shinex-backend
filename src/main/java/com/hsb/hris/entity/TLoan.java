package com.hsb.hris.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "TBL_T_Lone")
public class TLoan {

    @Id
    @Column(name = "Lone_ID", length = 10, nullable = false)
    private String loanId;

    @Column(name = "EMP_EPF_No", length = 10, nullable = false)
    private String epfNo;

    @Column(name = "Lone_Amount")
    private Double loanAmount;

    @Column(name = "Lone_Start_Date")
    private LocalDate loanStartDate;

    @Column(name = "Lone_End_Date")
    private Double loanEndDate; // stored as float in legacy schema (likely a data-entry bug worth fixing in the new system)

    @Column(name = "Lone_Duration")
    private Double loanDuration;

    @Column(name = "Business_Unit", length = 10)
    private String businessUnit;

    public String getLoanId() { return loanId; }
    public void setLoanId(String loanId) { this.loanId = loanId; }
    public String getEpfNo() { return epfNo; }
    public void setEpfNo(String epfNo) { this.epfNo = epfNo; }
    public Double getLoanAmount() { return loanAmount; }
    public void setLoanAmount(Double loanAmount) { this.loanAmount = loanAmount; }
    public LocalDate getLoanStartDate() { return loanStartDate; }
    public void setLoanStartDate(LocalDate loanStartDate) { this.loanStartDate = loanStartDate; }
    public Double getLoanEndDate() { return loanEndDate; }
    public void setLoanEndDate(Double loanEndDate) { this.loanEndDate = loanEndDate; }
    public Double getLoanDuration() { return loanDuration; }
    public void setLoanDuration(Double loanDuration) { this.loanDuration = loanDuration; }
    public String getBusinessUnit() { return businessUnit; }
    public void setBusinessUnit(String businessUnit) { this.businessUnit = businessUnit; }
}
