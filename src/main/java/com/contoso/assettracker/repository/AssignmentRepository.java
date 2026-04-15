package com.contoso.assettracker.repository;

import com.contoso.assettracker.model.Assignment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AssignmentRepository {
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Assignment> rowMapper = (rs, rowNum) -> {
        Assignment assignment = new Assignment();
        assignment.setAssignmentId(rs.getLong("assignment_id"));
        assignment.setAssetId(rs.getLong("asset_id"));
        assignment.setEmployeeId(rs.getLong("employee_id"));
        assignment.setAssignedDate(rs.getString("assigned_date"));
        assignment.setReturnedDate(rs.getString("returned_date"));
        assignment.setAssignedBy(rs.getString("assigned_by"));
        assignment.setNotes(rs.getString("notes"));
        return assignment;
    };

    public AssignmentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Assignment> findAll() {
        return jdbcTemplate.query("SELECT * FROM assignment_history ORDER BY assignment_id DESC", rowMapper);
    }

    public long countActiveAssignmentsForAsset(Long assetId) {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM assignment_history WHERE asset_id = ? AND returned_date IS NULL", Long.class, assetId);
        return count == null ? 0 : count;
    }

    public void create(Assignment assignment) {
        jdbcTemplate.update("INSERT INTO assignment_history (asset_id, employee_id, assigned_date, returned_date, assigned_by, notes) VALUES (?,?,?,?,?,?)",
                assignment.getAssetId(), assignment.getEmployeeId(), assignment.getAssignedDate(), assignment.getReturnedDate(), assignment.getAssignedBy(), assignment.getNotes());
        jdbcTemplate.update("UPDATE assets SET status = 'assigned' WHERE asset_id = ?", assignment.getAssetId());
    }

    public void returnAsset(Long assignmentId, String returnDate) {
        jdbcTemplate.update("UPDATE assignment_history SET returned_date = ? WHERE assignment_id = ?", returnDate, assignmentId);
        Long assetId = jdbcTemplate.queryForObject("SELECT asset_id FROM assignment_history WHERE assignment_id = ?", Long.class, assignmentId);
        if (assetId != null) {
            jdbcTemplate.update("UPDATE assets SET status = 'available' WHERE asset_id = ?", assetId);
        }
    }

    public long countOpenAssignments() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM assignment_history WHERE returned_date IS NULL", Long.class);
        return count == null ? 0 : count;
    }
}
