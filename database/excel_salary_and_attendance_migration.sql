-- ============================================================================
-- Migration: Add Employee_Name to Attendance and create TBL_Salary_Calc
-- Project: HSB HRIS & Payroll System (Shinex)
-- ============================================================================

-- 1. Add nullable Employee_Name column to TBL_T_Attendance if not present
IF NOT EXISTS (
    SELECT 1 FROM sys.columns 
    WHERE object_id = OBJECT_ID(N'dbo.TBL_T_Attendance') 
      AND name = 'Employee_Name'
)
BEGIN
    ALTER TABLE dbo.TBL_T_Attendance ADD Employee_Name NVARCHAR(100) NULL;
    PRINT 'Added Employee_Name column to dbo.TBL_T_Attendance.';
END
ELSE
BEGIN
    PRINT 'Column Employee_Name already exists on dbo.TBL_T_Attendance.';
END;

-- 2. Create TBL_Salary_Calc table for Excel-style month & plant-wise salary calculations
IF OBJECT_ID(N'dbo.TBL_Salary_Calc', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.TBL_Salary_Calc (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        EPF_No NVARCHAR(10) NOT NULL,
        Employee_Name NVARCHAR(100) NULL,
        Plant NVARCHAR(50) NOT NULL,
        Year NVARCHAR(4) NOT NULL,
        Month NVARCHAR(2) NOT NULL,
        Days_Worked FLOAT NULL,
        OT_Hours FLOAT NULL,
        Basic_Salary DECIMAL(18,2) NULL,
        Gross_Salary DECIMAL(18,2) NULL,
        EPF_8 DECIMAL(18,2) NULL,
        EPF_12 DECIMAL(18,2) NULL,
        ETF_3 DECIMAL(18,2) NULL,
        Advance_Deduction DECIMAL(18,2) NULL,
        Telephone_Deduction DECIMAL(18,2) NULL,
        Meal_Deduction DECIMAL(18,2) NULL,
        Loan_Deduction DECIMAL(18,2) NULL,
        Other_Deductions DECIMAL(18,2) NULL,
        Total_Deductions DECIMAL(18,2) NULL,
        Net_Salary DECIMAL(18,2) NULL,
        Calculated_At DATETIME2 NULL
    );

    CREATE NONCLUSTERED INDEX IX_Salary_Calc_Year_Month_Plant 
    ON dbo.TBL_Salary_Calc (Year, Month, Plant);

    PRINT 'Created table dbo.TBL_Salary_Calc and index IX_Salary_Calc_Year_Month_Plant.';
END
ELSE
BEGIN
    PRINT 'Table dbo.TBL_Salary_Calc already exists.';
END;
