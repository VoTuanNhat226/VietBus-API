package com.vtn.repository;

import com.vtn.entity.EmployeeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<EmployeeEntity, UUID> {
    EmployeeEntity findByEmployeeId(UUID employeeId);
}
