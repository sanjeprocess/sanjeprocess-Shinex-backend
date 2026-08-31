package com.hsb.hris.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "TBL_T_Lone")
public class TLoan {

    @Id
    @Column(name = "Lone_ID", length = 10, nullable = false)
    @JsonProperty("loanId")
    private String loanId;

    @Column(name = "EMP_EPF_No", length = 10, nullable = false)
    @JsonProperty("epfNo")
    private String epfNo;

    @Column(name = "Lone_Amount")
    @JsonProperty("loanAmount")
    private Double loanAmount;

    @Column(name = "Lone_Start_Date")
    @JsonProperty("loanStartDate")
    private LocalDate loanStartDate;

    @Column(name = "Lone_End_Date")
    @JsonProperty("loanEndDate")
    private Double loanEndDate;

    @Column(name = "Lone_Duration")
    @JsonProperty("loanDuration")
    private Double loanDuration;

    @Column(name = "Business_Unit", length = 10)
    @JsonProperty("businessUnit")
    private String businessUnit;

    public String getLoanId() { return loanId; }
    public void setLoanId(String loanId) { this.loanId = loanId; }

    @JsonProperty("id")
    public String getId() { return loanId; }
    @JsonProperty("id")
    public void setId(String id) { this.loanId = id; }

    @JsonProperty("loneId")
    public String getLoneId() { return loanId; }
    @JsonProperty("loneId")
    public void setLoneId(String loneId) { this.loanId = loneId; }

    public String getEpfNo() { return epfNo; }
    public void setEpfNo(String epfNo) { this.epfNo = epfNo; }

    @JsonProperty("empEpfNo")
    public String getEmpEpfNo() { return epfNo; }
    @JsonProperty("empEpfNo")
    public void setEmpEpfNo(String empEpfNo) { this.epfNo = empEpfNo; }

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

    @JsonProperty("businessCenter")
    public String getBusinessCenter() { return businessUnit; }
    @JsonProperty("businessCenter")
    public void setBusinessCenter(String businessCenter) { this.businessUnit = businessCenter; }
}
