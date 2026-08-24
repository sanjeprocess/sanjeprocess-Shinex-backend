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
    public List<Employee> findByBusinessCenter(String bc) { return repo.findByBusinessCenter(bc); }
}
