package com.vtn.repository;

import com.vtn.entity.TripSeatEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TripSeatRepository extends JpaRepository<TripSeatEntity, UUID> {
}
