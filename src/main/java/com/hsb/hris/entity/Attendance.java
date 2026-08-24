package com.hsb.hris.entity;

import com.hsb.hris.entity.id.AttendanceId;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Maps TBL_T_Attendance — the main transaction table populated either
 * row-by-row from the Attendance screen, or in bulk via the Excel upload.
 */
@Entity
@Table(name = "TBL_T_Attendance")
@IdClass(AttendanceId.class)
public class Attendance {

    @Id
    @Column(name = "Attt_Year", length = 4, nullable = false)
    private String attYear;

    @Id
    @Column(name = "Att_Month", length = 2, nullable = false)
    private String attMonth;

    @Id
    @Column(name = "EPF_No", length = 10, nullable = false)
    private String epfNo;

    @Id
    @Column(name = "Day_in", nullable = false)
    private LocalDate dayIn;

    @Column(name = "Plant_Code", length = 10, nullable = false)
    private String plantCode;

    @Column(name = "Working_Days")
    private Double workingDays;

    @Column(name = "OT_Cal_Auto", length = 1)
    private String otCalAuto;

    @Column(name = "No_Of_Staff")
    private Double noOfStaff;

    @Column(name = "Att_Allowance", length = 1)
    private String attAllowance;

    @Column(name = "Das_for_Att_Alowance")
    private Double daysForAttAllowance;

    @Column(name = "Poya_Saturday_normal", length = 1)
    private String poyaSaturdayNormal;

    @Column(name = "Basic_Salary")
    private Double basicSalary;

    @Column(name = "Day_Allowance")
    private Double dayAllowance;

    @Column(name = "Night_Allowance")
    private Double nightAllowance;

    @Column(name = "Time_IN", nullable = false)
    private LocalTime timeIn;

    @Column(name = "Day_out")
    private LocalDateTime dayOut;

    @Column(name = "Time_Out")
    private LocalTime timeOut;

    @Column(name = "Half_Day")
    private Double halfDay;

    @Column(name = "Total_Working_Hours")
    private Double totalWorkingHours;

    @Column(name = "Total_OT")
    private Double totalOt;

    @Column(name = "Normal_Day", length = 1)
    private String normalDay;

    @Column(name = "Saturday_Poya", length = 1)
    private String saturdayPoya;

    @Column(name = "Special_Day", length = 1)
    private String specialDay;

    @Column(name = "Late_Allow_No", length = 11)
    private String lateAllowNo;

    @Column(name = "Day_shift", length = 1)
    private String dayShift;

    @Column(name = "second_Shift", length = 1)
    private String secondShift;

    @Column(name = "Night_Shift", length = 1)
    private String nightShift;

    @Column(name = "Full_Night", length = 1)
    private String fullNight;

    @Column(name = "No_of_Meal")
    private Double noOfMeal;

    @Column(name = "Total_Meal_Value")
    private Double totalMealValue;

    @Column(name = "Statutory_holidays")
    private Double statutoryHolidays;

    @Column(name = "Sunday_Poya_Extra")
    private Double sundayPoyaExtra;

    @Column(name = "Business_Center", length = 100, nullable = false)
    private String businessCenter;

    // --- getters and setters ---

    public String getAttYear() { return attYear; }
    public void setAttYear(String attYear) { this.attYear = attYear; }
    public String getAttMonth() { return attMonth; }
    public void setAttMonth(String attMonth) { this.attMonth = attMonth; }
    public String getEpfNo() { return epfNo; }
    public void setEpfNo(String epfNo) { this.epfNo = epfNo; }
    public LocalDate getDayIn() { return dayIn; }
    public void setDayIn(LocalDate dayIn) { this.dayIn = dayIn; }
    public String getPlantCode() { return plantCode; }
    public void setPlantCode(String plantCode) { this.plantCode = plantCode; }
    public Double getWorkingDays() { return workingDays; }
    public void setWorkingDays(Double workingDays) { this.workingDays = workingDays; }
    public String getOtCalAuto() { return otCalAuto; }
    public void setOtCalAuto(String otCalAuto) { this.otCalAuto = otCalAuto; }
    public Double getNoOfStaff() { return noOfStaff; }
    public void setNoOfStaff(Double noOfStaff) { this.noOfStaff = noOfStaff; }
    public String getAttAllowance() { return attAllowance; }
    public void setAttAllowance(String attAllowance) { this.attAllowance = attAllowance; }
    public Double getDaysForAttAllowance() { return daysForAttAllowance; }
    public void setDaysForAttAllowance(Double daysForAttAllowance) { this.daysForAttAllowance = daysForAttAllowance; }
    public String getPoyaSaturdayNormal() { return poyaSaturdayNormal; }
    public void setPoyaSaturdayNormal(String poyaSaturdayNormal) { this.poyaSaturdayNormal = poyaSaturdayNormal; }
    public Double getBasicSalary() { return basicSalary; }
    public void setBasicSalary(Double basicSalary) { this.basicSalary = basicSalary; }
    public Double getDayAllowance() { return dayAllowance; }
    public void setDayAllowance(Double dayAllowance) { this.dayAllowance = dayAllowance; }
    public Double getNightAllowance() { return nightAllowance; }
    public void setNightAllowance(Double nightAllowance) { this.nightAllowance = nightAllowance; }
    public LocalTime getTimeIn() { return timeIn; }
    public void setTimeIn(LocalTime timeIn) { this.timeIn = timeIn; }
    public LocalDateTime getDayOut() { return dayOut; }
    public void setDayOut(LocalDateTime dayOut) { this.dayOut = dayOut; }
    public LocalTime getTimeOut() { return timeOut; }
    public void setTimeOut(LocalTime timeOut) { this.timeOut = timeOut; }
    public Double getHalfDay() { return halfDay; }
    public void setHalfDay(Double halfDay) { this.halfDay = halfDay; }
    public Double getTotalWorkingHours() { return totalWorkingHours; }
    public void setTotalWorkingHours(Double totalWorkingHours) { this.totalWorkingHours = totalWorkingHours; }
    public Double getTotalOt() { return totalOt; }
    public void setTotalOt(Double totalOt) { this.totalOt = totalOt; }
    public String getNormalDay() { return normalDay; }
    public void setNormalDay(String normalDay) { this.normalDay = normalDay; }
    public String getSaturdayPoya() { return saturdayPoya; }
    public void setSaturdayPoya(String saturdayPoya) { this.saturdayPoya = saturdayPoya; }
    public String getSpecialDay() { return specialDay; }
    public void setSpecialDay(String specialDay) { this.specialDay = specialDay; }
    public String getLateAllowNo() { return lateAllowNo; }
    public void setLateAllowNo(String lateAllowNo) { this.lateAllowNo = lateAllowNo; }
    public String getDayShift() { return dayShift; }
    public void setDayShift(String dayShift) { this.dayShift = dayShift; }
    public String getSecondShift() { return secondShift; }
    public void setSecondShift(String secondShift) { this.secondShift = secondShift; }
    public String getNightShift() { return nightShift; }
    public void setNightShift(String nightShift) { this.nightShift = nightShift; }
    public String getFullNight() { return fullNight; }
    public void setFullNight(String fullNight) { this.fullNight = fullNight; }
    public Double getNoOfMeal() { return noOfMeal; }
    public void setNoOfMeal(Double noOfMeal) { this.noOfMeal = noOfMeal; }
    public Double getTotalMealValue() { return totalMealValue; }
    public void setTotalMealValue(Double totalMealValue) { this.totalMealValue = totalMealValue; }
    public Double getStatutoryHolidays() { return statutoryHolidays; }
    public void setStatutoryHolidays(Double statutoryHolidays) { this.statutoryHolidays = statutoryHolidays; }
    public Double getSundayPoyaExtra() { return sundayPoyaExtra; }
    public void setSundayPoyaExtra(Double sundayPoyaExtra) { this.sundayPoyaExtra = sundayPoyaExtra; }
    public String getBusinessCenter() { return businessCenter; }
    public void setBusinessCenter(String businessCenter) { this.businessCenter = businessCenter; }
}
