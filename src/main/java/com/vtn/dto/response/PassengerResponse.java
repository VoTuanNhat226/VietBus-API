package com.vtn.dto.response;

import lombok.Data;

import java.util.UUID;

@Data
public class PassengerResponse {
    private UUID passengerId;
    private String fullName;
    private String phoneNumber;
    private String email;
    private String idCardNumber;
    private String note;
    private String createdBy;
    private String updatedBy;
}
