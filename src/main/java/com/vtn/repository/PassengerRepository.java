package com.vtn.repository;

import com.vtn.entity.PassengerEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface PassengerRepository extends JpaRepository<PassengerEntity, UUID> {
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByIdCardNumber(String idCardNumber);

    @Query("""
        SELECT COUNT(p)
        FROM PassengerEntity p
        WHERE p.createdAt >= :startOfMonth
            AND p.createdAt < :endOfMonth
    """)
    BigDecimal countPassengerByMonth(
            LocalDateTime startOfMonth,
            LocalDateTime endOfMonth
    );

    @Query("""
        SELECT p FROM PassengerEntity p
        WHERE (:fullName IS NULL OR LOWER(p.fullName) LIKE LOWER(CONCAT('%', CAST(:fullName AS string), '%')))
        AND (:phoneNumber IS NULL OR p.phoneNumber LIKE CONCAT('%', CAST(:phoneNumber AS string), '%'))
        AND (:email IS NULL OR LOWER(p.email) LIKE LOWER(CONCAT('%', CAST(:email AS string), '%')))
        AND (:idCardNumber IS NULL OR p.idCardNumber LIKE CONCAT('%', CAST(:idCardNumber AS string), '%'))
        """)
    Page<PassengerEntity> searchPassengers(
            @Param("fullName") String fullName,
            @Param("phoneNumber") String phoneNumber,
            @Param("email") String email,
            @Param("idCardNumber") String idCardNumber,
            Pageable pageable
    );
}
