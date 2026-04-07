package com.vtn.dto.request;

import com.vtn.enumdef.EmployeePositionEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class EmployeeRequest {
    private UUID employeeId;
    private String fullName;
    private String phoneNumber;
    private EmployeePositionEnum position;
    private Boolean active;
    private String createdBy;
    private LocalDate createdAt;
    private String updatedBy;
    private LocalDate updatedAt;

    private UUID accountId;
}
