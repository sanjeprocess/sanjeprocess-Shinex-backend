package com.hsb.hris.service.master.impl;

import com.hsb.hris.entity.Employee;
import com.hsb.hris.repository.EmployeeRepository;
import com.hsb.hris.service.master.EmployeeService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository repo;

    public EmployeeServiceImpl(EmployeeRepository repo) { this.repo = repo; }

    @Override
    public List<Employee> findAll() { return repo.findAll(); }

    @Override
    public Optional<Employee> findById(String id) { return repo.findById(id); }

    @Override
    public Employee save(Employee e) { return repo.save(e); }

    @Override
    public void deleteById(String id) { repo.deleteById(id); }

    @Override
    public List<Employee> findByBusinessCenter(String bc) {
        if (bc == null || bc.trim().isEmpty() || "ALL".equalsIgnoreCase(bc.trim())) {
            return repo.findAll();
        }
        return repo.findByBusinessCenterSmart(bc.trim());
    }

    @Override
    public String getNextEpfNo(String bc) {
        List<String> epfs = repo.findAllEpfNos();
        long maxNum = 0;
        int maxDigits = 3;
        String prefix = "91EPF";
        
        for (String raw : epfs) {
            if (raw == null) continue;
            String epf = raw.trim().toUpperCase();
            String numPart = "";
            if (epf.startsWith("91EPF")) {
                numPart = epf.substring(5).trim();
            } else if (epf.startsWith("EPF")) {
                numPart = epf.substring(3).trim();
            } else {
                numPart = epf.replaceAll("\\D", "");
            }
            if (!numPart.isEmpty()) {
                try {
                    long val = Long.parseLong(numPart);
                    if (val > maxNum) {
                        maxNum = val;
                        if (numPart.length() > maxDigits) {
                            maxDigits = numPart.length();
                        }
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        
        long nextVal = maxNum + 1;
        String formatted = String.format("%0" + Math.max(3, maxDigits) + "d", nextVal);
        return prefix + formatted;
    }
}
