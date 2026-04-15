package com.contoso.assettracker.controller;

import com.contoso.assettracker.service.AssetService;
import com.contoso.assettracker.service.AssignmentService;
import com.contoso.assettracker.service.EmployeeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {
    private final AssetService assetService;
    private final EmployeeService employeeService;
    private final AssignmentService assignmentService;

    public DashboardController(AssetService assetService, EmployeeService employeeService, AssignmentService assignmentService) {
        this.assetService = assetService;
        this.employeeService = employeeService;
        this.assignmentService = assignmentService;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("totalAssets", assetService.countAll());
        model.addAttribute("totalEmployees", employeeService.countAll());
        model.addAttribute("openAssignments", assignmentService.countOpenAssignments());
        model.addAttribute("availableCount", assetService.countByStatus("available"));
        model.addAttribute("assignedCount", assetService.countByStatus("assigned"));
        model.addAttribute("retiredCount", assetService.countByStatus("retired"));
        model.addAttribute("lostCount", assetService.countByStatus("lost"));
        return "dashboard";
    }
}
