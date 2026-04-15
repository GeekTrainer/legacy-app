package com.contoso.assettracker.service;

import com.contoso.assettracker.model.Assignment;
import com.contoso.assettracker.repository.AssignmentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AssignmentService {
    private final AssignmentRepository assignmentRepository;

    public AssignmentService(AssignmentRepository assignmentRepository) {
        this.assignmentRepository = assignmentRepository;
    }

    public List<Assignment> findAll() { return assignmentRepository.findAll(); }

    public String assign(Assignment assignment) {
        if (assignmentRepository.countActiveAssignmentsForAsset(assignment.getAssetId()) > 0) {
            return "Asset already has an active assignment.";
        }
        if (assignment.getAssignedDate() == null || assignment.getAssignedDate().isBlank()) {
            assignment.setAssignedDate(LocalDate.now().toString());
        }
        assignmentRepository.create(assignment);
        return null;
    }

    public void returnAsset(Long assignmentId) {
        assignmentRepository.returnAsset(assignmentId, LocalDate.now().toString());
    }

    public long countOpenAssignments() { return assignmentRepository.countOpenAssignments(); }
}
