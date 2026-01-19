package com.vtn.controller;

import com.vtn.constant.APIConstants;
import com.vtn.dto.request.AccountRequest;
import com.vtn.service.AccountDetailsService;
import com.vtn.utils.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccountController {
    private final AccountDetailsService accountDetailsService;

    @Autowired
    public AccountController(AccountDetailsService accountDetailsService) {
        this.accountDetailsService = accountDetailsService;
    }

    @PostMapping(value = APIConstants.API_GET_ALL_ACCOUNT)
    public ResponseEntity<BaseResponse> getAllAccount(@RequestBody AccountRequest accountRequest) {
        long begin = System.currentTimeMillis();
        BaseResponse response = accountDetailsService.getAllAccounts(accountRequest);
        response.setTook(System.currentTimeMillis() - begin);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(value = APIConstants.API_GET_ALL_ACCOUNT_BY_ROLE)
    public ResponseEntity<BaseResponse> getAllByRole(@RequestBody AccountRequest request) {
        long beginTime = System.currentTimeMillis();
        BaseResponse response = accountDetailsService.getAllAccountsByRole(request);
        response.setTook(System.currentTimeMillis() - beginTime);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
