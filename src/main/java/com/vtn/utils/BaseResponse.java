package com.vtn.utils;

import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@MappedSuperclass
public class BaseResponse implements Serializable {
    // Status code
    Integer statusCode;

    // Generic data field
    Object data;

    // Status description
    String description;

    // Status message
    String messageStatus;

    // Time taken to process the request API (in milliseconds)
    Long took;

    public BaseResponse(Integer statusCode, Object data, String description, String messageStatus, Long took) {
        this.statusCode = statusCode;
        this.data = data;
        this.description = description;
        this.messageStatus = messageStatus;
        this.took = took;
    }
}
