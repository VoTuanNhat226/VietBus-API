package com.vtn.repository;

import com.vtn.entity.SeatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface SeatRepository extends JpaRepository<SeatEntity, UUID> {

    @Query("""
        SELECT s
        FROM SeatEntity s
        WHERE s.vehicle.vehicleId = :vehicleId
    """)
    List<SeatEntity> findByVehicleId(UUID vehicleId);
}
