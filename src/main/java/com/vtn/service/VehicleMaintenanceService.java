package com.vtn.service;

import com.vtn.dto.request.VehicleMaintenanceRequest;
import com.vtn.entity.VehicleEntity;
import com.vtn.entity.VehicleMaintenanceEntity;
import com.vtn.enumdef.VehicleMaintenanceStatusEnum;
import com.vtn.repository.VehicleMaintenanceRepository;
import com.vtn.repository.VehicleRepository;
import com.vtn.utils.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class VehicleMaintenanceService {
    private final VehicleMaintenanceRepository vehicleMaintenanceRepository;
    private final VehicleRepository vehicleRepository;

    @Autowired
    public VehicleMaintenanceService(VehicleMaintenanceRepository vehicleMaintenanceRepository,
                                      VehicleRepository vehicleRepository) {
        this.vehicleMaintenanceRepository = vehicleMaintenanceRepository;
        this.vehicleRepository = vehicleRepository;
    }

    public BaseResponse getByVehicleId(VehicleMaintenanceRequest request) {
        BaseResponse vehicleIdError = validateVehicleIdRequired(request);
        if (vehicleIdError != null) {
            return vehicleIdError;
        }

        VehicleEntity vehicle = vehicleRepository.findByVehicleId(request.getVehicleId());
        BaseResponse vehicleExistsError = validateVehicleExists(vehicle);
        if (vehicleExistsError != null) {
            return vehicleExistsError;
        }

        List<VehicleMaintenanceEntity> maintenanceHistory =
                vehicleMaintenanceRepository.findByVehicleId(request.getVehicleId());

        return new BaseResponse(200, maintenanceHistory, "Get vehicle maintenance history successful", null, null);
    }

    @Transactional
    public BaseResponse createMaintenance(VehicleMaintenanceRequest request) {
        UserDetails info = getInfo();

        String validationError = validate(request, true);
        if (validationError != null) {
            return new BaseResponse(400, null, validationError, null, null);
        }

        VehicleEntity vehicle = vehicleRepository.findByVehicleId(request.getVehicleId());
        BaseResponse vehicleExistsError = validateVehicleExists(vehicle);
        if (vehicleExistsError != null) {
            return vehicleExistsError;
        }

        VehicleMaintenanceEntity maintenance = new VehicleMaintenanceEntity();
        maintenance.setVehicle(vehicle);
        mapRequestToEntity(request, maintenance);
        maintenance.setCreatedBy(info.getUsername());
        maintenance.setCreatedAt(LocalDateTime.now());

        vehicleMaintenanceRepository.save(maintenance);

        return new BaseResponse(201, maintenance, "Create vehicle maintenance successful", null, null);
    }

    @Transactional
    public BaseResponse updateMaintenance(VehicleMaintenanceRequest request) {
        UserDetails info = getInfo();

        BaseResponse idRequiredError = validateMaintenanceIdRequired(request);
        if (idRequiredError != null) {
            return idRequiredError;
        }

        VehicleMaintenanceEntity maintenance = vehicleMaintenanceRepository.findByMaintenanceId(request.getId());
        BaseResponse maintenanceExistsError = validateMaintenanceExists(maintenance);
        if (maintenanceExistsError != null) {
            return maintenanceExistsError;
        }

        String validationError = validate(request, false);
        if (validationError != null) {
            return new BaseResponse(400, null, validationError, null, null);
        }

        // Allow re-assigning to another vehicle if vehicleId is provided and different
        if (request.getVehicleId() != null && !request.getVehicleId().equals(maintenance.getVehicle().getVehicleId())) {
            VehicleEntity vehicle = vehicleRepository.findByVehicleId(request.getVehicleId());
            BaseResponse vehicleExistsError = validateVehicleExists(vehicle);
            if (vehicleExistsError != null) {
                return vehicleExistsError;
            }
            maintenance.setVehicle(vehicle);
        }

        mapRequestToEntity(request, maintenance);
        maintenance.setUpdatedBy(info.getUsername());
        maintenance.setUpdatedAt(LocalDateTime.now());

        vehicleMaintenanceRepository.save(maintenance);

        return new BaseResponse(200, maintenance, "Update vehicle maintenance successful", null, null);
    }

    // ------------------ validate ------------------
    private BaseResponse validateVehicleIdRequired(VehicleMaintenanceRequest request) {
        if (request.getVehicleId() == null) {
            return new BaseResponse(400, null, "VehicleId is required", null, null);
        }
        return null;
    }

    private BaseResponse validateMaintenanceIdRequired(VehicleMaintenanceRequest request) {
        if (request.getId() == null) {
            return new BaseResponse(400, null, "Id is required", null, null);
        }
        return null;
    }

    private BaseResponse validateVehicleExists(VehicleEntity vehicle) {
        if (vehicle == null) {
            return new BaseResponse(404, null, "Vehicle not found", null, null);
        }
        return null;
    }

    private BaseResponse validateMaintenanceExists(VehicleMaintenanceEntity maintenance) {
        if (maintenance == null) {
            return new BaseResponse(404, null, "Vehicle maintenance record not found", null, null);
        }
        return null;
    }

    /**
     * Validate a create/update request.
     * @param request        the incoming request
     * @param requireVehicle when true, vehicleId is mandatory (used for create)
     * @return an error message, or null if the request is valid
     */
    private String validate(VehicleMaintenanceRequest request, boolean requireVehicle) {
        if (requireVehicle && request.getVehicleId() == null) {
            return "VehicleId is required";
        }

        if (request.getMaintenanceType() == null) {
            return "MaintenanceType is required";
        }

        if (request.getStatus() == null) {
            return "Status is required";
        }

        if (request.getMaintenanceDate() == null) {
            return "MaintenanceDate is required";
        }

        if (request.getStatus() == VehicleMaintenanceStatusEnum.COMPLETED
                && request.getMaintenanceDate().isAfter(LocalDate.now())) {
            return "MaintenanceDate cannot be in the future for a completed maintenance";
        }

        if (request.getOdometerKm() != null && request.getOdometerKm() < 0) {
            return "OdometerKm cannot be negative";
        }

        if (request.getCost() != null && request.getCost().signum() < 0) {
            return "Cost cannot be negative";
        }

        if (request.getNextMaintenanceDate() != null
                && !request.getNextMaintenanceDate().isAfter(request.getMaintenanceDate())) {
            return "NextMaintenanceDate must be after MaintenanceDate";
        }

        if (request.getNextMaintenanceKm() != null) {
            if (request.getNextMaintenanceKm() < 0) {
                return "NextMaintenanceKm cannot be negative";
            }
            if (request.getOdometerKm() != null && request.getNextMaintenanceKm() <= request.getOdometerKm()) {
                return "NextMaintenanceKm must be greater than OdometerKm";
            }
        }

        return null;
    }

    // ------------------ helper ------------------
    private void mapRequestToEntity(VehicleMaintenanceRequest request, VehicleMaintenanceEntity maintenance) {
        maintenance.setMaintenanceType(request.getMaintenanceType());
        maintenance.setStatus(request.getStatus());
        maintenance.setMaintenanceDate(request.getMaintenanceDate());
        maintenance.setDescription(request.getDescription());
        maintenance.setOdometerKm(request.getOdometerKm());
        maintenance.setCost(request.getCost());
        maintenance.setGarageName(request.getGarageName());
        maintenance.setPerformedBy(request.getPerformedBy());
        maintenance.setNextMaintenanceDate(request.getNextMaintenanceDate());
        maintenance.setNextMaintenanceKm(request.getNextMaintenanceKm());
        maintenance.setInvoiceUrl(request.getInvoiceUrl());
    }

    private UserDetails getInfo() {
        return (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
