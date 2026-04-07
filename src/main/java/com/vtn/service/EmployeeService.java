package com.vtn.service;

import com.vtn.dto.request.EmployeeRequest;
import com.vtn.entity.AccountEntity;
import com.vtn.entity.EmployeeEntity;
import com.vtn.repository.AccountRepository;
import com.vtn.repository.EmployeeRepository;
import com.vtn.utils.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
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

    public BaseResponse getAllEmployees(EmployeeRequest request) {
        List<EmployeeEntity> employees = employeeRepository.getAllByCondition(
                    request.getFullName(),
                    request.getPhoneNumber(),
                    request.getPosition(),
                    request.getCreatedBy(),
                    request.getUpdatedBy(),
                    request.getActive());

        return new BaseResponse(200, employees, "Get all employees successful", null, null);
    }

    public BaseResponse getAllEmployeeActiveByPosition(EmployeeRequest request) {
        log.info("Get active employees by position: {}", request.getPosition());
        if (request.getPosition() == null) {
            log.warn("Position is required");
            return new BaseResponse(400, null, "Position is required", null, null);
        }
        List<EmployeeEntity> employeeActive = employeeRepository.getAllEmployeeActiveByPosition(request.getPosition());
        return new BaseResponse(200, employeeActive, "Get all employees active by position successful", null, null);
    }

    @Transactional
    public BaseResponse createEmployee(EmployeeRequest employeeRequest) {
        log.info("Start createEmployee with request: {}", employeeRequest);
        UserDetails info = getInfo();
        log.info("User {} is creating employee", info.getUsername());

        boolean isAdmin = isAdmin(info);
        if (!isAdmin) {
            log.warn("User {} does not have permission to create employee", info.getUsername());
            return new BaseResponse(403, null, "You don't have permission!", null, null);
        }

        if (employeeRequest.getFullName() == null || employeeRequest.getPhoneNumber() == null) {
            log.warn("Missing required fields: fullName or phoneNumber");
            return new BaseResponse(400, null, "FullName and Phone are required", null, null);
        }

        EmployeeEntity existed = employeeRepository.findByFullNameAndPhoneNumber(
                        employeeRequest.getFullName(),
                        employeeRequest.getPhoneNumber());

        if (existed != null) {
            log.warn("Employee already exists with name {} and phone {}", employeeRequest.getFullName(), employeeRequest.getPhoneNumber());
            return new BaseResponse(409, null, "Employee already existed", null, null);
        }

        EmployeeEntity employee = new EmployeeEntity();
        employee.setFullName(employeeRequest.getFullName());
        employee.setPhoneNumber(employeeRequest.getPhoneNumber());
        employee.setPosition(employeeRequest.getPosition());
        employee.setActive(employeeRequest.getActive());
        if(employeeRequest.getAccountId() != null) {
            log.info("Mapping accountId: {}", employeeRequest.getAccountId());
            AccountEntity account = accountRepository.findByAccountId(employeeRequest.getAccountId());
            if (account == null) {
                log.error("Account not found with id: {}", employeeRequest.getAccountId());
                return new BaseResponse(404, null, "Account not found", null, null);
            } else {
                employee.setAccount(account);
            }
        }
        employee.setCreatedBy(info.getUsername());
        employee.setCreatedAt(LocalDateTime.now());
        employeeRepository.save(employee);
        log.info("Employee created successful with id: {}", employee.getEmployeeId());

        return new BaseResponse(201, employee, "Create employee successful", null, null);
    }

    @Transactional
    public BaseResponse updateEmployee(EmployeeRequest employeeRequest) {
        log.info("Start updateEmployee with request: {}", employeeRequest);
        UserDetails info = getInfo();
        log.info("User {} is updating employee", info.getUsername());

        boolean isAdmin = isAdmin(info);
        if (!isAdmin) {
            log.warn("User {} does not have permission to create employee", info.getUsername());
            return new BaseResponse(403, null, "You don't have permission!", null, null);
        }

        EmployeeEntity employee = employeeRepository.findByEmployeeId(employeeRequest.getEmployeeId());
        if (employee == null) {
            return new BaseResponse(404, null, "Employee not found", null, null);
        } else {
            employee.setFullName(employeeRequest.getFullName());
            employee.setPhoneNumber(employeeRequest.getPhoneNumber());
            employee.setPosition(employeeRequest.getPosition());
            employee.setActive(employeeRequest.getActive());
            employee.setUpdatedBy(info.getUsername());
            employee.setUpdatedAt(LocalDateTime.now());
            employeeRepository.save(employee);
            log.info("Employee updated successful with id: {}", employee.getEmployeeId());

            return new BaseResponse(200, employee, "Update employee successful", null, null);
        }
    }

    public BaseResponse deleteEmployee(EmployeeRequest employeeRequest) {
        log.info("Start deleteEmployee with request: {}", employeeRequest);
        UserDetails info = getInfo();
        log.info("User {} is deleting employee", info.getUsername());
        boolean isAdmin = isAdmin(info);
        if (!isAdmin) {
            log.warn("User {} does not have permission to delete employee", info.getUsername());
            return new BaseResponse(403, null, "You don't have permission!", null, null);
        }

        if (employeeRequest.getEmployeeId() == null) {
            log.warn("Missing required fields: employeeId");
            return new BaseResponse(400, null, "EmployeeId is required", null, null);
        }

        EmployeeEntity employee = employeeRepository
                .findByEmployeeId(employeeRequest.getEmployeeId());

        if (employee == null) {
            log.error("Employee not found with id: {}", employeeRequest.getEmployeeId());
            return new BaseResponse(404, null, "Employee not found", null, null);
        }

        employeeRepository.delete(employee);
        log.info("Employee deleted successful with id: {}", employee.getEmployeeId());

        return new BaseResponse(200, null, "Delete employee successful", null, null);
    }

    private boolean isAdmin(UserDetails info) {
        return info.getAuthorities()
                .stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    private UserDetails getInfo() {
        return (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
