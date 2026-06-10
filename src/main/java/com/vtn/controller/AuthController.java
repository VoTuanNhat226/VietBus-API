package com.vtn.controller;

import com.vtn.constant.APIConstants;
import com.vtn.dto.request.LoginRequest;
import com.vtn.dto.request.RegisterRequest;
import com.vtn.dto.response.LoginResponse;
import com.vtn.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping(APIConstants.API_LOGIN)
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        return authService.login(request, response);
    }

    @PostMapping(APIConstants.API_REFRESH_TOKEN)
    public ResponseEntity<?> refresh(HttpServletRequest request, HttpServletResponse response) {
        return authService.refresh(request, response);
    }

    @PostMapping(APIConstants.API_LOGOUT)
    public ResponseEntity<LoginResponse> logout(HttpServletRequest request, HttpServletResponse response) {
        return authService.logout(request, response);
    }

    @PostMapping(APIConstants.API_LOGOUT_ALL)
    public ResponseEntity<LoginResponse> logoutAll(HttpServletRequest request, HttpServletResponse response) {
        return authService.logoutAll(request, response);
    }

    @PostMapping(APIConstants.API_CREATE_ACCOUNT)
    public ResponseEntity<LoginResponse> register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }
}