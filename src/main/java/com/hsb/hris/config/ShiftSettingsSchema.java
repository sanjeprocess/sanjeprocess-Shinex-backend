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
