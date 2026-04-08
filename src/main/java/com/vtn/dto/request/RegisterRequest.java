package com.vtn.dto.request;

import com.vtn.enumdef.AccountRoleEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    private String username;
    private String password;
    private boolean active;
    private AccountRoleEnum role;
}
