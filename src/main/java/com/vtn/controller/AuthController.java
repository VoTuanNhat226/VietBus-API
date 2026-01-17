package com.vtn.controller;

import com.vtn.constant.APIConstants;
import com.vtn.dto.request.LoginRequest;
import com.vtn.dto.request.RegisterRequest;
import com.vtn.dto.response.LoginResponse;
import com.vtn.entity.AccountEntity;
import com.vtn.repository.AccountRepository;
import com.vtn.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          AccountRepository accountRepository,
                          PasswordEncoder passwordEncoder, UserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
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

            // Lấy thông tin user sau khi authenticate
            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(request.getUsername());

            // Lấy role (giả sử mỗi user 1 role)
            String role = userDetails.getAuthorities()
                    .stream()
                    .findFirst()
                    .map(GrantedAuthority::getAuthority)
                    .orElse(null);

            // Thêm role vào JWT
            Map<String, Object> claims = new HashMap<>();
            claims.put("role", role);

            String token = jwtService.generateToken(claims, request.getUsername());

            return ResponseEntity.ok(new LoginResponse(token));

        } catch (Exception ex) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
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

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", account.getRole());

        String token = jwtService.generateToken(claims, account.getUsername());

        return ResponseEntity.ok(new LoginResponse(token));
    }
}
