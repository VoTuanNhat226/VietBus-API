package com.vtn.service;

import com.vtn.entity.RefreshTokenFamily;
import com.vtn.exception.TokenReuseException;
import com.vtn.repository.RefreshTokenFamilyRepository;
import com.vtn.security.JwtService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenFamilyRepository familyRepository;
    private final JwtService jwtService;

    /**
     * Tạo family mới khi user login.
     * Version bắt đầu từ 0.
     */
    @Transactional
    public String issueRefreshToken(String username) {
        // Revoke toàn bộ family cũ trước khi tạo mới
        familyRepository.revokeAllByUsername(username);

        String familyId = UUID.randomUUID().toString();
        int initialVersion = 0;

        RefreshTokenFamily family = RefreshTokenFamily.builder()
                .familyId(familyId)
                .username(username)
                .latestVersion(initialVersion)
                .revoked(false)
                .expiryDate(Instant.now().plusMillis(jwtService.getRefreshTokenExpirationMs()))
                .build();

        familyRepository.save(family);

        return jwtService.generateRefreshToken(username, familyId, initialVersion);
    }

    /**
     * Rotate refresh token:
     * 1. Validate token signature & expiry
     * 2. Load family từ DB
     * 3. Kiểm tra version khớp → nếu không: REUSE DETECTED → revoke
     * 4. Kiểm tra family chưa bị revoke
     * 5. Kiểm tra family chưa hết hạn
     * 6. Tăng version, lưu DB, trả token mới
     *
     * @return refreshToken mới (đã rotate)
     * @throws TokenReuseException nếu phát hiện reuse
     * @throws IllegalStateException nếu family revoked hoặc expired
     */
    @Transactional
    public RotateResult rotate(String oldRefreshToken) {
        // Bước 1: Parse claims (throws nếu chữ ký sai / expired JWT)
        TokenClaims claims = extractClaims(oldRefreshToken);

        // Bước 2: Load family
        RefreshTokenFamily family = familyRepository.findByFamilyId(claims.familyId())
                .orElseThrow(() -> new IllegalArgumentException("Unknown token family"));

        // Bước 3: Kiểm tra version (REUSE DETECTED nếu không khớp)
        validateVersionNotReused(claims, family);

        // Bước 4: Kiểm tra revoked
        validateNotRevoked(family);

        // Bước 5: Kiểm tra family expiry
        validateNotExpired(family);

        // Bước 6: Rotate — tăng version, issue token mới
        int newVersion = family.getLatestVersion() + 1;
        family.setLatestVersion(newVersion);
        familyRepository.save(family);

        String newRefreshToken = jwtService.generateRefreshToken(claims.username(), claims.familyId(), newVersion);

        return new RotateResult(claims.username(), newRefreshToken);
    }

    /**
     * Logout thiết bị hiện tại: revoke family này.
     */
    @Transactional
    public void revokeFamily(String refreshToken) {
        try {
            String familyId = jwtService.extractFamilyId(refreshToken);
            familyRepository.revokeByFamilyId(familyId);
        } catch (Exception ignored) {
            // Token không hợp lệ → bỏ qua, coi như đã logout
        }
    }

    /**
     * Logout tất cả thiết bị: revoke tất cả family của user.
     */
    @Transactional
    public void revokeAllFamilies(String username) {
        familyRepository.revokeAllByUsername(username);
    }

    public record RotateResult(String username, String newRefreshToken) {}

    private record TokenClaims(String familyId, int version, String username) {}

    @Scheduled(cron = "0 0 3 * * *") // Chạy lúc 3AM mỗi ngày
    @Transactional
    public void cleanupExpiredFamilies() {
        familyRepository.deleteByRevokedTrueOrExpiryDateBefore(Instant.now());
        log.info("Cleaned up expired/revoked refresh token families");
    }

    // ------------------ validate ------------------
    private TokenClaims extractClaims(String refreshToken) {
        try {
            String familyId = jwtService.extractFamilyId(refreshToken);
            int tokenVersion = jwtService.extractVersion(refreshToken);
            String username = jwtService.extractUsername(refreshToken);
            return new TokenClaims(familyId, tokenVersion, username);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid refresh token: " + e.getMessage());
        }
    }

    private void validateVersionNotReused(TokenClaims claims, RefreshTokenFamily family) {
        if (claims.version() != family.getLatestVersion()) {
            // REUSE DETECTED — token cũ bị tái sử dụng
            log.warn("Refresh token reuse detected! user={}, familyId={}, tokenVersion={}, latestVersion={}",
                    claims.username(), claims.familyId(), claims.version(), family.getLatestVersion());
            family.setRevoked(true);
            familyRepository.save(family);
            throw new TokenReuseException("Refresh token reuse detected. All sessions revoked.");
        }
    }

    private void validateNotRevoked(RefreshTokenFamily family) {
        if (family.isRevoked()) {
            throw new IllegalStateException("Token family has been revoked. Please login again.");
        }
    }

    private void validateNotExpired(RefreshTokenFamily family) {
        if (Instant.now().isAfter(family.getExpiryDate())) {
            throw new IllegalStateException("Session expired. Please login again.");
        }
    }
}
