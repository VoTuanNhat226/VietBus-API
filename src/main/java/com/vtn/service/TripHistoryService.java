package com.vtn.service;

import com.vtn.dto.request.TripRequest;
import com.vtn.dto.response.TripHistoryResponse;
import com.vtn.entity.TripHistory;
import com.vtn.repository.TripHistoryRepository;
import com.vtn.utils.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TripHistoryService {
    private final TripHistoryRepository tripHistoryRepository;

    @Autowired
    public TripHistoryService(TripHistoryRepository tripHistoryRepository) {
        this.tripHistoryRepository = tripHistoryRepository;
    }

    public BaseResponse getByTripId(TripRequest request) {
        List<TripHistoryResponse> tripHistoryResponses =
                tripHistoryRepository.findByTripId(request.getTripId())
                        .stream()
                        .map(history -> TripHistoryResponse.builder()
                                .id(history.getId())
                                .status(history.getStatus())
                                .changeBy(history.getChangeBy())
                                .changeAt(history.getChangeAt())
                                .tripId(history.getTrip().getTripId())
                                .build())
                        .toList();

        return new BaseResponse(200, tripHistoryResponses, "Get trip history by tripId successful", null, null);
    }
}
