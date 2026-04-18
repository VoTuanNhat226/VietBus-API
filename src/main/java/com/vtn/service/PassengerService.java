package com.vtn.service;

import com.vtn.dto.request.PassengerRequest;
import com.vtn.entity.PassengerEntity;
import com.vtn.repository.PassengerRepository;
import com.vtn.utils.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PassengerService {
    private final PassengerRepository passengerRepository;

    @Autowired
    public PassengerService(PassengerRepository passengerRepository) {
        this.passengerRepository = passengerRepository;
    }

    public BaseResponse createAPassenger(PassengerRequest request) {

        // check duplicate
        if (passengerRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        PassengerEntity passengerEntity = new PassengerEntity();
        passengerEntity.setFullName(request.getFullName().trim());
        passengerEntity.setEmail(request.getEmail().trim().toLowerCase());
        passengerEntity.setIdCard(request.getIdCard());
        passengerEntity.setNote(request.getNote());
        passengerEntity.setPhoneNumber(request.getPhoneNumber());

        passengerRepository.save(passengerEntity);

        return new BaseResponse(200, passengerEntity, "Create a passenger successfully!", null, null);
    }
}
