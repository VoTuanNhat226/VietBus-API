package com.vtn.repository;

import com.vtn.entity.VehicleMaintenanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface VehicleMaintenanceRepository extends JpaRepository<VehicleMaintenanceEntity, UUID> {

    @Query("""
        SELECT m
        FROM VehicleMaintenanceEntity m
        WHERE m.vehicle.vehicleId = :vehicleId
        ORDER BY m.maintenanceDate DESC
    """)
    List<VehicleMaintenanceEntity> findByVehicleId(@Param("vehicleId") UUID vehicleId);

    @Query("""
        SELECT m
        FROM VehicleMaintenanceEntity m
        WHERE m.id = :id
    """)
    VehicleMaintenanceEntity findByMaintenanceId(@Param("id") UUID id);
}
