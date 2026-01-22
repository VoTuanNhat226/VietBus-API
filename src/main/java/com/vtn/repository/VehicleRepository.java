package com.vtn.repository;

import com.vtn.entity.VehicleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface VehicleRepository extends JpaRepository<VehicleEntity, UUID> {
    VehicleEntity findByVehicleId(UUID vehicleId);

    VehicleEntity findByLicensePlate(String licensePlate);

    @Query("""
        SELECT v
        FROM VehicleEntity v
        WHERE v.active = true
    """)
    List<VehicleEntity> findAllVehiclesActive();
}
