package com.vtn.repository;

import com.vtn.entity.PassengerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PassengerRepository extends JpaRepository<PassengerEntity, UUID> {
}
