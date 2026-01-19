package com.vtn.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountRequest {
    private String accountId;
    private String username;
    private String role;
    private Boolean active;
    private String createdBy;
    private String updatedBy;
}
