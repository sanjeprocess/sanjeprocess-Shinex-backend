package com.hsb.hris.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "TBL_Emp_Master")
public class Employee {

    @Id
    @Column(name = "Emp_EPF_No", length = 10, nullable = false)
    private String epfNo;

    @Column(name = "Emp_NIC_No", length = 15)
    private String nicNo;

    @Column(name = "Emp_Plant_Code", length = 10)
    private String plantCode;

    @Column(name = "Emp_Date_Of_Birth")
    private LocalDate dateOfBirth;

    @Column(name = "Emp_Name", length = 50)
    private String firstName;

    @Column(name = "Emp_Nam1", length = 50)
    private String lastName;

    @Column(name = "Emp_Address", length = 50)
    private String address;

    @Column(name = "Emp_Contact_No", length = 15)
    private String contactNo;

    @Column(name = "Emp_Mobile_No", length = 15)
    private String mobileNo;

    @Column(name = "Emp_Email_Address", length = 30)
    private String emailAddress;

    @Column(name = "Emp_Hired_Date")
    private LocalDate hiredDate;

    @Column(name = "Emp_hired_month", length = 15)
    private String hiredMonth;

    @Column(name = "Emp_Basic_Salary")
    private Double basicSalary;

    @Column(name = "Emp_Bank_Account_Number", length = 20)
    private String bankAccountNumber;

    @Column(name = "Emp_Bank_Name", length = 30)
    private String bankName;

    @Column(name = "Emp_Bank_Branch_Name", length = 25)
    private String bankBranchName;

    @Column(name = "Emp_SWIFT", length = 15)
    private String swift;

    @Column(name = "Emp_Gender", length = 8)
    private String gender;

    @Column(name = "Emp_Section_Code", length = 6)
    private String sectionCode;

    @Column(name = "Emp_Day_Allowance")
    private Double dayAllowance;

    @Column(name = "Emp_Night_Allowance")
    private Double nightAllowance;

    @Column(name = "Emp_B_Card_Yes", length = 3)
    private String bCardYes;

    @Column(name = "Emp_Enative_Yes", length = 3)
    private String enativeYes;

    @Column(name = "Emp_Deth_Denotion", length = 3)
    private String dethDenotion;

    @Column(name = "Emp_Business_Center", length = 100)
    private String businessCenter;

    @Column(name = "Sunday_Poya_Extra")
    private Double sundayPoyaExtra;

    public String getEpfNo() { return epfNo; }
    public void setEpfNo(String epfNo) { this.epfNo = epfNo; }
    public String getNicNo() { return nicNo; }
    public void setNicNo(String nicNo) { this.nicNo = nicNo; }
    public String getPlantCode() { return plantCode; }
    public void setPlantCode(String plantCode) { this.plantCode = plantCode; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getContactNo() { return contactNo; }
    public void setContactNo(String contactNo) { this.contactNo = contactNo; }
    public String getMobileNo() { return mobileNo; }
    public void setMobileNo(String mobileNo) { this.mobileNo = mobileNo; }
    public String getEmailAddress() { return emailAddress; }
    public void setEmailAddress(String emailAddress) { this.emailAddress = emailAddress; }
    public LocalDate getHiredDate() { return hiredDate; }
    public void setHiredDate(LocalDate hiredDate) { this.hiredDate = hiredDate; }
    public String getHiredMonth() { return hiredMonth; }
    public void setHiredMonth(String hiredMonth) { this.hiredMonth = hiredMonth; }
    public Double getBasicSalary() { return basicSalary; }
    public void setBasicSalary(Double basicSalary) { this.basicSalary = basicSalary; }
    public String getBankAccountNumber() { return bankAccountNumber; }
    public void setBankAccountNumber(String bankAccountNumber) { this.bankAccountNumber = bankAccountNumber; }
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    public String getBankBranchName() { return bankBranchName; }
    public void setBankBranchName(String bankBranchName) { this.bankBranchName = bankBranchName; }
    public String getSwift() { return swift; }
    public void setSwift(String swift) { this.swift = swift; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getSectionCode() { return sectionCode; }
    public void setSectionCode(String sectionCode) { this.sectionCode = sectionCode; }
    public Double getDayAllowance() { return dayAllowance; }
    public void setDayAllowance(Double dayAllowance) { this.dayAllowance = dayAllowance; }
    public Double getNightAllowance() { return nightAllowance; }
    public void setNightAllowance(Double nightAllowance) { this.nightAllowance = nightAllowance; }
    public String getbCardYes() { return bCardYes; }
    public void setbCardYes(String bCardYes) { this.bCardYes = bCardYes; }
    public String getEnativeYes() { return enativeYes; }
    public void setEnativeYes(String enativeYes) { this.enativeYes = enativeYes; }
    public String getDethDenotion() { return dethDenotion; }
    public void setDethDenotion(String dethDenotion) { this.dethDenotion = dethDenotion; }
    public String getBusinessCenter() { return businessCenter; }
    public void setBusinessCenter(String businessCenter) { this.businessCenter = businessCenter; }
    public Double getSundayPoyaExtra() { return sundayPoyaExtra; }
    public void setSundayPoyaExtra(Double sundayPoyaExtra) { this.sundayPoyaExtra = sundayPoyaExtra; }
}
