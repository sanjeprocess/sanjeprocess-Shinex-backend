package com.hsb.hris.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "TBL_M_Loan_Type")
public class LoanType {

    @Id
    @Column(name = "Loan_Type", length = 3, nullable = false)
    private String loanType;

    @Column(name = "Loan_Name", length = 50)
    private String loanName;

    public String getLoanType() { return loanType; }
    public void setLoanType(String loanType) { this.loanType = loanType; }
    public String getLoanName() { return loanName; }
    public void setLoanName(String loanName) { this.loanName = loanName; }
}
