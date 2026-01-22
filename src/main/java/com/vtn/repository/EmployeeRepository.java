package com.vtn.repository;

import com.vtn.entity.EmployeeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
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
        AND (:createdBy IS NULL OR e.createdBy LIKE %:createdBy%)
        AND (:updatedBy IS NULL OR e.updatedBy LIKE :updatedBy%)
        AND (:active IS NULL OR e.active = :active)
    """)
    List<EmployeeEntity> getAllByCondition(
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("phoneNumber") String phoneNumber,
            @Param("position") String position,
            @Param("createdBy") String createdBy,
            @Param("updatedBy") String updatedBy,
            @Param("active") Boolean active
    );

    @Query("""
        SELECT e
        FROM EmployeeEntity e
        WHERE e.active = true
            AND e.position = :position
    """)
    List<EmployeeEntity> getAllEmployeeActiveByPosition(@Param("position") String position);

    @Query("""
        SELECT e
        FROM EmployeeEntity e
        WHERE (e.firstName LIKE %:firstName%)
            AND (e.lastName LIKE %:lastName%)
            AND (e.phoneNumber LIKE %:phoneNumber%)       
    """)
    EmployeeEntity findByFirstNameLastNamePhoneNumber(
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("phoneNumber") String phoneNumber
    );
}
