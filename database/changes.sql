-- =============================================================================
-- HSB HRIS - Additive Database Changes (Production Safe)
-- Run this script on the SQL Server database.
-- All changes are strictly additive: new nullable columns and new tables only.
-- No DROP, DELETE, TRUNCATE, or ALTER ... DROP operations.
-- =============================================================================

-- 1. TBL_T_Attendance: Add Section_Code, Customer_Code, Employee_Name if missing
IF NOT EXISTS (
    SELECT 1 FROM sys.columns 
    WHERE object_id = OBJECT_ID(N'dbo.TBL_T_Attendance') AND name = 'Employee_Name'
)
BEGIN
    ALTER TABLE dbo.TBL_T_Attendance ADD Employee_Name NVARCHAR(200) NULL;
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.columns 
    WHERE object_id = OBJECT_ID(N'dbo.TBL_T_Attendance') AND name = 'Section_Code'
)
BEGIN
    ALTER TABLE dbo.TBL_T_Attendance ADD Section_Code NVARCHAR(10) NULL;
END
GO

IF NOT EXISTS (
    SELECT 1 FROM sys.columns 
    WHERE object_id = OBJECT_ID(N'dbo.TBL_T_Attendance') AND name = 'Customer_Code'
)
BEGIN
    ALTER TABLE dbo.TBL_T_Attendance ADD Customer_Code NVARCHAR(50) NULL;
END
GO

-- 2. TBL_Salary_Calc: New table for Excel-style monthly salary calculations
IF NOT EXISTS (
    SELECT 1 FROM sys.tables 
    WHERE name = 'TBL_Salary_Calc' AND schema_id = SCHEMA_ID('dbo')
)
BEGIN
    CREATE TABLE dbo.TBL_Salary_Calc (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        EPF_No NVARCHAR(10) NOT NULL,
        Employee_Name NVARCHAR(200) NULL,
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

    CREATE NONCLUSTERED INDEX IX_Salary_Calc_EPF 
    ON dbo.TBL_Salary_Calc (EPF_No);
END
GO

-- 3. TBL_Deduction_Transaction: New table for tracking multi-month deduction transactions
IF NOT EXISTS (
    SELECT 1 FROM sys.tables 
    WHERE name = 'TBL_Deduction_Transaction' AND schema_id = SCHEMA_ID('dbo')
)
BEGIN
    CREATE TABLE dbo.TBL_Deduction_Transaction (
        id NVARCHAR(50) NOT NULL PRIMARY KEY,
        EPF_No NVARCHAR(10) NOT NULL,
        Employee_Name NVARCHAR(200) NULL,
        Did_Code NVARCHAR(10) NOT NULL,
        Deduction_Name NVARCHAR(100) NULL,
        Is_Loan BIT NULL DEFAULT 0,
        Is_Uniform BIT NULL DEFAULT 0,
        Business_Center NVARCHAR(100) NULL,
        Start_Date DATE NOT NULL,
        End_Date DATE NOT NULL,
        Total_Amount DECIMAL(18,2) NOT NULL,
        Monthly_Cut DECIMAL(18,2) NOT NULL,
        Status NVARCHAR(20) NOT NULL DEFAULT 'Active',
        Created_At DATETIME2 NULL,
        Notes NVARCHAR(500) NULL
    );

    CREATE NONCLUSTERED INDEX IX_Deduction_Transaction_EPF 
    ON dbo.TBL_Deduction_Transaction (EPF_No);

    CREATE NONCLUSTERED INDEX IX_Deduction_Transaction_BC 
    ON dbo.TBL_Deduction_Transaction (Business_Center);
END
GO

-- 4. TBL_Loging_User: Add Module_Permissions column if missing
IF NOT EXISTS (
    SELECT 1 FROM sys.columns 
    WHERE object_id = OBJECT_ID(N'dbo.TBL_Loging_User') AND name = 'Module_Permissions'
)
BEGIN
    ALTER TABLE dbo.TBL_Loging_User ADD Module_Permissions NVARCHAR(MAX) NULL;
END
GO
