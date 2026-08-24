package com.hsb.hris.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "TBL_Addition_Master")
public class AdditionMaster {

    @Id
    @Column(name = "Addition_Code", length = 10, nullable = false)
    private String additionCode;

    @Column(name = "Addition_Name", length = 50)
    private String additionName;

    @Column(name = "Addition_Value")
    private Double additionValue;

    @Column(name = "Add_EPF", length = 1)
    private String addToEpf;

    @Column(name = "Add_to_Basic", length = 1)
    private String addToBasic;

    @Column(name = "Add_Other", length = 1)
    private String addOther;

    public String getAdditionCode() { return additionCode; }
    public void setAdditionCode(String additionCode) { this.additionCode = additionCode; }
    public String getAdditionName() { return additionName; }
    public void setAdditionName(String additionName) { this.additionName = additionName; }
    public Double getAdditionValue() { return additionValue; }
    public void setAdditionValue(Double additionValue) { this.additionValue = additionValue; }
    public String getAddToEpf() { return addToEpf; }
    public void setAddToEpf(String addToEpf) { this.addToEpf = addToEpf; }
    public String getAddToBasic() { return addToBasic; }
    public void setAddToBasic(String addToBasic) { this.addToBasic = addToBasic; }
    public String getAddOther() { return addOther; }
    public void setAddOther(String addOther) { this.addOther = addOther; }
}
