package com.contoso.assettracker.service;

import com.contoso.assettracker.model.Employee;
import com.contoso.assettracker.repository.EmployeeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public List<Employee> findAll() { return employeeRepository.findAll(); }
    public Employee findById(Long id) { return employeeRepository.findById(id); }
    public long countAll() { return employeeRepository.countAll(); }
}
