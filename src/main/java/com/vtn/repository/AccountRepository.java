package com.vtn.repository;

import com.vtn.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, UUID> {
    Optional<AccountEntity> findByUsername(String username);

    @Query("""
    SELECT a
    FROM AccountEntity a
    WHERE a.account_id = :account_id    
    """)
    AccountEntity findByAccountId(@Param("account_id") UUID account_id);

    boolean existsByUsername(String username);
}