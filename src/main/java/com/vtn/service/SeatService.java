package com.vtn.service;

import com.vtn.dto.request.SeatRequest;
import com.vtn.entity.SeatEntity;
import com.vtn.repository.SeatRepository;
import com.vtn.utils.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SeatService {
    private final SeatRepository seatRepository;

    @Autowired
    public SeatService(SeatRepository seatRepository) {
        this.seatRepository = seatRepository;
    }

    public BaseResponse getSeatByVehicleId(SeatRequest seatRequest) {
        List<SeatEntity> seats = seatRepository.findByVehicleId(seatRequest.getVehicleId());
        return new BaseResponse(200,seats,"Get seats by vehicle id successful",null,null);
    }
}
