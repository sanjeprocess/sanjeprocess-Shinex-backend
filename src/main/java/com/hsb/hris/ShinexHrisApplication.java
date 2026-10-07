package com.hsb.hris;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hsb.hris.entity.Employee;
import com.hsb.hris.entity.LoginUser;
import com.hsb.hris.repository.LoginUserRepository;
import com.hsb.hris.service.master.EmployeeService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.io.InputStream;
import java.util.List;

@SpringBootApplication
public class ShinexHrisApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShinexHrisApplication.class, args);
    }

    @Bean
    CommandLineRunner runner(EmployeeService employeeService, LoginUserRepository userRepo, PasswordEncoder passwordEncoder, org.springframework.jdbc.core.JdbcTemplate jdbcTemplate) {
        return args -> {
            // ── Auto-migrate schema for Module_Permissions column if missing ──
            try {
                jdbcTemplate.execute(
                    "IF COL_LENGTH(N'dbo.TBL_Loging_User', N'Module_Permissions') IS NULL " +
                    "ALTER TABLE [dbo].[TBL_Loging_User] ADD [Module_Permissions] nvarchar(max) NULL;"
                );
            } catch (Exception e1) {
                try {
                    jdbcTemplate.execute(
                        "IF COL_LENGTH(N'TBL_Loging_User', N'Module_Permissions') IS NULL " +
                        "ALTER TABLE [TBL_Loging_User] ADD [Module_Permissions] nvarchar(max) NULL;"
                    );
                } catch (Exception ignored) {}
            }

            // ── Seed Superadmin User (superadmin / 123 / SUPERADMIN) ──
            try {
                LoginUser superAdmin = userRepo.findByLoginName("superadmin").orElse(null);
                if (superAdmin == null) {
                    superAdmin = new LoginUser();
                    superAdmin.setLoginName("superadmin");
                    superAdmin.setPassword(passwordEncoder.encode("123"));
                    superAdmin.setRole("SUPERADMIN");
                    superAdmin.setClientBusinessCode("ALL");
                    userRepo.save(superAdmin);
                    System.out.println("✅ Superadmin account seeded successfully (username: superadmin, password: 123)");
                } else {
                    // Ensure role is SUPERADMIN and password encoded
                    if (!"SUPERADMIN".equalsIgnoreCase(superAdmin.getRole())) {
                        superAdmin.setRole("SUPERADMIN");
                        userRepo.save(superAdmin);
                    }
                }
            } catch (Exception e) {
                System.out.println("⚠️ Note on superadmin user seed: " + e.getMessage());
            }

            // ── Seed Employees (if empty and json present) ──
            if (employeeService.findAll().isEmpty()) {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    InputStream inputStream = getClass().getResourceAsStream("/employees.json");
                    if (inputStream == null) {
                        java.io.File file = new java.io.File("employees.json");
                        if (file.exists()) {
                            List<Employee> employees = mapper.readValue(file, new TypeReference<List<Employee>>(){});
                            for (Employee employee : employees) {
                                Employee saved = employeeService.save(employee);
                                System.out.println("Saved employee: " + saved.getEpfNo() + " - " + saved.getFirstName());
                            }
                            System.out.println("Employees saved to database successfully.");
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Unable to save employees: " + e.getMessage());
                }
            }
        };
    }
}
