package com.irctc.service;

import com.irctc.dao.EmployeeDAO;
import com.irctc.exception.ValidationException;
import com.irctc.model.Employee;
import com.irctc.util.Catalog;
import com.irctc.util.DaoSupport;
import com.irctc.util.Validators;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class EmployeeService {
    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final ActivityService activityService = new ActivityService();

    public void save(long adminId, Employee employee) throws ValidationException {
        validate(employee);
        employee.setEmail(employee.getEmail().toLowerCase(Locale.ROOT));
        Map<String, String> errors = new LinkedHashMap<>();
        DaoSupport.transaction("The employee could not be saved.", connection -> {
            if (employeeDAO.emailTaken(connection, employee.getEmail(), employee.getId())) {
                errors.put("email", "That email is already in use.");
                return;
            }
            if (employee.getId() == 0) {
                long id = employeeDAO.insert(connection, employee);
                activityService.record(connection, adminId, "EMPLOYEE_ADDED",
                        "Employee registered (" + employee.getFullName() + ")", "EMPLOYEE", id);
            } else if (employeeDAO.findById(employee.getId()) == null) {
                errors.put("form", "That employee no longer exists.");
            } else {
                employeeDAO.update(connection, employee);
                activityService.record(connection, adminId, "EMPLOYEE_UPDATED",
                        "Employee updated (" + employee.getFullName() + ")", "EMPLOYEE", employee.getId());
            }
        });
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    public boolean setStatus(long adminId, long employeeId, String status) {
        if (!Catalog.ACCOUNT_STATUSES.contains(status)) {
            return false;
        }
        boolean[] updated = {false};
        DaoSupport.transaction("The employee status could not be changed.", connection -> {
            Employee employee = employeeDAO.findById(employeeId);
            if (employee == null) {
                return;
            }
            employeeDAO.updateStatus(connection, employeeId, status);
            updated[0] = true;
            String verb = "ACTIVE".equals(status) ? "activated" : "deactivated";
            activityService.record(connection, adminId, "EMPLOYEE_UPDATED",
                    "Employee " + verb + " (" + employee.getFullName() + ")", "EMPLOYEE", employeeId);
        });
        return updated[0];
    }

    private void validate(Employee employee) throws ValidationException {
        Validators validators = new Validators();
        validators.required("fullName", employee.getFullName(), "Full name");
        validators.maxLength("fullName", employee.getFullName(), 120, "Full name");
        validators.required("email", employee.getEmail(), "Email");
        validators.maxLength("email", employee.getEmail(), 150, "Email");
        validators.email("email", employee.getEmail());
        validators.required("phone", employee.getPhone(), "Mobile number");
        validators.phone("phone", employee.getPhone());
        validators.required("department", employee.getDepartment(), "Department");
        validators.maxLength("department", employee.getDepartment(), 80, "Department");
        validators.required("designation", employee.getDesignation(), "Designation");
        validators.maxLength("designation", employee.getDesignation(), 80, "Designation");
        validators.oneOf("status", employee.getStatus(), Catalog.ACCOUNT_STATUSES, "Choose a status.");
        validators.finish();
    }
}
