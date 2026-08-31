package com.hsb.hris.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "TBL_M_Company")
public class BusinessCenter {

    @Id
    @Column(name = "Company_ID", length = 10, nullable = false)
    @JsonProperty("companyId")
    private String companyId;

    @Column(name = "Company_Name", length = 50)
    @JsonProperty("companyName")
    private String companyName;

    @Column(name = "Company_Address", length = 150)
    @JsonProperty("companyAddress")
    private String companyAddress;

    @Column(name = "Tel_No", length = 10)
    @JsonProperty("telNo")
    private String telNo;

    @Column(name = "Email_ID", length = 50)
    @JsonProperty("emailId")
    private String emailId;

    @Column(name = "Web_Addres", length = 50)
    @JsonProperty("webAddress")
    private String webAddress;

    @Column(name = "EPF_Reg", length = 15)
    private String epfReg;

    @Column(name = "VAR_Reg", length = 15)
    private String vatReg;

    @Column(name = "BR_No", length = 15)
    private String brNo;

    @Column(name = "Fax_No", length = 15)
    @JsonProperty("faxNo")
    private String faxNo;

    public String getCompanyId() { return companyId; }
    public void setCompanyId(String companyId) { this.companyId = companyId; }

    @JsonProperty("code")
    public String getCode() { return companyId; }
    @JsonProperty("code")
    public void setCode(String code) { this.companyId = code; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    @JsonProperty("name")
    public String getName() { return companyName; }
    @JsonProperty("name")
    public void setName(String name) { this.companyName = name; }

    public String getCompanyAddress() { return companyAddress; }
    public void setCompanyAddress(String companyAddress) { this.companyAddress = companyAddress; }

    @JsonProperty("address")
    public String getAddress() { return companyAddress; }
    @JsonProperty("address")
    public void setAddress(String address) { this.companyAddress = address; }

    public String getTelNo() { return telNo; }
    public void setTelNo(String telNo) { this.telNo = telNo; }

    @JsonProperty("tel")
    public String getTel() { return telNo; }
    @JsonProperty("tel")
    public void setTel(String tel) { this.telNo = tel; }

    public String getEmailId() { return emailId; }
    public void setEmailId(String emailId) { this.emailId = emailId; }

    @JsonProperty("email")
    public String getEmail() { return emailId; }
    @JsonProperty("email")
    public void setEmail(String email) { this.emailId = email; }

    public String getWebAddress() { return webAddress; }
    public void setWebAddress(String webAddress) { this.webAddress = webAddress; }

    @JsonProperty("web")
    public String getWeb() { return webAddress; }
    @JsonProperty("web")
    public void setWeb(String web) { this.webAddress = web; }

    public String getEpfReg() { return epfReg; }
    public void setEpfReg(String epfReg) { this.epfReg = epfReg; }
    public String getVatReg() { return vatReg; }
    public void setVatReg(String vatReg) { this.vatReg = vatReg; }
    public String getBrNo() { return brNo; }
    public void setBrNo(String brNo) { this.brNo = brNo; }

    public String getFaxNo() { return faxNo; }
    public void setFaxNo(String faxNo) { this.faxNo = faxNo; }

    @JsonProperty("fax")
    public String getFax() { return faxNo; }
    @JsonProperty("fax")
    public void setFax(String fax) { this.faxNo = fax; }
}
