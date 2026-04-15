package com.contoso.assettracker.controller;

import com.contoso.assettracker.service.EmployeeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class EmployeeController {
    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping("/employees")
    public String listEmployees(Model model) {
        model.addAttribute("employees", employeeService.findAll());
        return "employees";
    }

    @GetMapping("/employees/detail")
    public String employeeDetail(@RequestParam Long id, Model model) {
        model.addAttribute("employee", employeeService.findById(id));
        return "employee-detail";
    }
}
