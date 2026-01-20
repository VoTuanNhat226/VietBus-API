package com.vtn.repository;

import com.vtn.entity.VehicleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VehicleRepository extends JpaRepository<VehicleEntity, UUID> {
    VehicleEntity findByVehicleId(UUID vehicleId);

    VehicleEntity findByLicensePlate(String licensePlate);
}
