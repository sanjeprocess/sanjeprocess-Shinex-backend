package com.hsb.hris.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "TBL_M_Rate_ID")
public class RateId {

    @Id
    @Column(name = "Rate_ID", length = 3, nullable = false)
    private String rateId;

    @Column(name = "Rate_Name", length = 50)
    private String rateName;

    @Column(name = "Rate_Value")
    private Double rateValue;

    public String getRateId() { return rateId; }
    public void setRateId(String rateId) { this.rateId = rateId; }
    public String getRateName() { return rateName; }
    public void setRateName(String rateName) { this.rateName = rateName; }
    public Double getRateValue() { return rateValue; }
    public void setRateValue(Double rateValue) { this.rateValue = rateValue; }
}
