package com.vtn.utils;

import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@MappedSuperclass
public class BaseResponse {
    // Status code
    private Integer statusCode;

    // Generic data field
    private Object data;

    // Status description
    private String description;

    // Status message
    private String messageStatus;

    // Time taken to process the request API (in milliseconds)
    private Long took;

    public BaseResponse(Integer statusCode, Object data, String description, String messageStatus, Long took) {
        this.statusCode = statusCode;
        this.data = data;
        this.description = description;
        this.messageStatus = messageStatus;
        this.took = took;
    }
}
