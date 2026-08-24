package com.hsb.hris.entity;

import com.hsb.hris.entity.id.TDeductionId;
import jakarta.persistence.*;

@Entity
@Table(name = "TBL_T_Diduction")
@IdClass(TDeductionId.class)
public class TDeduction {

    @Id
    @Column(name = "EPF_No", length = 10, nullable = false)
    private String epfNo;

    @Id
    @Column(name = "Did_Code", length = 10, nullable = false)
    private String didCode;

    @Column(name = "Business_Center", length = 100)
    private String businessCenter;

    @Column(name = "Did_Amount")
    private Double didAmount;

    @Column(name = "Every_Month", length = 10)
    private String everyMonth;

    @Column(name = "Add_Month", length = 2)
    private String addMonth;

    @Column(name = "Add_Year", length = 4)
    private String addYear;

    public String getEpfNo() { return epfNo; }
    public void setEpfNo(String epfNo) { this.epfNo = epfNo; }
    public String getDidCode() { return didCode; }
    public void setDidCode(String didCode) { this.didCode = didCode; }
    public String getBusinessCenter() { return businessCenter; }
    public void setBusinessCenter(String businessCenter) { this.businessCenter = businessCenter; }
    public Double getDidAmount() { return didAmount; }
    public void setDidAmount(Double didAmount) { this.didAmount = didAmount; }
    public String getEveryMonth() { return everyMonth; }
    public void setEveryMonth(String everyMonth) { this.everyMonth = everyMonth; }
    public String getAddMonth() { return addMonth; }
    public void setAddMonth(String addMonth) { this.addMonth = addMonth; }
    public String getAddYear() { return addYear; }
    public void setAddYear(String addYear) { this.addYear = addYear; }
}
