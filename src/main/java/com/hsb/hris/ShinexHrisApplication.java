package com.hsb.hris;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hsb.hris.entity.Employee;
import com.hsb.hris.service.master.EmployeeService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import java.io.InputStream;
import java.util.List;

@SpringBootApplication
public class ShinexHrisApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShinexHrisApplication.class, args);
    }

    @Bean
    CommandLineRunner runner(EmployeeService employeeService) {
        return args -> {
            if (employeeService.findAll().isEmpty()) {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    InputStream inputStream = getClass().getResourceAsStream("/employees.json");
                    if (inputStream == null) {
                        System.out.println("employees.json not found in classpath. Looking in project root.");
                        java.io.File file = new java.io.File("employees.json");
                        if (file.exists()) {
                            List<Employee> employees = mapper.readValue(file, new TypeReference<List<Employee>>(){});
                            for (Employee employee : employees) {
                                Employee saved = employeeService.save(employee);
                                System.out.println("Saved employee: " + saved.getEpfNo() + " - " + saved.getFirstName());
                            }
                            System.out.println("Employees saved to database successfully.");
                        } else {
                            System.out.println("employees.json not found.");
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Unable to save employees: " + e.getMessage());
                }
            } else {
                System.out.println("Employees already exist in the database.");
            }
        };
    }
}
