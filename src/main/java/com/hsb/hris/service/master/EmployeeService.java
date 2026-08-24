package com.hsb.hris.service.master;

import com.hsb.hris.entity.Employee;

import java.util.List;
import java.util.Optional;

public interface EmployeeService {
    List<Employee> findAll();
    Optional<Employee> findById(String id);
    Employee save(Employee e);
    void deleteById(String id);
    List<Employee> findByBusinessCenter(String bc);
}
