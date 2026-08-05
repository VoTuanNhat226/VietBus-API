package com.vtn.service;

import com.vtn.dto.request.VehicleRequest;
import com.vtn.entity.VehicleEntity;
import com.vtn.entity.SeatEntity;
import com.vtn.repository.VehicleRepository;
import com.vtn.repository.SeatRepository;
import com.vtn.utils.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class VehicleService {
    private final VehicleRepository vehicleRepository;
    private final SeatRepository seatRepository;

    @Autowired
    public VehicleService(VehicleRepository vehicleRepository,
                          SeatRepository seatRepository) {
        this.vehicleRepository = vehicleRepository;
        this.seatRepository = seatRepository;
    }

    public BaseResponse getAllVehicles() {
        List<VehicleEntity> vehicles = vehicleRepository.findAll();
        return new BaseResponse(200, vehicles, "Get all vehicle successful", null, null);
    }

    public BaseResponse getAllVehiclesActive() {
        List<VehicleEntity> vehicles = vehicleRepository.findAllVehiclesActive();
        return new BaseResponse(200, vehicles, "Get all vehicle active successful", null, null);
    }

    @Cacheable(value = "vehicle", key = "#request.vehicleId")
    public BaseResponse getVehicleById(VehicleRequest request) {
        VehicleEntity vehicle = vehicleRepository.findByVehicleId(request.getVehicleId());
        return new BaseResponse(200, vehicle, "Get vehicle successful", null, null);
    }

    @Transactional
    public BaseResponse createVehicle(VehicleRequest request) {
        UserDetails info = getInfo();

        BaseResponse permissionError = validateIsAdmin(info);
        if (permissionError != null) {
            return permissionError;
        }

        BaseResponse requiredFieldsError = validateRequiredFields(request);
        if (requiredFieldsError != null) {
            return requiredFieldsError;
        }

        BaseResponse duplicateError = validateLicensePlateNotDuplicate(request);
        if (duplicateError != null) {
            return duplicateError;
        }

        // ===== CREATE VEHICLE =====
        VehicleEntity vehicle = new VehicleEntity();
        vehicle.setLicensePlate(request.getLicensePlate());
        vehicle.setTotalSeat(request.getTotalSeat());
        vehicle.setActive(request.getActive());
        vehicle.setModel(request.getModel());
        vehicle.setManufactureYear(request.getManufactureYear());
        vehicle.setTotalKm(0);
        vehicle.setCreatedBy(info.getUsername());
        vehicle.setCreatedAt(LocalDateTime.now());
        vehicleRepository.save(vehicle);

        // ===== CREATE SEATS =====
        int totalSeat = request.getTotalSeat();
        switch (totalSeat) {
            case 40 -> generateSeat40(vehicle, info);
            case 34 -> generateSeat34(vehicle, info);
            case 24 -> generateSeat24(vehicle, info);
            default -> throw new RuntimeException("Vehicle type not yet supported");
        }

        return new BaseResponse(201,vehicle,"Create vehicle successful",null,null);
    }

    public BaseResponse updateVehicle(VehicleRequest request) {
        UserDetails info = getInfo();

        VehicleEntity vehicle = vehicleRepository.findByVehicleId(request.getVehicleId());
        BaseResponse vehicleError = validateVehicleExists(vehicle);
        if (vehicleError != null) {
            return vehicleError;
        }

        vehicle.setActive(request.getActive());
        vehicle.setUpdatedBy(info.getUsername());
        vehicle.setUpdatedAt(LocalDateTime.now());
        vehicleRepository.save(vehicle);

        return new BaseResponse(200, vehicle, "Update vehicle successful", null, null);
    }

    public BaseResponse deleteVehicle(VehicleRequest request) {
        UserDetails info = getInfo();

        BaseResponse permissionError = validateIsAdmin(info);
        if (permissionError != null) {
            return permissionError;
        }

        VehicleEntity vehicle = vehicleRepository.findByVehicleId(request.getVehicleId());
        if (vehicle == null) {
            return new BaseResponse(404, null, "Vehicle not found", "No error", null);
        }
        vehicleRepository.delete(vehicle);
        return new BaseResponse(204, null, "Delete vehicle successfully", "No error", null);
    }

    // ------------------ validate ------------------
    private BaseResponse validateIsAdmin(UserDetails info) {
        if (!isAdmin(info)) {
            return new BaseResponse(403, null, "You don't have permission!", null, null);
        }
        return null;
    }

    private BaseResponse validateRequiredFields(VehicleRequest request) {
        if (request.getLicensePlate() == null || request.getTotalSeat() == null) {
            return new BaseResponse(400, null, "LicensePlate and TotalSeat are required", null, null);
        }
        return null;
    }

    private BaseResponse validateLicensePlateNotDuplicate(VehicleRequest request) {
        VehicleEntity existedVehicle = vehicleRepository.findByLicensePlate(request.getLicensePlate());
        if (existedVehicle != null) {
            return new BaseResponse(409, null,"Vehicle already existed", null,null);
        }
        return null;
    }

    private BaseResponse validateVehicleExists(VehicleEntity vehicle) {
        if (vehicle == null) {
            return new BaseResponse(404, null, "Vehicle not found", null, null);
        }
        return null;
    }

    // ------------------ helper ------------------
    private void generateSeat40(VehicleEntity vehicle, UserDetails info) {
        List<SeatEntity> seats = new ArrayList<>();
        String[] columns = {"A", "B", "C"};
        int totalRows = 6;
        // ===== FLOOR 1 (ODD) =====
        for (int row = 1; row <= totalRows; row++) {
            int seatIndex = row * 2 - 1; // 1,3,5,7,9,11
            for (String col : columns) {
                seats.add(createSeat(
                        col + seatIndex,
                        1,
                        row,
                        col,
                        vehicle,
                        info
                ));
            }
        }
        // extra seats floor 1
        seats.add(createSeat("A13", 1, totalRows + 1, "A", vehicle, info));
        seats.add(createSeat("C13", 1, totalRows + 1, "C", vehicle, info));
        // ===== FLOOR 2 (EVEN) =====
        for (int row = 1; row <= totalRows; row++) {
            int seatIndex = row * 2; // 2,4,6,8,10,12
            for (String col : columns) {
                seats.add(createSeat(
                        col + seatIndex,
                        2,
                        row,
                        col,
                        vehicle,
                        info
                ));
            }
        }
        // extra seats floor 2
        seats.add(createSeat("A14", 2, totalRows + 1, "A", vehicle, info));
        seats.add(createSeat("C14", 2, totalRows + 1, "C", vehicle, info));
        seatRepository.saveAll(seats);
    }

    private void generateSeat34(VehicleEntity vehicle, UserDetails info) {
        List<SeatEntity> seats = new ArrayList<>();
        int totalRows = 6;
        // ===== FLOOR 1 (ODD) =====
        for (int row = 1; row <= totalRows; row++) {
            int seatIndex = row * 2 - 1; // 1,3,5,7,9,11
            // A luôn có
            seats.add(createSeat("A" + seatIndex, 1, row, "A", vehicle, info));
            // B: bỏ row 1
            if (row > 1) {
                int bIndex = (row - 1) * 2 - 1; // B1, B3, B5...
                seats.add(createSeat("B" + bIndex, 1, row, "B", vehicle, info));
            }
            // C luôn có
            seats.add(createSeat("C" + seatIndex, 1, row, "C", vehicle, info));
        }
        // ===== FLOOR 2 (EVEN) =====
        for (int row = 1; row <= totalRows; row++) {
            int seatIndex = row * 2; // 2,4,6,8,10,12
            // A luôn có
            seats.add(createSeat("A" + seatIndex, 2, row, "A", vehicle, info));
            // B: bỏ row 1
            if (row > 1) {
                int bIndex = (row - 1) * 2; // B2, B4, B6...
                seats.add(createSeat("B" + bIndex, 2, row, "B", vehicle, info));
            }
            // C luôn có
            seats.add(createSeat("C" + seatIndex, 2, row, "C", vehicle, info));
        }
        seatRepository.saveAll(seats);
    }

    private void generateSeat24(VehicleEntity vehicle, UserDetails info) {
        List<SeatEntity> seats = new ArrayList<>();
        String[] columns = {"A", "B"};
        int totalRows = 6;
        // ===== FLOOR 1 (ODD) =====
        for (int row = 1; row <= totalRows; row++) {
            int seatIndex = row * 2 - 1; // 1,3,5,7,9,11
            for (String col : columns) {
                seats.add(createSeat(
                        col + seatIndex,
                        1,
                        row,
                        col,
                        vehicle,
                        info
                ));
            }
        }
        // ===== FLOOR 2 (EVEN) =====
        for (int row = 1; row <= totalRows; row++) {
            int seatIndex = row * 2; // 2,4,6,8,10,12
            for (String col : columns) {
                seats.add(createSeat(
                        col + seatIndex,
                        2,
                        row,
                        col,
                        vehicle,
                        info
                ));
            }
        }
        seatRepository.saveAll(seats);
    }

    private SeatEntity createSeat(String seatNumber, int floor, int seatRow, String seatColumn, VehicleEntity vehicle, UserDetails info) {
        SeatEntity seat = new SeatEntity();
        seat.setSeatNumber(seatNumber);
        seat.setFloor(floor);
        seat.setSeatRow(seatRow);
        seat.setSeatColumn(seatColumn);
        seat.setVehicle(vehicle);
        seat.setCreatedBy(info.getUsername());
        seat.setCreatedAt(LocalDateTime.now());
        return seat;
    }

    private boolean isAdmin(UserDetails info) {
        return info.getAuthorities()
                .stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    private UserDetails getInfo() {
        return (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
