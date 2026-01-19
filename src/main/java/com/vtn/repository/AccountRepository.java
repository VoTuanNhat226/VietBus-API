package com.vtn.repository;

import com.vtn.entity.AccountEntity;
import com.vtn.entity.EmployeeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, UUID> {
    Optional<AccountEntity> findByUsername(String username);

    @Query("""
    SELECT a
    FROM AccountEntity a
    WHERE (:username IS NULL OR a.username LIKE %:username%)
        AND (:role IS NULL OR a.role = :role)
        AND (:active IS NULL OR a.active = :active)
        AND (:createdBy IS NULL OR a.createdBy LIKE %:createdBy%)
        AND (:updatedBy IS NULL OR a.updatedBy LIKE :updatedBy%)
    """)
    List<AccountEntity> getAllByCondition(
            @Param("username") String username,
            @Param("role") String role,
            @Param("active") Boolean active,
            @Param("createdBy") String createdBy,
            @Param("updatedBy") String updatedBy
    );

    @Query("""
    SELECT a
    FROM AccountEntity a
    WHERE a.accountId = :accountId    
    """)
    AccountEntity findByAccountId(@Param("accountId") UUID accountId);

    @Query("""
        SELECT a
        FROM AccountEntity a
        WHERE a.role = :role
        AND a.active = true
        AND NOT EXISTS (
            SELECT 1
            FROM EmployeeEntity e
            WHERE e.account = a
            )
    """)
    List<AccountEntity> findAccountsNotUsedByEmployee(@Param("role") String role);

    boolean existsByUsername(String username);
}