package com.hsb.hris.entity.id;

import java.io.Serializable;
import java.util.Objects;

public class TDeductionId implements Serializable {
    private String epfNo;
    private String didCode;

    public TDeductionId() {}
    public TDeductionId(String epfNo, String didCode) {
        this.epfNo = epfNo;
        this.didCode = didCode;
    }

    public String getEpfNo() { return epfNo; }
    public void setEpfNo(String epfNo) { this.epfNo = epfNo; }
    public String getDidCode() { return didCode; }
    public void setDidCode(String didCode) { this.didCode = didCode; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TDeductionId)) return false;
        TDeductionId that = (TDeductionId) o;
        return Objects.equals(epfNo, that.epfNo) && Objects.equals(didCode, that.didCode);
    }

    @Override
    public int hashCode() { return Objects.hash(epfNo, didCode); }
}
