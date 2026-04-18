package com.vtn.dto.request;

import lombok.Data;
import java.util.UUID;

@Data
public class PassengerRequest {
    private UUID passengerId;
    private String fullName;
    private String phoneNumber;
    private String email;
    private String idCardNumber;
    private String note;
    private String createdBy;
    private String updatedBy;
}
