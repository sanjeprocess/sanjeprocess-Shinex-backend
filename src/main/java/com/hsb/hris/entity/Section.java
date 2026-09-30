package com.hsb.hris.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "TBL_M_Section")
public class Section {

    @Id
    @Column(name = "Section_Code", length = 6, nullable = false)
    private String sectionCode;

    @Column(name = "Section_Name", length = 50)
    private String sectionName;

    @Column(name = "Business_Center", length = 100)
    private String businessCenter;

    @Column(name = "Basic_Salary")
    private Double basicSalary;

    public String getSectionCode() { return sectionCode; }
    public void setSectionCode(String sectionCode) { this.sectionCode = sectionCode; }

    public String getSectionName() { return sectionName; }
    public void setSectionName(String sectionName) { this.sectionName = sectionName; }

    public String getBusinessCenter() { return businessCenter; }
    public void setBusinessCenter(String businessCenter) { this.businessCenter = businessCenter; }

    public Double getBasicSalary() { return basicSalary; }
    public void setBasicSalary(Double basicSalary) { this.basicSalary = basicSalary; }
}

