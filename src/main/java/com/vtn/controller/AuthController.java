package com.vtn.controller;

import com.vtn.constant.APIConstants;
import com.vtn.dto.request.LoginRequest;
import com.vtn.dto.request.RegisterRequest;
import com.vtn.dto.response.LoginResponse;
import com.vtn.entity.AccountEntity;
import com.vtn.repository.AccountRepository;
import com.vtn.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          AccountRepository accountRepository,
                          PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping(APIConstants.API_LOGIN)
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            String token = jwtService.generateToken(request.getUsername());
            return ResponseEntity.ok(new LoginResponse(token));

        } catch (Exception ex) {
            return ResponseEntity
                    .status(401)
                    .body(new LoginResponse("Invalid username or password"));
        }
    }

    @PostMapping(value = APIConstants.API_REGISTER)
    public ResponseEntity<LoginResponse> register(@RequestBody RegisterRequest request) {

        if (accountRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new LoginResponse("Username already exists"));
        }

        AccountEntity account = new AccountEntity();
        account.setUsername(request.getUsername());
        account.setPassword(passwordEncoder.encode(request.getPassword()));
        account.setRole("STAFF");
        account.setActive(true);
        account.setCreated_at(LocalDateTime.now());
        account.setCreated_by("SYSTEM");

        accountRepository.save(account);

        String token = jwtService.generateToken(account.getUsername());
        return ResponseEntity.ok(new LoginResponse(token));
    }
}
