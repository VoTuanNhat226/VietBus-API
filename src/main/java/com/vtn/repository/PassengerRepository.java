package com.vtn.repository;

import com.vtn.entity.PassengerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface PassengerRepository extends JpaRepository<PassengerEntity, UUID> {
    boolean existsByEmail(String email);

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
}
