package com.hsb.hris.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "TBL_M_Company")
public class BusinessCenter {

    @Id
    @Column(name = "Company_ID", length = 10, nullable = false)
    private String companyId;

    @Column(name = "Company_Name", length = 50)
    private String companyName;

    @Column(name = "Company_Address", length = 150)
    private String companyAddress;

    @Column(name = "Tel_No", length = 10)
    private String telNo;

    @Column(name = "Email_ID", length = 50)
    private String emailId;

    @Column(name = "Web_Addres", length = 50)
    private String webAddress;

    @Column(name = "EPF_Reg", length = 15)
    private String epfReg;

    @Column(name = "VAR_Reg", length = 15)
    private String vatReg;

    @Column(name = "BR_No", length = 15)
    private String brNo;

    @Column(name = "Fax_No", length = 15)
    private String faxNo;

    public String getCompanyId() { return companyId; }
    public void setCompanyId(String companyId) { this.companyId = companyId; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getCompanyAddress() { return companyAddress; }
    public void setCompanyAddress(String companyAddress) { this.companyAddress = companyAddress; }
    public String getTelNo() { return telNo; }
    public void setTelNo(String telNo) { this.telNo = telNo; }
    public String getEmailId() { return emailId; }
    public void setEmailId(String emailId) { this.emailId = emailId; }
    public String getWebAddress() { return webAddress; }
    public void setWebAddress(String webAddress) { this.webAddress = webAddress; }
    public String getEpfReg() { return epfReg; }
    public void setEpfReg(String epfReg) { this.epfReg = epfReg; }
    public String getVatReg() { return vatReg; }
    public void setVatReg(String vatReg) { this.vatReg = vatReg; }
    public String getBrNo() { return brNo; }
    public void setBrNo(String brNo) { this.brNo = brNo; }
    public String getFaxNo() { return faxNo; }
    public void setFaxNo(String faxNo) { this.faxNo = faxNo; }
}
