package com.vtn.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Mỗi lần login tạo ra 1 family.
 * Mỗi lần refresh → version tăng lên.
 * Nếu version không khớp → Reuse Detected → revoke toàn bộ family.
 */
@Entity
@Table(name = "refresh_token_families")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenFamily {

    @Id
    @Column(name = "family_id", updatable = false, nullable = false)
    private String familyId; // UUID string

    @Column(name = "username", nullable = false)
    private String username;

    /**
     * Version hiện tại hợp lệ.
     * Refresh token được issue với version này.
     * Sau khi rotate: latestVersion++
     */
    @Column(name = "latest_version", nullable = false)
    private int latestVersion;

    /**
     * True nếu family đã bị revoke (reuse detected hoặc logout).
     */
    @Column(name = "revoked", nullable = false)
    private boolean revoked;

    /**
     * Thời điểm family hết hạn (= refresh token dài nhất có thể sống).
     */
    @Column(name = "expiry_date", nullable = false)
    private Instant expiryDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (familyId == null) familyId = UUID.randomUUID().toString();
        if (createdAt == null) createdAt = Instant.now();
    }
}