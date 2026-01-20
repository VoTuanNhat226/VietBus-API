package com.vtn.service;

import com.vtn.dto.request.VehicleRequest;
import com.vtn.entity.VehicleEntity;
import com.vtn.entity.SeatEntity;
import com.vtn.repository.VehicleRepository;
import com.vtn.repository.SeatRepository;
import com.vtn.utils.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    public BaseResponse getAllBuses() {
        try {
            List<VehicleEntity> buses = vehicleRepository.findAll();
            return new BaseResponse(200, buses, "Get all buses successfully", "Get all buses successfully", null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public BaseResponse getVehicleById(VehicleRequest vehicleRequest) {
        try {
            Optional<VehicleEntity> bus = vehicleRepository.findById(vehicleRequest.getVehicleId());
            return new BaseResponse(200, bus, "Get bus successfully", "Get buse successfully", null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public BaseResponse createBus(VehicleRequest request) {
        UserDetails info = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try {
            VehicleEntity existedVehicle = vehicleRepository.findByLicensePlate(request.getLicensePlate());
            if (existedVehicle != null) {
                return new BaseResponse(400, null,"Xe đã tồn tại", null,null);
            }

            // ===== CREATE BUS =====
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

            return new BaseResponse(201,vehicle,"Thêm xe thành công",null,null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    public BaseResponse updateBus(VehicleRequest request) {
        UserDetails info = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try {
            VehicleEntity vehicle = vehicleRepository.findByVehicleId(request.getVehicleId());
            if (vehicle == null) {
                return new BaseResponse(404, null, "Không tìm thấy phương tiện", null, null);
            } else {
                vehicle.setLicensePlate(request.getLicensePlate());
                vehicle.setTotalSeat(request.getTotalSeat());
                vehicle.setActive(request.getActive());
                vehicle.setUpdatedBy(info.getUsername());
                vehicle.setUpdatedAt(LocalDateTime.now());
                vehicleRepository.save(vehicle);
                return new BaseResponse(200, vehicle, "Cập nhật phương tiện thành công", null, null);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public BaseResponse deleteBus(VehicleRequest vehicleRequest) {
        UserDetails info = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String role = info.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse(null);
        try {
            if("ROLE_ADMIN".equals(role)) {
                VehicleEntity bus = vehicleRepository.findByVehicleId(vehicleRequest.getVehicleId());
                if (bus == null) {
                    return new BaseResponse(404, null, "Not found bus", "Not found bus", null);
                } else {
                    vehicleRepository.delete(bus);
                    return new BaseResponse(204, null, "Delete bus successfully", "No error", null);
                }
            } else {
                return new BaseResponse(403, null, "You don't has permission", "No error", null);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
