package com.hsb.hris.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "TBL_M_Section")
public class Section {

    @Id
    @Column(name = "Section_Code", length = 3, nullable = false)
    private String sectionCode;

    @Column(name = "Section_Name", length = 25)
    private String sectionName;

    public String getSectionCode() { return sectionCode; }
    public void setSectionCode(String sectionCode) { this.sectionCode = sectionCode; }
    public String getSectionName() { return sectionName; }
    public void setSectionName(String sectionName) { this.sectionName = sectionName; }
}
