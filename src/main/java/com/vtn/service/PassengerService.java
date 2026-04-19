package com.vtn.service;

import com.vtn.dto.request.PassengerRequest.CreatePassengerRequest;
import com.vtn.dto.response.PassengerResponse;
import com.vtn.entity.PassengerEntity;
import com.vtn.repository.PassengerRepository;
import com.vtn.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class PassengerService {
    private final PassengerRepository passengerRepository;

    @Autowired
    public PassengerService(PassengerRepository passengerRepository) {
        this.passengerRepository = passengerRepository;
    }

    public BaseResponse createAPassenger(CreatePassengerRequest request) {

        // check duplicate
        if (passengerRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        PassengerEntity passengerEntity = new PassengerEntity();
        passengerEntity.setFullName(request.getFullName().trim());
        passengerEntity.setEmail(request.getEmail().trim().toLowerCase());
        passengerEntity.setIdCardNumber(request.getIdCardNumber());
        passengerEntity.setNote(request.getNote());
        passengerEntity.setPhoneNumber(request.getPhoneNumber());

        passengerRepository.save(passengerEntity);

        return new BaseResponse(200, passengerEntity, "Create a passenger successful", null, null);
    }

    public BaseResponeNew<List<PassengerResponse>> getListPassenger(BasePageRequest request) {
        long start = System.currentTimeMillis();
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        Page<PassengerEntity> page = passengerRepository.findAll(pageable);

        List<PassengerResponse> data = page.getContent()
                .stream()
                .map(p -> new PassengerResponse()).toList();

        PageMeta pageMeta = new PageMeta(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );

        Meta meta = new Meta(pageMeta, System.currentTimeMillis() - start);

        String message = "Get list passenger";

        return new BaseResponeNew<>(
                200, message, data, meta, message, "Success", meta.getTook()
        );
    }
}
