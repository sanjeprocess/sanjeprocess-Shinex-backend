/*
  Shinex HRIS - Phase 1 local SQL Server schema

  Use this script for a fresh local Shinex database. The application uses
  ddl-auto=none, so schema changes are intentionally explicit.
  Do not run this against an existing production database without profiling
  and migrating the legacy tables first.
*/

IF DB_ID(N'Shinex') IS NULL
BEGIN
    CREATE DATABASE Shinex;
END;
GO

USE Shinex;
GO

IF OBJECT_ID(N'dbo.TBL_M_Company', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.TBL_M_Company (
        Company_ID nvarchar(10) NOT NULL CONSTRAINT PK_TBL_M_Company PRIMARY KEY,
        Company_Name nvarchar(50) NULL,
        Company_Address nvarchar(150) NULL,
        Tel_No nvarchar(10) NULL,
        Email_ID nvarchar(50) NULL,
        Web_Addres nvarchar(50) NULL,
        EPF_Reg nvarchar(15) NULL,
        VAR_Reg nvarchar(15) NULL,
        BR_No nvarchar(15) NULL,
        Fax_No nvarchar(15) NULL
    );
END;
GO

IF OBJECT_ID(N'dbo.TBL_M_Customer', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.TBL_M_Customer (
        Cus_Code nvarchar(10) NOT NULL CONSTRAINT PK_TBL_M_Customer PRIMARY KEY,
        Cus_Name nvarchar(100) NULL,
        Cus_Add1 nvarchar(50) NULL,
        Cus_Add2 nvarchar(50) NULL,
        Cus_Add3 nvarchar(50) NULL,
        Cus_Email nvarchar(30) NULL,
        Cus_Contact_No nvarchar(12) NULL,
        Cus_Contact_name nvarchar(50) NULL,
        Cus_Working_das decimal(10,2) NULL,
        Cus_OT_Calcultion_Auto bit NULL,
        Cus_Min_Staff_Quy decimal(10,2) NULL,
        Attendence_Allowance bit NULL,
        days_to_work_for_Att_Allow decimal(10,2) NULL
    );
END;
GO

IF OBJECT_ID(N'dbo.TBL_M_Section', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.TBL_M_Section (
        Section_Code nvarchar(6) NOT NULL CONSTRAINT PK_TBL_M_Section PRIMARY KEY,
        Section_Name nvarchar(25) NULL
    );
END;
GO

IF OBJECT_ID(N'dbo.TBL_M_Bank', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.TBL_M_Bank (
        Bank_ID nvarchar(10) NOT NULL CONSTRAINT PK_TBL_M_Bank PRIMARY KEY,
        Bank_Name nvarchar(50) NULL
    );
END;
GO

IF OBJECT_ID(N'dbo.TBL_M_Leave_type', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.TBL_M_Leave_type (
        Leave_Type nchar(3) NOT NULL CONSTRAINT PK_TBL_M_Leave_type PRIMARY KEY,
        Leave_Name nvarchar(50) NULL
    );
END;
GO

MERGE dbo.TBL_M_Leave_type AS target
USING (VALUES
    (N'AL', N'Annual Leave'),
    (N'CL', N'Casual Leave'),
    (N'SL', N'Sick Leave'),
    (N'ML', N'Maternity Leave'),
    (N'NOP', N'No Pay Leave'),
    (N'SH', N'Special Holiday')
) AS source (Leave_Type, Leave_Name)
ON target.Leave_Type = source.Leave_Type
WHEN MATCHED THEN UPDATE SET Leave_Name = source.Leave_Name
WHEN NOT MATCHED THEN INSERT (Leave_Type, Leave_Name) VALUES (source.Leave_Type, source.Leave_Name);
GO

IF OBJECT_ID(N'dbo.TBL_Emp_Master', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.TBL_Emp_Master (
        Emp_EPF_No nvarchar(10) NOT NULL CONSTRAINT PK_TBL_Emp_Master PRIMARY KEY,
        Emp_NIC_No nvarchar(15) NULL,
        Emp_Plant_Code nvarchar(10) NULL,
        Emp_Date_Of_Birth date NULL,
        Emp_Name nvarchar(50) NOT NULL,
        Emp_Nam1 nvarchar(50) NULL,
        Emp_Address nvarchar(150) NULL,
        Emp_Contact_No nvarchar(15) NULL,
        Emp_Mobile_No nvarchar(15) NULL,
        Emp_Email_Address nvarchar(100) NULL,
        Emp_Hired_Date date NULL,
        Emp_hired_month nvarchar(15) NULL,
        Emp_Basic_Salary decimal(19,4) NULL,
        Emp_Bank_Account_Number nvarchar(30) NULL,
        Emp_Bank_Name nvarchar(50) NULL,
        Emp_Bank_Branch_Name nvarchar(50) NULL,
        Emp_SWIFT nvarchar(15) NULL,
        Emp_Gender nvarchar(8) NULL,
        Emp_Section_Code nvarchar(6) NULL,
        Emp_Day_Allowance decimal(19,4) NULL,
        Emp_Night_Allowance decimal(19,4) NULL,
        Emp_B_Card_Yes bit NULL CONSTRAINT DF_Emp_BCard DEFAULT 0,
        Emp_Enative_Yes bit NULL CONSTRAINT DF_Emp_Enative DEFAULT 0,
        Emp_Deth_Denotion bit NULL CONSTRAINT DF_Emp_DeathDonation DEFAULT 0,
        Emp_Business_Center nvarchar(10) NULL,
        Sunday_Poya_Extra decimal(19,4) NULL,
        CONSTRAINT FK_Emp_Plant FOREIGN KEY (Emp_Plant_Code)
            REFERENCES dbo.TBL_M_Customer(Cus_Code),
        CONSTRAINT FK_Emp_Section FOREIGN KEY (Emp_Section_Code)
            REFERENCES dbo.TBL_M_Section(Section_Code),
        CONSTRAINT FK_Emp_Company FOREIGN KEY (Emp_Business_Center)
            REFERENCES dbo.TBL_M_Company(Company_ID)
    );
END;
GO

IF OBJECT_ID(N'dbo.TBL_Loging_User', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.TBL_Loging_User (
        Login_Name nvarchar(50) NOT NULL CONSTRAINT PK_TBL_Loging_User PRIMARY KEY,
        Password nvarchar(255) NOT NULL,
        Client_Busness_Code nvarchar(100) NOT NULL,
        Is_Blocked bit NOT NULL CONSTRAINT DF_LoginUser_Blocked DEFAULT 0,
        Site_Visibility bit NOT NULL CONSTRAINT DF_LoginUser_ViewSite DEFAULT 1,
        Access_Level nvarchar(20) NOT NULL CONSTRAINT DF_LoginUser_AccessLevel DEFAULT N'EDIT_ALLOWED',
        Manage_Users bit NOT NULL CONSTRAINT DF_LoginUser_ManageUsers DEFAULT 0,
        CONSTRAINT FK_Login_Company FOREIGN KEY (Client_Busness_Code)
            REFERENCES dbo.TBL_M_Company(Company_ID)
    );
END;
GO

IF OBJECT_ID(N'dbo.TBL_Loging_User', N'U') IS NOT NULL
BEGIN
    ALTER TABLE dbo.TBL_Loging_User ALTER COLUMN Login_Name nvarchar(50) NOT NULL;
    ALTER TABLE dbo.TBL_Loging_User ALTER COLUMN Password nvarchar(255) NOT NULL;
    ALTER TABLE dbo.TBL_Loging_User ALTER COLUMN Client_Busness_Code nvarchar(100) NOT NULL;
    IF COL_LENGTH(N'dbo.TBL_Loging_User', N'Is_Blocked') IS NULL
        ALTER TABLE dbo.TBL_Loging_User ADD Is_Blocked bit NOT NULL CONSTRAINT DF_LoginUser_Blocked_Migration DEFAULT 0;
    IF COL_LENGTH(N'dbo.TBL_Loging_User', N'Site_Visibility') IS NULL
        ALTER TABLE dbo.TBL_Loging_User ADD Site_Visibility bit NOT NULL CONSTRAINT DF_LoginUser_ViewSite_Migration DEFAULT 1;
    IF COL_LENGTH(N'dbo.TBL_Loging_User', N'Access_Level') IS NULL
        ALTER TABLE dbo.TBL_Loging_User ADD Access_Level nvarchar(20) NOT NULL CONSTRAINT DF_LoginUser_AccessLevel_Migration DEFAULT N'EDIT_ALLOWED';
    IF COL_LENGTH(N'dbo.TBL_Loging_User', N'Manage_Users') IS NULL
        ALTER TABLE dbo.TBL_Loging_User ADD Manage_Users bit NOT NULL CONSTRAINT DF_LoginUser_ManageUsers_Migration DEFAULT 0;
END;
GO

-- Local-only bootstrap account. Change the password after first login.
-- Password is BCrypt for: password
IF NOT EXISTS (SELECT 1 FROM dbo.TBL_Loging_User WHERE Login_Name = N'admin')
AND EXISTS (SELECT 1 FROM dbo.TBL_M_Company)
BEGIN
    INSERT INTO dbo.TBL_Loging_User (Login_Name, Password, Client_Busness_Code)
    SELECT N'admin', N'$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
           MIN(Company_ID)
    FROM dbo.TBL_M_Company;
END;
GO

-- Optional local alias accepted by the backend for the bootstrap account.
IF NOT EXISTS (SELECT 1 FROM dbo.TBL_Loging_User WHERE Login_Name = N'admin@local')
AND EXISTS (SELECT 1 FROM dbo.TBL_Loging_User WHERE Login_Name = N'admin')
BEGIN
    INSERT INTO dbo.TBL_Loging_User (Login_Name, Password, Client_Busness_Code)
    SELECT N'admin@local', Password, Client_Busness_Code
    FROM dbo.TBL_Loging_User
    WHERE Login_Name = N'admin';
END;
GO
