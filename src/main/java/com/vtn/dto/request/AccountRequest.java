package com.vtn.dto.request;

import com.vtn.enumdef.AccountRoleEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AccountRequest {
    private UUID accountId;
    private String username;
    private AccountRoleEnum role;
    private Boolean active;
    private String createdBy;
    private String updatedBy;
}
