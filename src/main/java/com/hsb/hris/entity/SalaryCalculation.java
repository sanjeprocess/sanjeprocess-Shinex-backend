package com.hsb.hris.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "TBL_Salary_Calc")
public class SalaryCalculation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "EPF_No", length = 10, nullable = false)
    private String epfNo;

    @Column(name = "Employee_Name", length = 100)
    private String employeeName;

    @Column(name = "Plant", length = 50, nullable = false)
    private String plant;

    @Column(name = "Year", length = 4, nullable = false)
    private String year;

    @Column(name = "Month", length = 2, nullable = false)
    private String month;

    @Column(name = "Days_Worked")
    private Double daysWorked;

    @Column(name = "OT_Hours")
    private Double otHours;

    @Column(name = "Basic_Salary")
    private Double basicSalary;

    @Column(name = "Gross_Salary")
    private Double grossSalary;

    @Column(name = "EPF_8")
    private Double epf8;

    @Column(name = "EPF_12")
    private Double epf12;

    @Column(name = "ETF_3")
    private Double etf3;

    @Column(name = "Advance_Deduction")
    private Double advanceDeduction;

    @Column(name = "Telephone_Deduction")
    private Double telephoneDeduction;

    @Column(name = "Meal_Deduction")
    private Double mealDeduction;

    @Column(name = "Loan_Deduction")
    private Double loanDeduction;

    @Column(name = "Other_Deductions")
    private Double otherDeductions;

    @Column(name = "Total_Deductions")
    private Double totalDeductions;

    @Column(name = "Net_Salary")
    private Double netSalary;

    @Column(name = "Calculated_At")
    private LocalDateTime calculatedAt;

    public SalaryCalculation() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEpfNo() { return epfNo; }
    public void setEpfNo(String epfNo) { this.epfNo = epfNo; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getPlant() { return plant; }
    public void setPlant(String plant) { this.plant = plant; }

    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }

    public Double getDaysWorked() { return daysWorked; }
    public void setDaysWorked(Double daysWorked) { this.daysWorked = daysWorked; }

    public Double getOtHours() { return otHours; }
    public void setOtHours(Double otHours) { this.otHours = otHours; }

    public Double getBasicSalary() { return basicSalary; }
    public void setBasicSalary(Double basicSalary) { this.basicSalary = basicSalary; }

    public Double getGrossSalary() { return grossSalary; }
    public void setGrossSalary(Double grossSalary) { this.grossSalary = grossSalary; }

    public Double getEpf8() { return epf8; }
    public void setEpf8(Double epf8) { this.epf8 = epf8; }

    public Double getEpf12() { return epf12; }
    public void setEpf12(Double epf12) { this.epf12 = epf12; }

    public Double getEtf3() { return etf3; }
    public void setEtf3(Double etf3) { this.etf3 = etf3; }

    public Double getAdvanceDeduction() { return advanceDeduction; }
    public void setAdvanceDeduction(Double advanceDeduction) { this.advanceDeduction = advanceDeduction; }

    public Double getTelephoneDeduction() { return telephoneDeduction; }
    public void setTelephoneDeduction(Double telephoneDeduction) { this.telephoneDeduction = telephoneDeduction; }

    public Double getMealDeduction() { return mealDeduction; }
    public void setMealDeduction(Double mealDeduction) { this.mealDeduction = mealDeduction; }

    public Double getLoanDeduction() { return loanDeduction; }
    public void setLoanDeduction(Double loanDeduction) { this.loanDeduction = loanDeduction; }

    public Double getOtherDeductions() { return otherDeductions; }
    public void setOtherDeductions(Double otherDeductions) { this.otherDeductions = otherDeductions; }

    public Double getTotalDeductions() { return totalDeductions; }
    public void setTotalDeductions(Double totalDeductions) { this.totalDeductions = totalDeductions; }

    public Double getNetSalary() { return netSalary; }
    public void setNetSalary(Double netSalary) { this.netSalary = netSalary; }

    public LocalDateTime getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(LocalDateTime calculatedAt) { this.calculatedAt = calculatedAt; }
}
