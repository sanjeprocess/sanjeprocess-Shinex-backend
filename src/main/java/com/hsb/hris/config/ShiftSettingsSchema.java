package com.hsb.hris.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** Additive, idempotent columns for plant shift settings, for databases that have not run the SQL script. */
@Component
public class ShiftSettingsSchema {

    private static final Logger log = LoggerFactory.getLogger(ShiftSettingsSchema.class);

    private final JdbcTemplate jdbc;

    public ShiftSettingsSchema(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @EventListener(ApplicationReadyEvent.class)
    public void ensureSchema() {
        addColumn("TBL_M_Customer", "Shift_Start", "VARCHAR(5) NULL");
        addColumn("TBL_M_Customer", "Shift_End", "VARCHAR(5) NULL");
        addColumn("TBL_M_Customer", "Min_Work_Hours", "DECIMAL(5,2) NULL");
        addColumn("TBL_M_Customer", "Saturday_Min_Work_Hours", "DECIMAL(5,2) NULL");
        addColumn("TBL_M_Customer", "Count_From_Shift_Start", "BIT NULL");
        addColumn("TBL_M_Customer", "Max_Hours_Cap", "DECIMAL(5,2) NULL");
        addColumn("TBL_T_Attendance", "Min_Work_Hours", "FLOAT NULL");
        addColumn("TBL_M_Customer", "Business_Center", "NVARCHAR(100) NULL");
        fillEmptyBusinessCenters();
    }

    // Empty plants and sections take the business center most of their employees belong to
    private void fillEmptyBusinessCenters() {
        String[] updates = {
            "UPDATE c SET Business_Center = (SELECT TOP 1 LTRIM(RTRIM(e.Emp_Business_Center)) FROM dbo.TBL_Emp_Master e " +
                "WHERE (LTRIM(RTRIM(e.Emp_Plant_Code)) = LTRIM(RTRIM(c.Cus_Code)) OR LTRIM(RTRIM(e.Emp_Plant_Code)) LIKE LTRIM(RTRIM(c.Cus_Code)) + ' /%') " +
                "AND NULLIF(LTRIM(RTRIM(e.Emp_Business_Center)), '') IS NOT NULL " +
                "GROUP BY LTRIM(RTRIM(e.Emp_Business_Center)) ORDER BY COUNT(*) DESC) " +
                "FROM dbo.TBL_M_Customer c WHERE NULLIF(LTRIM(RTRIM(c.Business_Center)), '') IS NULL",
            "UPDATE s SET Business_Center = (SELECT TOP 1 LTRIM(RTRIM(e.Emp_Business_Center)) FROM dbo.TBL_Emp_Master e " +
                "WHERE LTRIM(RTRIM(e.Emp_Section_Code)) = LTRIM(RTRIM(s.Section_Code)) " +
                "AND NULLIF(LTRIM(RTRIM(e.Emp_Business_Center)), '') IS NOT NULL " +
                "GROUP BY LTRIM(RTRIM(e.Emp_Business_Center)) ORDER BY COUNT(*) DESC) " +
                "FROM dbo.TBL_M_Section s WHERE NULLIF(LTRIM(RTRIM(s.Business_Center)), '') IS NULL"
        };
        for (String sql : updates) {
            try {
                jdbc.execute(sql);
            } catch (Exception ex) {
                log.warn("Filling empty business centers failed: {}", ex.getMessage());
            }
        }
    }

    private void addColumn(String table, String column, String definition) {
        try {
            jdbc.execute("IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID(N'dbo." + table + "') AND name = '" + column + "') " +
                    "ALTER TABLE dbo." + table + " ADD " + column + " " + definition);
        } catch (Exception ex) {
            log.warn("Shift settings schema check failed for {}.{}: {}", table, column, ex.getMessage());
        }
    }
}
