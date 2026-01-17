package com.vtn.repository;

import com.vtn.entity.EmployeeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<EmployeeEntity, UUID> {
    EmployeeEntity findByEmployeeId(UUID employeeId);

    @Query("""
    SELECT e
    FROM EmployeeEntity e
    WHERE (:firstName IS NULL OR e.firstName LIKE %:firstName%)
    AND (:lastName IS NULL OR e.lastName LIKE %:lastName%)
    AND (:phoneNumber IS NULL OR e.phoneNumber LIKE %:phoneNumber%)
    AND (:position IS NULL OR e.position = :position)
""")
    List<EmployeeEntity> findByCondition(
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("phoneNumber") String phoneNumber,
            @Param("position") String position
    );
}
