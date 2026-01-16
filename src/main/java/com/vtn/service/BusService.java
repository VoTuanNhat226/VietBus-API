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

    public BaseResponse createBus(BusRequest busRequest) {
        UserDetails info = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try {
                BusEntity busEntity = busRepository.findByLicensePlate(busRequest.getLicensePlate());
                if (busEntity != null) {
                    return new BaseResponse(400, null, "Bus already exists", "Bus already exists", null);
                }
                BusEntity bus = new BusEntity();
                bus.setLicensePlate(busRequest.getLicensePlate());
                bus.setTotalSeat(busRequest.getTotalSeat());
                bus.setActive(true);
                bus.setCreated_by(info.getUsername());
                bus.setCreated_at(LocalDateTime.now());
                busRepository.save(bus);
                if(busRequest.getTotalSeat() > 0) {
                    List<SeatEntity> seats = new ArrayList<>();
                    for(int i = 1; i <= busRequest.getTotalSeat(); i++) {
                        SeatEntity seat = new SeatEntity();
                        seat.setSeatNumber("A" + i);
                        seat.setBusLicensePlate(busRequest.getLicensePlate());
                        seat.setBus(bus);
                        seat.setCreated_by(info.getUsername());
                        seat.setCreated_at(LocalDateTime.now());
                        seats.add(seat);
                    }
                    seatRepository.saveAll(seats);
                }
                return new BaseResponse(201, bus, "Create bus successfully", "No error", null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
