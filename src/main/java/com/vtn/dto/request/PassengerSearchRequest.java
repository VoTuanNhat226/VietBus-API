package com.vtn.dto.request;

import lombok.Data;

@Data
public class PassengerSearchRequest {
    private String fullName;
    private String phoneNumber;
    private String email;
    private String idCardNumber;
    private int page = 0;
    private int size = 10;
}