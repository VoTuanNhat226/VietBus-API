package com.vtn.repository;

import com.vtn.entity.EmployeeEntity;
import com.vtn.enumdef.EmployeePositionEnum;
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
        WHERE (:fullName IS NULL OR e.fullName LIKE %:fullName%)
            AND (:phoneNumber IS NULL OR e.phoneNumber LIKE %:phoneNumber%)
            AND (:position IS NULL OR e.position = :position)
            AND (:createdBy IS NULL OR e.createdBy LIKE %:createdBy%)
            AND (:updatedBy IS NULL OR e.updatedBy LIKE :updatedBy%)
            AND (:active IS NULL OR e.active = :active)
    """)
    List<EmployeeEntity> getAllByCondition(
            @Param("fullName") String fullName,
            @Param("phoneNumber") String phoneNumber,
            @Param("position") EmployeePositionEnum position,
            @Param("createdBy") String createdBy,
            @Param("updatedBy") String updatedBy,
            @Param("active") Boolean active
    );

    @Query("""
        SELECT e
        FROM EmployeeEntity e
        WHERE e.position = :position
            AND e.active = true
    """)
    List<EmployeeEntity> getAllEmployeeActiveByPosition(@Param("position") EmployeePositionEnum position);

    @Query("""
        SELECT e
        FROM EmployeeEntity e
        WHERE e.fullName = :fullName
            AND e.phoneNumber = :phoneNumber
    """)
    EmployeeEntity findByFullNameAndPhoneNumber(
            @Param("fullName") String fullName,
            @Param("phoneNumber") String phoneNumber
    );
}
