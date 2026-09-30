package com.hsb.hris.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DatabaseSchemaMigration implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseSchemaMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        try {
            // Ensure TBL_M_Section exists and has Business_Center and Basic_Salary columns
            jdbcTemplate.execute(
                "IF OBJECT_ID(N'dbo.TBL_M_Section', N'U') IS NULL " +
                "BEGIN " +
                "    CREATE TABLE dbo.TBL_M_Section ( " +
                "        Section_Code nvarchar(6) NOT NULL CONSTRAINT PK_TBL_M_Section PRIMARY KEY, " +
                "        Section_Name nvarchar(50) NULL, " +
                "        Business_Center nvarchar(100) NULL, " +
                "        Basic_Salary decimal(18,2) NULL " +
                "    ); " +
                "END " +
                "ELSE " +
                "BEGIN " +
                "    IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID(N'dbo.TBL_M_Section') AND name = 'Business_Center') " +
                "        ALTER TABLE dbo.TBL_M_Section ADD Business_Center nvarchar(100) NULL; " +
                "    IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID(N'dbo.TBL_M_Section') AND name = 'Basic_Salary') " +
                "        ALTER TABLE dbo.TBL_M_Section ADD Basic_Salary decimal(18,2) NULL; " +
                "END"
            );
            // Ensure TBL_Emp_Master has Emp_Photo_Url column
            jdbcTemplate.execute(
                "IF OBJECT_ID(N'dbo.TBL_Emp_Master', N'U') IS NOT NULL " +
                "BEGIN " +
                "    IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID(N'dbo.TBL_Emp_Master') AND name = 'Emp_Photo_Url') " +
                "        ALTER TABLE dbo.TBL_Emp_Master ADD Emp_Photo_Url nvarchar(max) NULL; " +
                "END"
            );
            System.out.println("✅ Database schema migration for TBL_M_Section & TBL_Emp_Master checked successfully.");
        } catch (Exception e) {
            System.err.println("⚠️ Database schema migration note: " + e.getMessage());
        }
    }
}
