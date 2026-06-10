package com.vtn.service;

import com.vtn.constant.APIConstants;
import com.vtn.dto.request.LoginRequest;
import com.vtn.dto.request.RegisterRequest;
import com.vtn.dto.response.LoginResponse;
import com.vtn.dto.response.RefreshResponse;
import com.vtn.entity.AccountEntity;
import com.vtn.exception.TokenReuseException;
import com.vtn.repository.AccountRepository;
import com.vtn.security.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;
    private final RefreshTokenService refreshTokenService;

    private static final String REFRESH_TOKEN_COOKIE = "refresh_token";
    private static final int COOKIE_MAX_AGE_SECONDS = 24 * 60 * 60;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       AccountRepository accountRepository,
                       PasswordEncoder passwordEncoder,
                       UserDetailsService userDetailsService,
                       RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
        this.refreshTokenService = refreshTokenService;
    }

    public ResponseEntity<LoginResponse> login(LoginRequest request, HttpServletResponse response) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()));

            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());

            String role = userDetails.getAuthorities()
                    .stream()
                    .findFirst()
                    .map(GrantedAuthority::getAuthority)
                    .orElse(null);

            Map<String, Object> claims = new HashMap<>();
            claims.put("role", role);

            String accessToken = jwtService.generateAccessToken(claims, request.getUsername());

            String refreshToken = refreshTokenService.issueRefreshToken(request.getUsername());
            setRefreshTokenCookie(response, refreshToken);

            return ResponseEntity.ok(new LoginResponse(accessToken));

        } catch (Exception ex) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse("Invalid username or password"));
        }
    }

    public ResponseEntity<?> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshTokenFromCookie(request);

        if (refreshToken == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse("No refresh token found"));
        }

        try {
            RefreshTokenService.RotateResult result = refreshTokenService.rotate(refreshToken);

            UserDetails userDetails = userDetailsService.loadUserByUsername(result.username());

            String role = userDetails.getAuthorities()
                    .stream()
                    .findFirst()
                    .map(GrantedAuthority::getAuthority)
                    .orElse(null);

            Map<String, Object> claims = new HashMap<>();
            claims.put("role", role);

            String newAccessToken = jwtService.generateAccessToken(claims, result.username());

            setRefreshTokenCookie(response, result.newRefreshToken());

            return ResponseEntity.ok(new RefreshResponse(newAccessToken));

        } catch (TokenReuseException e) {
            clearRefreshTokenCookie(response);
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new LoginResponse("Security violation detected. Please login again."));

        } catch (IllegalArgumentException | IllegalStateException e) {
            clearRefreshTokenCookie(response);
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse(e.getMessage()));
        }
    }

    public ResponseEntity<LoginResponse> logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshTokenFromCookie(request);

        if (refreshToken != null) {
            refreshTokenService.revokeFamily(refreshToken);
        }

        clearRefreshTokenCookie(response);

        return ResponseEntity.ok(new LoginResponse("Logged out successfully"));
    }

    public ResponseEntity<LoginResponse> logoutAll(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshTokenFromCookie(request);

        if (refreshToken != null) {
            try {
                String username = jwtService.extractUsername(refreshToken);
                refreshTokenService.revokeAllFamilies(username);
            } catch (Exception ignored) {}
        }

        clearRefreshTokenCookie(response);

        return ResponseEntity.ok(new LoginResponse("Logged out from all devices"));
    }

    public ResponseEntity<LoginResponse> register(RegisterRequest request) {
        if (accountRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new LoginResponse("Tên đăng nhập đã được sử dụng!"));
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse("Bạn chưa đăng nhập"));
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new LoginResponse("Bạn không có quyền tạo tài khoản"));
        }

        UserDetails info = (UserDetails) authentication.getPrincipal();

        AccountEntity account = new AccountEntity();
        account.setUsername(request.getUsername());
        account.setPassword(passwordEncoder.encode(request.getPassword()));
        account.setRole(request.getRole());
        account.setActive(request.isActive());
        account.setCreatedAt(LocalDateTime.now());
        account.setCreatedBy(info.getUsername());

        accountRepository.save(account);

        return ResponseEntity.ok(new LoginResponse(null));
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE, token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath(APIConstants.API_REFRESH_TOKEN);
        cookie.setMaxAge(COOKIE_MAX_AGE_SECONDS);
        response.addCookie(cookie);
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath(APIConstants.API_REFRESH_TOKEN);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null)return null;

        return Arrays.stream(request.getCookies())
                .filter(c -> REFRESH_TOKEN_COOKIE.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
