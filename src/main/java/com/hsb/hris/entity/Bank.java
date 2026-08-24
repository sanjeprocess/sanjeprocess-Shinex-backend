package com.hsb.hris.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "TBL_M_Bank")
public class Bank {

    @Id
    @Column(name = "Bank_ID", length = 3, nullable = false)
    private String bankId;

    @Column(name = "Bank_Name", length = 50)
    private String bankName;

    public String getBankId() { return bankId; }
    public void setBankId(String bankId) { this.bankId = bankId; }
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
}
