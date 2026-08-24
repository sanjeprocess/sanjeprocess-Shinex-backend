package com.hsb.hris.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "TBL_Dudction_Master")
public class DeductionMaster {

    @Id
    @Column(name = "Dud_Code", length = 10, nullable = false)
    private String dudCode;

    @Column(name = "Dud_Name", length = 50)
    private String dudName;

    @Column(name = "Dud_Amount")
    private Double dudAmount;

    @Column(name = "Dud_If_Lone", length = 1)
    private String ifLoan;

    @Column(name = "If_Uniform", length = 1)
    private String ifUniform;

    @Column(name = "Add_Other", length = 1)
    private String other;

    public String getDudCode() { return dudCode; }
    public void setDudCode(String dudCode) { this.dudCode = dudCode; }
    public String getDudName() { return dudName; }
    public void setDudName(String dudName) { this.dudName = dudName; }
    public Double getDudAmount() { return dudAmount; }
    public void setDudAmount(Double dudAmount) { this.dudAmount = dudAmount; }
    public String getIfLoan() { return ifLoan; }
    public void setIfLoan(String ifLoan) { this.ifLoan = ifLoan; }
    public String getIfUniform() { return ifUniform; }
    public void setIfUniform(String ifUniform) { this.ifUniform = ifUniform; }
    public String getOther() { return other; }
    public void setOther(String other) { this.other = other; }
}
