package com.hsb.hris.entity.id;

import java.io.Serializable;
import java.util.Objects;

public class AttSummaryId implements Serializable {
    private String attYear;
    private String attMonth;
    private String epfNo;

    public AttSummaryId() {}
    public AttSummaryId(String attYear, String attMonth, String epfNo) {
        this.attYear = attYear;
        this.attMonth = attMonth;
        this.epfNo = epfNo;
    }

    public String getAttYear() { return attYear; }
    public void setAttYear(String attYear) { this.attYear = attYear; }
    public String getAttMonth() { return attMonth; }
    public void setAttMonth(String attMonth) { this.attMonth = attMonth; }
    public String getEpfNo() { return epfNo; }
    public void setEpfNo(String epfNo) { this.epfNo = epfNo; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AttSummaryId)) return false;
        AttSummaryId that = (AttSummaryId) o;
        return Objects.equals(attYear, that.attYear)
                && Objects.equals(attMonth, that.attMonth)
                && Objects.equals(epfNo, that.epfNo);
    }

    @Override
    public int hashCode() { return Objects.hash(attYear, attMonth, epfNo); }
}
