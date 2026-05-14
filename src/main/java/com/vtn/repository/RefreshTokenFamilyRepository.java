package com.vtn.repository;

import com.vtn.entity.RefreshTokenFamily;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenFamilyRepository extends JpaRepository<RefreshTokenFamily, String> {
    Optional<RefreshTokenFamily> findByFamilyId(String familyId);

    List<RefreshTokenFamily> findAllByUsername(String username);

    /**
     * Revoke toàn bộ family của 1 user (dùng khi logout all devices).
     */
    @Modifying
    @Query("UPDATE RefreshTokenFamily f SET f.revoked = true WHERE f.username = :username")
    void revokeAllByUsername(String username);

    /**
     * Revoke 1 family cụ thể (dùng khi reuse detected hoặc logout thiết bị đó).
     */
    @Modifying
    @Query("UPDATE RefreshTokenFamily f SET f.revoked = true WHERE f.familyId = :familyId")
    void revokeByFamilyId(String familyId);

    void deleteByRevokedTrueOrExpiryDateBefore(Instant date);
}
