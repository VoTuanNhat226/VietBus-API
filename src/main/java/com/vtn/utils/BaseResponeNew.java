package com.vtn.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseResponeNew<T> {
    private Integer statusCode;
    private String message;
    private T data;
    private Meta meta;
    private String description;
    private String messageStatus;
    private Long took;
}
