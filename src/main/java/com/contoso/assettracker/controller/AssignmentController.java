package com.contoso.assettracker.controller;

import com.contoso.assettracker.model.Assignment;
import com.contoso.assettracker.service.AssetService;
import com.contoso.assettracker.service.AssignmentService;
import com.contoso.assettracker.service.EmployeeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AssignmentController {
    private final AssignmentService assignmentService;
    private final AssetService assetService;
    private final EmployeeService employeeService;

    public AssignmentController(AssignmentService assignmentService, AssetService assetService, EmployeeService employeeService) {
        this.assignmentService = assignmentService;
        this.assetService = assetService;
        this.employeeService = employeeService;
    }

    @GetMapping("/assignments")
    public String assignments(Model model) {
        model.addAttribute("assignments", assignmentService.findAll());
        model.addAttribute("assets", assetService.findAll());
        model.addAttribute("employees", employeeService.findAll());
        return "assignments";
    }

    @PostMapping("/assignments")
    public String createAssignment(Assignment assignment, Model model) {
        String error = assignmentService.assign(assignment);
        if (error != null) {
            model.addAttribute("error", error);
            return assignments(model);
        }
        return "redirect:/assignments";
    }

    @PostMapping("/assignments/return")
    public String returnAssignment(@RequestParam Long assignmentId) {
        assignmentService.returnAsset(assignmentId);
        return "redirect:/assignments";
    }
}
