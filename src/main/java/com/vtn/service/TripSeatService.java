package com.vtn.service;

import com.vtn.dto.request.TripSeatRequest;
import com.vtn.dto.response.TripSeatResponse;
import com.vtn.entity.TripSeatEntity;
import com.vtn.enumdef.TripSeatStatusEnum;
import com.vtn.repository.TripSeatRepository;
import com.vtn.utils.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class TripSeatService {
    private final TripSeatRepository tripSeatRepository;

    @Autowired
    public TripSeatService(TripSeatRepository tripSeatRepository) {
        this.tripSeatRepository = tripSeatRepository;
    }

    public BaseResponse countTripSeatSoldByTripId(TripSeatRequest request) {
        if (request.getTripId() == null) {
            return new BaseResponse(400, null, "TripId is required", null, null);
        }
        Integer count = tripSeatRepository.countTripSeatSoldByTripId(request.getTripId(), List.of(TripSeatStatusEnum.SOLD, TripSeatStatusEnum.HOLD));
        return new BaseResponse(200, count, "Get quantity trip seat sold successful",null,null);
    }

    public BaseResponse getAllTripSeatsByTripId(TripSeatRequest request) {
        if (request.getTripId() == null) {
            return new BaseResponse(400, null, "TripId is required", null, null);
        }

        // Clear lock ghế
        tripSeatRepository.clearExpiredProcessing(request.getTripId(), TripSeatStatusEnum.BLOCKED, LocalDateTime.now());

        List<TripSeatEntity> tripSeats = tripSeatRepository.findAllTripSeatsByTripId(request.getTripId());
        return new BaseResponse(200, tripSeats, "Get all trip seats successful",null,null);
    }

    public BaseResponse getAllTripSeatAvailableByTripId(TripSeatRequest request) {
        List<TripSeatResponse> result  = tripSeatRepository.findAllTripSeatAvailableByTripId(request.getTripId())
                .stream()
                .map(ts -> new TripSeatResponse(
                        //TripSeat
                        ts.getId(),
                        ts.getStatus(),
                        //Seat
                        ts.getSeat().getSeatId(),
                        ts.getSeat().getSeatNumber(),
                        ts.getSeat().getFloor(),
                        ts.getSeat().getSeatRow(),
                        ts.getSeat().getSeatColumn(),
                        //Trip
                        ts.getTrip().getTripId(),
                        ts.getTrip().getTripCode(),
                        ts.getTrip().getRoute().getFromStation().getName(),
                        ts.getTrip().getRoute().getToStation().getName(),
                        ts.getTrip().getDepartureTime(),
                        ts.getTrip().getArrivalTime(),
                        ts.getTrip().getPrice(),
                        ts.getTrip().getStatus()
                ))
                .toList();
        return new BaseResponse(200, result, "Get all trip seats can sell successful",null,null);
    }

    @Transactional
    public BaseResponse lockTripSeat(TripSeatRequest request) {
        UserDetails info = getInfo();
        String currentUsername = info.getUsername();
        LocalDateTime now = LocalDateTime.now();

        if (request.getTripSeatId() == null) {
            return new BaseResponse(400, null, "TripSeatId is required", null, null);
        }

        TripSeatEntity tripSeat = tripSeatRepository.findByTripSeatId(request.getTripSeatId());
        if (tripSeat == null) {
            return new BaseResponse(404, null, "Seat not found", null, null);
        }

        // Kiểm tra ghế đã SOLD/HOLD chưa (không thể lock)
        TripSeatStatusEnum status = tripSeat.getStatus();
        if (TripSeatStatusEnum.SOLD.equals(status) || TripSeatStatusEnum.HOLD.equals(status)) {
            return new BaseResponse(409, null, "Seat has already been booked", null, null);
        }

        // Atomic UPDATE — chỉ 1 thread thắng
        int updated = tripSeatRepository.tryLockSeat(request.getTripSeatId(), currentUsername, now, now.plusMinutes(10));

        if (updated == 0) {
            // Không lock được -> người khác đang giữ lock còn hiệu lực
            // Fetch lại để lấy tên staff đang giữ
            TripSeatEntity current = tripSeatRepository.findByTripSeatId(request.getTripSeatId());
            String holder = current != null ? current.getProcessingStaff() : "unknown";
            return new BaseResponse(423,null,"Seat is being processed by another staff: " + holder,null,null);
        }

        // Fetch lại entity sau khi update để trả về data mới nhất
        TripSeatEntity updated_seat = tripSeatRepository.findByTripSeatId(request.getTripSeatId());
        return new BaseResponse(200, updated_seat, "Lock trip seat successful", null, null);
    }

    @Transactional
    public BaseResponse unlockTripSeat(TripSeatRequest request) {
        UserDetails info = getInfo();
        String currentUsername = info.getUsername();
        LocalDateTime now = LocalDateTime.now();

        if (request.getTripSeatId() == null) {
            return new BaseResponse(400, null, "TripSeatId is required", null, null);
        }

        TripSeatEntity tripSeat = tripSeatRepository.findByTripSeatId(request.getTripSeatId());
        if (tripSeat == null) {
            return new BaseResponse(404, null, "Seat not found", null, null);
        }

        // HOLD,SOLD -> không cho unlock
        if (TripSeatStatusEnum.SOLD.equals(tripSeat.getStatus()) || TripSeatStatusEnum.HOLD.equals(tripSeat.getStatus())) {
            return new BaseResponse(409, null, "Seat has already been booked", null, null);
        }

        // Không có ai lock -> không cần unlock
        if (tripSeat.getProcessingStaff() == null || tripSeat.getProcessingStaff().isEmpty()) {
            return new BaseResponse(200, null, "Seat is already unlocked", null, null);
        }

        // Atomic UPDATE — chỉ unlock nếu đúng điều kiện
        int updated = tripSeatRepository.tryUnlockSeat(request.getTripSeatId(), currentUsername, now);

        if (updated == 0) {
            // Không unlock được -> người khác đang giữ lock còn hiệu lực
            TripSeatEntity current = tripSeatRepository.findByTripSeatId(request.getTripSeatId());
            String holder = current != null ? current.getProcessingStaff() : "unknown";
            return new BaseResponse(423,null,"Seat is being processed by another staff: " + holder,null,null);
        }

        return new BaseResponse(200, null, "Unlock trip seat successful", null, null);
    }

    private UserDetails getInfo() {
        return (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
