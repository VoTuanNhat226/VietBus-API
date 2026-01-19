package com.vtn.service;

import com.vtn.dto.request.EmployeeRequest;
import com.vtn.entity.AccountEntity;
import com.vtn.entity.EmployeeEntity;
import com.vtn.repository.AccountRepository;
import com.vtn.repository.EmployeeRepository;
import com.vtn.utils.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
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

    private boolean isAllParametersNull(EmployeeRequest request) {
        return ((request.getLastName() == null) &&
                (request.getFirstName() == null) &&
                (request.getPhoneNumber() == null) &&
                (request.getPosition() == null) &&
                (request.getCreatedBy() == null) &&
                (request.getUpdatedBy() == null));
    }

    public BaseResponse getAllEmployees(EmployeeRequest request) {
        List<EmployeeEntity> employees;

        if (isAllParametersNull(request)) {
            employees = employeeRepository.findAll();
        } else {
            employees = employeeRepository.getAllByCondition(
                    request.getFirstName(),
                    request.getLastName(),
                    request.getPhoneNumber(),
                    request.getPosition(),
                    request.getCreatedBy(),
                    request.getUpdatedBy()
            );
        }

        return new BaseResponse(200, employees, "Get all employees successfully", null, null);
    }


    public BaseResponse createEmployee(EmployeeRequest employeeRequest) {
        UserDetails info = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        boolean isAdmin = info.getAuthorities()
                .stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

        if (!isAdmin) {
            return new BaseResponse(403, null, "Bạn không có quyền!", null, null);
        }

        EmployeeEntity existed = employeeRepository
                .findByFirstNameLastNamePhoneNumber(
                        employeeRequest.getFirstName(),
                        employeeRequest.getLastName(),
                        employeeRequest.getPhoneNumber());

        if (existed != null) {
            return new BaseResponse(409, null, "Nhân viên đã tồn tại", null, null);
        }

        AccountEntity account = accountRepository.findByAccountId(employeeRequest.getAccountId());
        if (account == null) {
            return new BaseResponse(404, null, "Không tìm thấy tài khoản", null, null);
        }

        EmployeeEntity employee = new EmployeeEntity();
        employee.setFirstName(employeeRequest.getFirstName());
        employee.setLastName(employeeRequest.getLastName());
        employee.setPhoneNumber(employeeRequest.getPhoneNumber());
        employee.setPosition(employeeRequest.getPosition());
        employee.setActive(employeeRequest.isActive());
        employee.setAccount(account);
        employee.setCreatedBy(info.getUsername());
        employee.setCreatedAt(LocalDateTime.now());

        employeeRepository.save(employee);

        return new BaseResponse(201, employee, "Thêm nhân viên thành công", null, null);
    }

    public BaseResponse updateEmployee(EmployeeRequest employeeRequest) {
        UserDetails info = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try {
            EmployeeEntity employee = employeeRepository.findByEmployeeId(employeeRequest.getEmployeeId());
            if (employee == null) {
                return new BaseResponse(404, null, "Không tìm thấy nhân viên", null, null);
            } else {
                employee.setFirstName(employeeRequest.getFirstName());
                employee.setLastName(employeeRequest.getLastName());
                employee.setPhoneNumber(employeeRequest.getPhoneNumber());
                employee.setPosition(employeeRequest.getPosition());
                employee.setActive(employeeRequest.isActive());
                employee.setUpdatedBy(info.getUsername());
                employee.setUpdatedAt(LocalDateTime.now());
                employeeRepository.save(employee);
                return new BaseResponse(200, employee, "Cập nhật nhân viên thành công", null, null);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public BaseResponse deleteEmployee(EmployeeRequest employeeRequest) {
        UserDetails info = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        boolean isAdmin = info.getAuthorities()
                .stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

        if (!isAdmin) {
            return new BaseResponse(403, null, "Bạn không có quyền!", null, null);
        }

        EmployeeEntity employee = employeeRepository
                .findByEmployeeId(employeeRequest.getEmployeeId());

        if (employee == null) {
            return new BaseResponse(404, null, "Không tìm thấy nhân viên", null, null);
        }

        employeeRepository.delete(employee);
        return new BaseResponse(200, null, "Xóa nhân viên thành công", null, null);
    }
}
