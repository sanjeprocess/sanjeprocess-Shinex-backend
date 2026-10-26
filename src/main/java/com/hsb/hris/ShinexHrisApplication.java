package com.hsb.hris;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class ShinexHrisApplication {

    private static final Logger log = LoggerFactory.getLogger(ShinexHrisApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ShinexHrisApplication.class, args);
    }

    @Bean
    CommandLineRunner databaseReadinessCheck(JdbcTemplate jdbcTemplate,
                                             @Value("${app.schema.migrate:false}") boolean schemaMigrationEnabled) {
        return args -> {
            try {
                jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            } catch (CannotGetJdbcConnectionException exception) {
                log.warn("Database is unavailable during startup; the application will continue.");
                return;
            }

            if (schemaMigrationEnabled) {
                runAdditiveMigration(jdbcTemplate);
            }
        };
    }

    private void runAdditiveMigration(JdbcTemplate jdbcTemplate) throws IOException {
        ClassPathResource script = new ClassPathResource("db/changes.sql");
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(script.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder batch = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                if ("GO".equalsIgnoreCase(line.trim())) {
                    executeBatch(jdbcTemplate, batch);
                } else {
                    batch.append(line).append('\n');
                }
            }
            executeBatch(jdbcTemplate, batch);
        }
    }

    private void executeBatch(JdbcTemplate jdbcTemplate, StringBuilder batch) {
        String sql = batch.toString().trim();
        if (!sql.isEmpty()) {
            jdbcTemplate.execute(sql);
            batch.setLength(0);
        }
    }
}
