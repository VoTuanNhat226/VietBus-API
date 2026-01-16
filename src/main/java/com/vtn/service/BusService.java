package com.vtn.service;

import com.vtn.dto.request.BusRequest;
import com.vtn.entity.BusEntity;
import com.vtn.repository.BusRepository;
import com.vtn.utils.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BusService {
    private final BusRepository busRepository;

    @Autowired
    public BusService(BusRepository busRepository) {
        this.busRepository = busRepository;
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
                BusEntity bus = new BusEntity();
                bus.setLicensePlate(busRequest.getLicensePlate());
                bus.setTotalSeat(busRequest.getTotalSeat());
                bus.setActive(true);
                bus.setCreated_by(info.getUsername());
                bus.setCreated_at(LocalDateTime.now());
                busRepository.save(bus);
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
