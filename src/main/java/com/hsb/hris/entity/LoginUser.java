package com.hsb.hris.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "TBL_Loging_User")
public class LoginUser {

    @Id
    @Column(name = "Login_Name", length = 50, nullable = false)
    private String loginName;

    @Column(name = "Password", length = 255, nullable = false)
    private String password;

    @Column(name = "Client_Busness_Code", length = 100, nullable = false)
    private String clientBusinessCode;

    @Column(name = "Full_Name", length = 100)
    private String fullName;

    @Column(name = "NIC_Number", length = 20)
    private String nicNumber;

    @Column(name = "Is_Blocked", nullable = false)
    private Boolean blocked = false;

    @Column(name = "Site_Visibility", nullable = false)
    private Boolean canViewSite = true;

    @Column(name = "Access_Level", length = 20, nullable = false)
    private String accessLevel = "READ_WRITE";

    @Column(name = "Manage_Users", nullable = false)
    private Boolean canManageUsers = false;

    @Transient
    private String role = "ADMIN";

    public String getLoginName() { return loginName; }
    public void setLoginName(String loginName) { this.loginName = loginName; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getClientBusinessCode() { return clientBusinessCode; }
    public void setClientBusinessCode(String clientBusinessCode) { this.clientBusinessCode = clientBusinessCode; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getNicNumber() { return nicNumber; }
    public void setNicNumber(String nicNumber) { this.nicNumber = nicNumber; }

    public String getRole() {
        if (loginName != null && loginName.trim().equalsIgnoreCase("superadmin")) {
            return "SUPERADMIN";
        }
        return role != null ? role : "ADMIN";
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isBlocked() { return Boolean.TRUE.equals(blocked); }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }
    public Boolean getBlocked() { return blocked; }
    public void setBlocked(String blocked) { this.blocked = "Y".equalsIgnoreCase(blocked) || "1".equals(blocked) || "true".equalsIgnoreCase(blocked); }

    public boolean isCanViewSite() { return !Boolean.FALSE.equals(canViewSite); }
    public void setCanViewSite(boolean value) { this.canViewSite = value; }
    public Boolean getCanViewSite() { return canViewSite; }
    public void setCanViewSite(String value) { this.canViewSite = !"N".equalsIgnoreCase(value) && !"0".equals(value) && !"false".equalsIgnoreCase(value); }

    public String getAccessLevel() {
        return "EDIT_ALLOWED".equalsIgnoreCase(accessLevel) ? "READ_WRITE"
                : accessLevel == null ? "READ_ONLY" : accessLevel.trim().toUpperCase();
    }
    public void setAccessLevel(String value) {
        this.accessLevel = ("READ_WRITE".equalsIgnoreCase(value) || "EDIT_ALLOWED".equalsIgnoreCase(value))
                ? "EDIT_ALLOWED" : "READ_ONLY";
    }

    public boolean isCanManageUsers() { return Boolean.TRUE.equals(canManageUsers); }
    public void setCanManageUsers(boolean value) { this.canManageUsers = value; }
    public Boolean getCanManageUsers() { return canManageUsers; }
    public void setCanManageUsers(String value) { this.canManageUsers = "Y".equalsIgnoreCase(value) || "1".equals(value) || "true".equalsIgnoreCase(value); }
}
