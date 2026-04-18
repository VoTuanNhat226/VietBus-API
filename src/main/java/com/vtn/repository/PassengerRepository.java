package com.vtn.repository;

import com.vtn.entity.PassengerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PassengerRepository extends JpaRepository<PassengerEntity, UUID> {
    boolean existsByEmail(String email);
}
