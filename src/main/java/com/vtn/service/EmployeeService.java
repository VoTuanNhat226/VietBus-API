package com.vtn.service;

import com.vtn.dto.request.EmployeeRequest;
import com.vtn.entity.AccountEntity;
import com.vtn.entity.EmployeeEntity;
import com.vtn.repository.AccountRepository;
import com.vtn.repository.EmployeeRepository;
import com.vtn.utils.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final AccountRepository accountRepository;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository,
                           AccountRepository accountRepository) {
        this.employeeRepository = employeeRepository;
        this.accountRepository = accountRepository;
    }

    private boolean isAllParametersNull(EmployeeRequest employeeRequest) {
        return ((employeeRequest.getLastName() == null) &&
                (employeeRequest.getFirstName() == null) &&
                (employeeRequest.getPhoneNumber() == null) &&
                (employeeRequest.getPosition() == null));
    }

    public BaseResponse getAllEmployees(EmployeeRequest employeeRequest) {
        try {
            List<EmployeeEntity> employees = new ArrayList<>();
            if(isAllParametersNull(employeeRequest)) {
                employees = employeeRepository.findAll();
            } else {
                employees = employeeRepository.findByCondition(
                        employeeRequest.getFirstName(),
                        employeeRequest.getLastName(),
                        employeeRequest.getPhoneNumber(),
                        employeeRequest.getPosition());
            }
            return new BaseResponse(200, employees, "Get all employees successfully", "Get all employees successfully", null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public BaseResponse createEmployee(EmployeeRequest employeeRequest) {
        UserDetails info = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try {
            AccountEntity account = accountRepository.findByAccountId(employeeRequest.getAccountId());
            if (account == null) {
                return new BaseResponse(404, null, "Not found account", "Not found account", null);
            } else {
                EmployeeEntity employee = new EmployeeEntity();
                employee.setFirstName(employeeRequest.getFirstName());
                employee.setLastName(employeeRequest.getLastName());
                employee.setPhoneNumber(employeeRequest.getPhoneNumber());
                employee.setPosition(employeeRequest.getPosition());
                employee.setActive(true);
                employee.setAccount(account);
                employee.setCreated_by(info.getUsername());
                employee.setCreated_at(LocalDateTime.now());
                employeeRepository.save(employee);
                return new BaseResponse(201, employee, "Create employee successfully", "No error", null);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public BaseResponse updateEmployee(EmployeeRequest employeeRequest) {
        UserDetails info = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try {
            EmployeeEntity employee = employeeRepository.findByEmployeeId(employeeRequest.getEmployeeId());
            if (employee == null) {
                return new BaseResponse(404, null, "Not found employee", "Not found employee", null);
            } else {
                employee.setFirstName(employeeRequest.getFirstName());
                employee.setLastName(employeeRequest.getLastName());
                employee.setPhoneNumber(employeeRequest.getPhoneNumber());
                employee.setPosition(employeeRequest.getPosition());
                employee.setActive(true);
                employee.setUpdated_by(info.getUsername());
                employee.setUpdated_at(LocalDateTime.now());
                employeeRepository.save(employee);
                return new BaseResponse(200, employee, "Update employee successfully", "No error", null);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public BaseResponse deleteEmployee(EmployeeRequest employeeRequest) {
        UserDetails info = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String role = info.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse(null);
        try {
            if("ROLE_ADMIN".equals(role)) {
                EmployeeEntity employee = employeeRepository.findByEmployeeId(employeeRequest.getEmployeeId());
                if (employee == null) {
                    return new BaseResponse(404, null, "Not found employee", "Not found employee", null);
                } else {
                    employeeRepository.delete(employee);
                    return new BaseResponse(204, null, "Delete employee successfully", "No error", null);
                }
            } else {
                return new BaseResponse(403, null, "You don't has permission", "No error", null);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
