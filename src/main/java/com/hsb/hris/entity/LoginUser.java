package com.hsb.hris.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "TBL_Loging_User")
public class LoginUser {

    @Id
    @Column(name = "Login_Name", length = 10, nullable = false)
    private String loginName;

    @Column(name = "Password", length = 10, nullable = false)
    private String password;

    @Column(name = "Client_Busness_Code", length = 50, nullable = false)
    private String clientBusinessCode;

    public String getLoginName() { return loginName; }
    public void setLoginName(String loginName) { this.loginName = loginName; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getClientBusinessCode() { return clientBusinessCode; }
    public void setClientBusinessCode(String clientBusinessCode) { this.clientBusinessCode = clientBusinessCode; }
}
