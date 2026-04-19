package com.vtn.dto.request.PassengerRequest;

import lombok.Data;

@Data
public class CreatePassengerRequest {
    private String fullName;
    private String phoneNumber;
    private String email;
    private String idCardNumber;
    private String note;
    private String createdBy;
    private String updatedBy;
}
