package com.vtn.repository;

import com.vtn.entity.VehicleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface VehicleRepository extends JpaRepository<VehicleEntity, UUID> {
    VehicleEntity findByLicensePlate(String licensePlate);

    @Query("""
        SELECT v
        FROM VehicleEntity v
        WHERE v.vehicleId = :vehicleId
    """)
    VehicleEntity findByVehicleId(@Param("vehicleId") UUID vehicleId);

    @Query("""
        SELECT v
        FROM VehicleEntity v
        WHERE v.active = true
    """)
    List<VehicleEntity> findAllVehiclesActive();

    @Query("""
        SELECT COUNT(v)
        FROM VehicleEntity v
        WHERE v.active = :status
    """)
    BigDecimal countVehiclesActiveByStatus(@Param("status") Boolean status);
}
