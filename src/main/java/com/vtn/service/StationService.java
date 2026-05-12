package com.vtn.service;

import com.vtn.entity.StationEntity;
import com.vtn.repository.StationRepository;
import com.vtn.utils.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StationService {
    private final StationRepository stationRepository;

    @Autowired
    public StationService(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    public BaseResponse getAllStations() {
        List<StationEntity> stations = stationRepository.findAll();
        return new BaseResponse(200,stations,"Get all stations successful",null,null);
    }
}
