package com.hsb.hris.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "TBL_M_Leave_type")
public class LeaveType {

    @Id
    @Column(name = "Leave_Type", length = 3, nullable = false)
    private String leaveType;

    @Column(name = "Leave_Name", length = 50)
    private String leaveName;

    public String getLeaveType() { return leaveType; }
    public void setLeaveType(String leaveType) { this.leaveType = leaveType; }
    public String getLeaveName() { return leaveName; }
    public void setLeaveName(String leaveName) { this.leaveName = leaveName; }
}
