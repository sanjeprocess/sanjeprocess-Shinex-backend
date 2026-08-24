package com.hsb.hris.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "Tbl_M_B_Card")
public class BCard {

    @Id
    @Column(name = "EPF_No", length = 10, nullable = false)
    private String epfNo;

    @Column(name = "B_Fill", length = 3)
    private String bFill;

    @Column(name = "B_Fill_Date")
    private LocalDate bFillDate;

    @Column(name = "B_Regiistered", length = 3)
    private String bRegistered;

    @Column(name = "B_Reg_Date")
    private LocalDate bRegDate;

    @Column(name = "B_Sign", length = 3)
    private String bSign;

    @Column(name = "B_Sign_Date")
    private LocalDate bSignDate;

    @Column(name = "B_Issue", length = 3)
    private String bIssue;

    @Column(name = "B_Issue_date")
    private LocalDate bIssueDate;

    @Column(name = "Business_Center", length = 50)
    private String businessCenter;

    public String getEpfNo() { return epfNo; }
    public void setEpfNo(String epfNo) { this.epfNo = epfNo; }
    public String getbFill() { return bFill; }
    public void setbFill(String bFill) { this.bFill = bFill; }
    public LocalDate getbFillDate() { return bFillDate; }
    public void setbFillDate(LocalDate bFillDate) { this.bFillDate = bFillDate; }
    public String getbRegistered() { return bRegistered; }
    public void setbRegistered(String bRegistered) { this.bRegistered = bRegistered; }
    public LocalDate getbRegDate() { return bRegDate; }
    public void setbRegDate(LocalDate bRegDate) { this.bRegDate = bRegDate; }
    public String getbSign() { return bSign; }
    public void setbSign(String bSign) { this.bSign = bSign; }
    public LocalDate getbSignDate() { return bSignDate; }
    public void setbSignDate(LocalDate bSignDate) { this.bSignDate = bSignDate; }
    public String getbIssue() { return bIssue; }
    public void setbIssue(String bIssue) { this.bIssue = bIssue; }
    public LocalDate getbIssueDate() { return bIssueDate; }
    public void setbIssueDate(LocalDate bIssueDate) { this.bIssueDate = bIssueDate; }
    public String getBusinessCenter() { return businessCenter; }
    public void setBusinessCenter(String businessCenter) { this.businessCenter = businessCenter; }
}
