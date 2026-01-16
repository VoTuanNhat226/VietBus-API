package com.vtn.repository;

import com.vtn.entity.BusEntity;
import com.vtn.entity.EmployeeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BusRepository extends JpaRepository<BusEntity, UUID> {
    BusEntity findByBusId(UUID busId);
}
