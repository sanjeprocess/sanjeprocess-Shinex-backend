package com.hsb.hris.entity.id;

import java.io.Serializable;
import java.util.Objects;

public class TAdditionId implements Serializable {
    private String epfNo;
    private String addCode;

    public TAdditionId() {}
    public TAdditionId(String epfNo, String addCode) {
        this.epfNo = epfNo;
        this.addCode = addCode;
    }

    public String getEpfNo() { return epfNo; }
    public void setEpfNo(String epfNo) { this.epfNo = epfNo; }
    public String getAddCode() { return addCode; }
    public void setAddCode(String addCode) { this.addCode = addCode; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TAdditionId)) return false;
        TAdditionId that = (TAdditionId) o;
        return Objects.equals(epfNo, that.epfNo) && Objects.equals(addCode, that.addCode);
    }

    @Override
    public int hashCode() { return Objects.hash(epfNo, addCode); }
}
