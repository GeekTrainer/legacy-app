package com.contoso.assettracker.repository;

import com.contoso.assettracker.model.Employee;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class EmployeeRepository {
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Employee> rowMapper = (rs, rowNum) -> {
        Employee employee = new Employee();
        employee.setEmployeeId(rs.getLong("employee_id"));
        employee.setFirstName(rs.getString("first_name"));
        employee.setLastName(rs.getString("last_name"));
        employee.setEmail(rs.getString("email"));
        employee.setDepartment(rs.getString("department"));
        employee.setTitle(rs.getString("title"));
        employee.setHireDate(rs.getString("hire_date"));
        employee.setActive(rs.getBoolean("is_active"));
        return employee;
    };

    public EmployeeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Employee> findAll() {
        return jdbcTemplate.query("SELECT * FROM employees ORDER BY employee_id", rowMapper);
    }

    public Employee findById(Long id) {
        List<Employee> list = jdbcTemplate.query("SELECT * FROM employees WHERE employee_id = ?", rowMapper, id);
        return list.isEmpty() ? null : list.get(0);
    }

    public long countAll() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM employees", Long.class);
        return count == null ? 0 : count;
    }
}
