package com.hsb.hris.entity;

import com.hsb.hris.entity.id.AttSummaryId;
import jakarta.persistence.*;

/**
 * Maps TBL_R_AttSummary — the computed monthly payroll summary report table.
 * Treat as READ-ONLY from the API (values are produced by the payroll
 * calculation process, not edited directly by users).
 */
@Entity
@Table(name = "TBL_R_AttSummary")
@IdClass(AttSummaryId.class)
public class AttendanceSummary {

    @Id
    @Column(name = "Attt_Year", length = 4, nullable = false)
    private String attYear;

    @Id
    @Column(name = "Att_Month", length = 2, nullable = false)
    private String attMonth;

    @Id
    @Column(name = "EPF_No", length = 10, nullable = false)
    private String epfNo;

    @Column(name = "Emp_Name", length = 150)
    private String empName;

    @Column(name = "Plant_Code", length = 10, nullable = false)
    private String plantCode;

    @Column(name = "Business_Center", length = 100, nullable = false)
    private String businessCenter;

    @Column(name = "Normal_Shift") private Double normalShift;
    @Column(name = "Day_Rate") private Double dayRate;
    @Column(name = "Total_Rate") private Double totalRate;
    @Column(name = "second_Shfit") private Double secondShift;
    @Column(name = "Day_rate2") private Double dayRate2;
    @Column(name = "Total_Rate2") private Double totalRate2;
    @Column(name = "Full_Night") private Double fullNight;
    @Column(name = "Night_Rate") private Double nightRate;
    @Column(name = "Night_total") private Double nightTotal;
    @Column(name = "Sun_Poya_Days") private Double sunPoyaDays;
    @Column(name = "Sun_Poya_Rate") private Double sunPoyaRate;
    @Column(name = "Sun_Poya_total") private Double sunPoyaTotal;
    @Column(name = "Statutory_Holiday") private Double statutoryHoliday;
    @Column(name = "Statu_Rate") private Double statuRate;
    @Column(name = "Statu_Total") private Double statuTotal;
    @Column(name = "OT1_Hours") private Double ot1Hours;
    @Column(name = "OT1_Rate") private Double ot1Rate;
    @Column(name = "OT1_Total") private Double ot1Total;
    @Column(name = "OT2_Hours") private Double ot2Hours;
    @Column(name = "OT2_Rate") private Double ot2Rate;
    @Column(name = "OT2_Total") private Double ot2Total;
    @Column(name = "Sunday_Poya_Extra") private Double sundayPoyaExtra;
    @Column(name = "Performance_Allow") private Double performanceAllow;
    @Column(name = "Transoprt_Allowce") private Double transportAllowance;
    @Column(name = "Other_Add") private Double otherAdd;
    @Column(name = "Att_Allowance") private Double attAllowance;
    @Column(name = "Day_Off") private Double dayOff;
    @Column(name = "Total_Addition") private Double totalAddition;
    @Column(name = "Gross_Salary") private Double grossSalary;
    @Column(name = "EPF_EMP") private Double epfEmp;
    @Column(name = "EPF_Company") private Double epfCompany;
    @Column(name = "ETF") private Double etf;
    @Column(name = "Other_Deduc") private Double otherDeduc;
    @Column(name = "Salary_Advance") private Double salaryAdvance;
    @Column(name = "Telphone") private Double telephone;
    @Column(name = "Number_of_Meal") private Double numberOfMeal;
    @Column(name = "Total_Meal_Value") private Double totalMealValue;
    @Column(name = "Loan") private Double loan;
    @Column(name = "Total_Diduction") private Double totalDeduction;
    @Column(name = "Net_Salary") private Double netSalary;

    // --- getters and setters ---
    public String getAttYear() { return attYear; }
    public void setAttYear(String attYear) { this.attYear = attYear; }
    public String getAttMonth() { return attMonth; }
    public void setAttMonth(String attMonth) { this.attMonth = attMonth; }
    public String getEpfNo() { return epfNo; }
    public void setEpfNo(String epfNo) { this.epfNo = epfNo; }
    public String getEmpName() { return empName; }
    public void setEmpName(String empName) { this.empName = empName; }
    public String getPlantCode() { return plantCode; }
    public void setPlantCode(String plantCode) { this.plantCode = plantCode; }
    public String getBusinessCenter() { return businessCenter; }
    public void setBusinessCenter(String businessCenter) { this.businessCenter = businessCenter; }
    public Double getNormalShift() { return normalShift; }
    public void setNormalShift(Double v) { this.normalShift = v; }
    public Double getDayRate() { return dayRate; }
    public void setDayRate(Double v) { this.dayRate = v; }
    public Double getTotalRate() { return totalRate; }
    public void setTotalRate(Double v) { this.totalRate = v; }
    public Double getSecondShift() { return secondShift; }
    public void setSecondShift(Double v) { this.secondShift = v; }
    public Double getDayRate2() { return dayRate2; }
    public void setDayRate2(Double v) { this.dayRate2 = v; }
    public Double getTotalRate2() { return totalRate2; }
    public void setTotalRate2(Double v) { this.totalRate2 = v; }
    public Double getFullNight() { return fullNight; }
    public void setFullNight(Double v) { this.fullNight = v; }
    public Double getNightRate() { return nightRate; }
    public void setNightRate(Double v) { this.nightRate = v; }
    public Double getNightTotal() { return nightTotal; }
    public void setNightTotal(Double v) { this.nightTotal = v; }
    public Double getSunPoyaDays() { return sunPoyaDays; }
    public void setSunPoyaDays(Double v) { this.sunPoyaDays = v; }
    public Double getSunPoyaRate() { return sunPoyaRate; }
    public void setSunPoyaRate(Double v) { this.sunPoyaRate = v; }
    public Double getSunPoyaTotal() { return sunPoyaTotal; }
    public void setSunPoyaTotal(Double v) { this.sunPoyaTotal = v; }
    public Double getStatutoryHoliday() { return statutoryHoliday; }
    public void setStatutoryHoliday(Double v) { this.statutoryHoliday = v; }
    public Double getStatuRate() { return statuRate; }
    public void setStatuRate(Double v) { this.statuRate = v; }
    public Double getStatuTotal() { return statuTotal; }
    public void setStatuTotal(Double v) { this.statuTotal = v; }
    public Double getOt1Hours() { return ot1Hours; }
    public void setOt1Hours(Double v) { this.ot1Hours = v; }
    public Double getOt1Rate() { return ot1Rate; }
    public void setOt1Rate(Double v) { this.ot1Rate = v; }
    public Double getOt1Total() { return ot1Total; }
    public void setOt1Total(Double v) { this.ot1Total = v; }
    public Double getOt2Hours() { return ot2Hours; }
    public void setOt2Hours(Double v) { this.ot2Hours = v; }
    public Double getOt2Rate() { return ot2Rate; }
    public void setOt2Rate(Double v) { this.ot2Rate = v; }
    public Double getOt2Total() { return ot2Total; }
    public void setOt2Total(Double v) { this.ot2Total = v; }
    public Double getSundayPoyaExtra() { return sundayPoyaExtra; }
    public void setSundayPoyaExtra(Double v) { this.sundayPoyaExtra = v; }
    public Double getPerformanceAllow() { return performanceAllow; }
    public void setPerformanceAllow(Double v) { this.performanceAllow = v; }
    public Double getTransportAllowance() { return transportAllowance; }
    public void setTransportAllowance(Double v) { this.transportAllowance = v; }
    public Double getOtherAdd() { return otherAdd; }
    public void setOtherAdd(Double v) { this.otherAdd = v; }
    public Double getAttAllowance() { return attAllowance; }
    public void setAttAllowance(Double v) { this.attAllowance = v; }
    public Double getDayOff() { return dayOff; }
    public void setDayOff(Double v) { this.dayOff = v; }
    public Double getTotalAddition() { return totalAddition; }
    public void setTotalAddition(Double v) { this.totalAddition = v; }
    public Double getGrossSalary() { return grossSalary; }
    public void setGrossSalary(Double v) { this.grossSalary = v; }
    public Double getEpfEmp() { return epfEmp; }
    public void setEpfEmp(Double v) { this.epfEmp = v; }
    public Double getEpfCompany() { return epfCompany; }
    public void setEpfCompany(Double v) { this.epfCompany = v; }
    public Double getEtf() { return etf; }
    public void setEtf(Double v) { this.etf = v; }
    public Double getOtherDeduc() { return otherDeduc; }
    public void setOtherDeduc(Double v) { this.otherDeduc = v; }
    public Double getSalaryAdvance() { return salaryAdvance; }
    public void setSalaryAdvance(Double v) { this.salaryAdvance = v; }
    public Double getTelephone() { return telephone; }
    public void setTelephone(Double v) { this.telephone = v; }
    public Double getNumberOfMeal() { return numberOfMeal; }
    public void setNumberOfMeal(Double v) { this.numberOfMeal = v; }
    public Double getTotalMealValue() { return totalMealValue; }
    public void setTotalMealValue(Double v) { this.totalMealValue = v; }
    public Double getLoan() { return loan; }
    public void setLoan(Double v) { this.loan = v; }
    public Double getTotalDeduction() { return totalDeduction; }
    public void setTotalDeduction(Double v) { this.totalDeduction = v; }
    public Double getNetSalary() { return netSalary; }
    public void setNetSalary(Double v) { this.netSalary = v; }
}
