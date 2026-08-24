package com.hsb.hris.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "TBL_M_OT_Type")
public class OtType {

    @Id
    @Column(name = "OT_Type", length = 3, nullable = false)
    private String otType;

    @Column(name = "OT_Name", length = 50)
    private String otName;

    @Column(name = "OT_Rate")
    private Double otRate;

    public String getOtType() { return otType; }
    public void setOtType(String otType) { this.otType = otType; }
    public String getOtName() { return otName; }
    public void setOtName(String otName) { this.otName = otName; }
    public Double getOtRate() { return otRate; }
    public void setOtRate(Double otRate) { this.otRate = otRate; }
}
