package com.vtn.repository;

import com.vtn.entity.StationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface StationRepository extends JpaRepository<StationEntity, UUID> {

    @Query("""
        SELECT s
        FROM StationEntity s
        WHERE s.stationId = :stationId    
    """)
    StationEntity findByStationId(UUID stationId);
}
