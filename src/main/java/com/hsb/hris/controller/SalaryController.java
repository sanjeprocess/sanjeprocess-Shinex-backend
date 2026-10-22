package com.hsb.hris.controller;

import com.hsb.hris.entity.SalaryCalculation;
import com.hsb.hris.service.ExcelStyleSalaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/salary", "/salary"})
public class SalaryController {

    private final ExcelStyleSalaryService salaryService;

    public SalaryController(ExcelStyleSalaryService salaryService) {
        this.salaryService = salaryService;
    }

    @PostMapping("/calculate")
    public ResponseEntity<List<SalaryCalculation>> calculate(
            @RequestParam String year,
            @RequestParam String month,
            @RequestParam(required = false) String plant) {
        List<SalaryCalculation> results = salaryService.calculateSalary(year, month, plant);
        return ResponseEntity.ok(results);
    }

    @GetMapping
    public ResponseEntity<List<SalaryCalculation>> getSalary(
            @RequestParam(required = false) String year,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) String plant) {
        List<SalaryCalculation> results = salaryService.getCalculatedSalary(year, month, plant);
        return ResponseEntity.ok(results);
    }
}
