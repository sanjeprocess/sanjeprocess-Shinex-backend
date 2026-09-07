USE [HSB_HRIS];
GO

IF COL_LENGTH(N'dbo.TBL_Loging_User', N'Full_Name') IS NULL
    ALTER TABLE [dbo].[TBL_Loging_User] ADD [Full_Name] nvarchar(100) NULL;
IF COL_LENGTH(N'dbo.TBL_Loging_User', N'NIC_Number') IS NULL
    ALTER TABLE [dbo].[TBL_Loging_User] ADD [NIC_Number] nvarchar(20) NULL;
GO

ALTER TABLE [dbo].[TBL_Loging_User] ALTER COLUMN [Login_Name] nvarchar(50) NOT NULL;
ALTER TABLE [dbo].[TBL_Loging_User] ALTER COLUMN [Password] nvarchar(255) NOT NULL;
ALTER TABLE [dbo].[TBL_Loging_User] ALTER COLUMN [Client_Busness_Code] nvarchar(100) NOT NULL;
GO

IF COL_LENGTH(N'dbo.TBL_Loging_User', N'Is_Blocked') IS NULL
    ALTER TABLE [dbo].[TBL_Loging_User] ADD [Is_Blocked] bit NOT NULL CONSTRAINT DF_TBL_Loging_User_Is_Blocked DEFAULT 0;
IF COL_LENGTH(N'dbo.TBL_Loging_User', N'Site_Visibility') IS NULL
    ALTER TABLE [dbo].[TBL_Loging_User] ADD [Site_Visibility] bit NOT NULL CONSTRAINT DF_TBL_Loging_User_Site_Visibility DEFAULT 1;
IF COL_LENGTH(N'dbo.TBL_Loging_User', N'Manage_Users') IS NULL
    ALTER TABLE [dbo].[TBL_Loging_User] ADD [Manage_Users] bit NOT NULL CONSTRAINT DF_TBL_Loging_User_Manage_Users DEFAULT 0;
IF COL_LENGTH(N'dbo.TBL_Loging_User', N'Access_Level') IS NULL
    ALTER TABLE [dbo].[TBL_Loging_User] ADD [Access_Level] nvarchar(20) NOT NULL CONSTRAINT DF_TBL_Loging_User_Access_Level DEFAULT N'EDIT_ALLOWED';
GO
