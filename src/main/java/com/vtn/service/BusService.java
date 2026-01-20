package com.vtn.service;

import com.vtn.dto.request.BusRequest;
import com.vtn.entity.BusEntity;
import com.vtn.entity.SeatEntity;
import com.vtn.repository.BusRepository;
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

@Service
public class BusService {
    private final BusRepository busRepository;
    private final SeatRepository seatRepository;

    @Autowired
    public BusService(BusRepository busRepository,
                      SeatRepository seatRepository) {
        this.busRepository = busRepository;
        this.seatRepository = seatRepository;
    }

    public BaseResponse getAllBuses() {
        try {
            List<BusEntity> buses = busRepository.findAll();
            return new BaseResponse(200, buses, "Get all buses successfully", "Get all buses successfully", null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public BaseResponse createBus(BusRequest busRequest) {
        UserDetails info = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try {
            BusEntity existedBus = busRepository.findByLicensePlate(busRequest.getLicensePlate());
            if (existedBus != null) {
                return new BaseResponse(400, null,"Bus already exists", "Bus already exists",null);
            }

            // ===== CREATE BUS =====
            BusEntity bus = new BusEntity();
            bus.setLicensePlate(busRequest.getLicensePlate());
            bus.setTotalSeat(busRequest.getTotalSeat());
            bus.setActive(true);
            bus.setCreated_by(info.getUsername());
            bus.setCreated_at(LocalDateTime.now());

            busRepository.save(bus);

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
                            bus,
                            info
                    ));
                }
            }
            // extra seats floor 1
            seats.add(createSeat("A13", 1, totalRows + 1, "A", bus, info));
            seats.add(createSeat("C13", 1, totalRows + 1, "C", bus, info));
            // ===== FLOOR 2 (EVEN) =====
            for (int row = 1; row <= totalRows; row++) {
                int seatIndex = row * 2; // 2,4,6,8,10,12
                for (String col : columns) {
                    seats.add(createSeat(
                            col + seatIndex,
                            2,
                            row,
                            col,
                            bus,
                            info
                    ));
                }
            }
            // extra seats floor 2
            seats.add(createSeat("A14", 2, totalRows + 1, "A", bus, info));
            seats.add(createSeat("C14", 2, totalRows + 1, "C", bus, info));
            seatRepository.saveAll(seats);

            return new BaseResponse(201,bus,"Create bus successfully","No error",null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private SeatEntity createSeat(String seatNumber, int floor, int seatRow, String seatColumn, BusEntity bus, UserDetails info) {
        SeatEntity seat = new SeatEntity();
        seat.setSeatNumber(seatNumber);
        seat.setFloor(floor);
        seat.setSeatRow(seatRow);
        seat.setSeatColumn(seatColumn);
        seat.setBus(bus);
        seat.setCreatedBy(info.getUsername());
        seat.setCreatedAt(LocalDateTime.now());
        return seat;
    }

    public BaseResponse updateBus(BusRequest busRequest) {
        UserDetails info = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try {
            BusEntity bus = busRepository.findByBusId(busRequest.getBusId());
            if (bus == null) {
                return new BaseResponse(404, null, "Not found bus", "Not found bus", null);
            } else {
                bus.setLicensePlate(busRequest.getLicensePlate());
                bus.setTotalSeat(busRequest.getTotalSeat());
                bus.setActive(busRequest.isActive());
                bus.setUpdated_by(info.getUsername());
                bus.setUpdated_at(LocalDateTime.now());
                busRepository.save(bus);
                return new BaseResponse(200, bus, "Update bus successfully", "No error", null);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public BaseResponse deleteBus(BusRequest busRequest) {
        UserDetails info = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String role = info.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse(null);
        try {
            if("ROLE_ADMIN".equals(role)) {
                BusEntity bus = busRepository.findByBusId(busRequest.getBusId());
                if (bus == null) {
                    return new BaseResponse(404, null, "Not found bus", "Not found bus", null);
                } else {
                    busRepository.delete(bus);
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
