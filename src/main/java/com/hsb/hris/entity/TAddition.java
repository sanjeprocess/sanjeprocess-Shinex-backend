package com.hsb.hris.entity;

import com.hsb.hris.entity.id.TAdditionId;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "TBL_T_Addition")
@IdClass(TAdditionId.class)
public class TAddition {

    @Id
    @Column(name = "EPF_No", length = 10, nullable = false)
    private String epfNo;

    @Id
    @Column(name = "Add_Code", length = 10, nullable = false)
    private String addCode;

    @Column(name = "Business_Center", length = 100)
    private String businessCenter;

    @Column(name = "Add_Amount")
    private Double addAmount;

    @Column(name = "Every_Month", length = 10)
    private String everyMonth;

    @Column(name = "Add_Month")
    private BigDecimal addMonth;

    @Column(name = "Add_Year", length = 4)
    private String addYear;

    public String getEpfNo() { return epfNo; }
    public void setEpfNo(String epfNo) { this.epfNo = epfNo; }
    public String getAddCode() { return addCode; }
    public void setAddCode(String addCode) { this.addCode = addCode; }
    public String getBusinessCenter() { return businessCenter; }
    public void setBusinessCenter(String businessCenter) { this.businessCenter = businessCenter; }
    public Double getAddAmount() { return addAmount; }
    public void setAddAmount(Double addAmount) { this.addAmount = addAmount; }
    public String getEveryMonth() { return everyMonth; }
    public void setEveryMonth(String everyMonth) { this.everyMonth = everyMonth; }
    public BigDecimal getAddMonth() { return addMonth; }
    public void setAddMonth(BigDecimal addMonth) { this.addMonth = addMonth; }
    public String getAddYear() { return addYear; }
    public void setAddYear(String addYear) { this.addYear = addYear; }
}
