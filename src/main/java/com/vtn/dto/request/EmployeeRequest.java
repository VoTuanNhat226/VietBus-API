package com.vtn.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class EmployeeRequest {
    private UUID employeeId;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String position;
    private boolean active;
    private UUID accountId;
}
