package com.vtn.controller;

import com.vtn.constant.APIConstants;
import com.vtn.dto.request.EmployeeRequest;
import com.vtn.service.EmployeeService;
import com.vtn.utils.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmployeeController {
    private final EmployeeService employeeService;

    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping(value = APIConstants.API_GET_ALL_EMPLOYEES)
    public ResponseEntity<BaseResponse> getAll(@RequestBody EmployeeRequest request) {
        long beginTime = System.currentTimeMillis();
        BaseResponse response = employeeService.getAllEmployees(request);
        response.setTook(System.currentTimeMillis() - beginTime);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(value = APIConstants.API_GET_ALL_EMPLOYEES_BY_POSITION)
    public ResponseEntity<BaseResponse> getAllByPosition(@RequestBody EmployeeRequest request) {
        long beginTime = System.currentTimeMillis();
        BaseResponse response = employeeService.getAllEmployeeActiveByPosition(request);
        response.setTook(System.currentTimeMillis() - beginTime);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(APIConstants.API_CREATE_EMPLOYEE)
    public ResponseEntity<BaseResponse> create(@RequestBody EmployeeRequest request) {
        long beginTime = System.currentTimeMillis();
        BaseResponse response = employeeService.createEmployee(request);
        response.setTook(System.currentTimeMillis() - beginTime);

        return ResponseEntity
                .status(response.getStatusCode())
                .body(response);
    }

    @PostMapping(value = APIConstants.API_UPDATE_EMPLOYEE)
    public ResponseEntity<BaseResponse> update(@RequestBody EmployeeRequest request) {
        long beginTime = System.currentTimeMillis();
        BaseResponse response = employeeService.updateEmployee(request);
        response.setTook(System.currentTimeMillis() - beginTime);

        return ResponseEntity
                .status(response.getStatusCode())
                .body(response);
    }

    @PostMapping(value = APIConstants.API_DELETE_EMPLOYEE)
    public ResponseEntity<BaseResponse> delete(@RequestBody EmployeeRequest request) {
        long beginTime = System.currentTimeMillis();
        BaseResponse response = employeeService.deleteEmployee(request);
        response.setTook(System.currentTimeMillis() - beginTime);

        return ResponseEntity
                .status(response.getStatusCode())
                .body(response);
    }
}
