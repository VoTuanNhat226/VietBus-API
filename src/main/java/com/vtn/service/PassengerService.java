package com.vtn.service;

import com.vtn.dto.request.PassengerRequest;
import com.vtn.dto.request.PassengerSearchRequest;
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
import java.util.UUID;

@Service
@Slf4j
public class PassengerService {
    private final PassengerRepository passengerRepository;

    @Autowired
    public PassengerService(PassengerRepository passengerRepository) {
        this.passengerRepository = passengerRepository;
    }

    public BaseResponse getAllPassenger() {
        List<PassengerEntity> passengers = passengerRepository.findAll();
        return new BaseResponse(200, passengers, "Get all passengers successful", null, null);
    }

    public BaseResponse createPassenger(PassengerRequest request) {

        validateNotDuplicateOnCreate(request);

        PassengerEntity passengerEntity = new PassengerEntity();
        passengerEntity.setFullName(request.getFullName().trim());
        passengerEntity.setEmail(request.getEmail().trim().toLowerCase());
        passengerEntity.setIdCardNumber(request.getIdCardNumber());
        passengerEntity.setNote(request.getNote());
        passengerEntity.setPhoneNumber(request.getPhoneNumber());
        passengerRepository.save(passengerEntity);

        return new BaseResponse(201, passengerEntity, "Create a passenger successful", null, null);
    }

    public BaseResponseNew<List<PassengerResponse>> getListPassenger(BasePageRequest request) {
        long start = System.currentTimeMillis();
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        Page<PassengerEntity> page = passengerRepository.findAll(pageable);

        List<PassengerResponse> data = page.getContent()
                .stream()
                .map(this::toResponse)
                .toList();

        PageMeta pageMeta = new PageMeta(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );

        Meta meta = new Meta(pageMeta, System.currentTimeMillis() - start);
        String message = "Get list passenger successfully";

        return new BaseResponseNew<>(
                200, message, data, meta, null, "Success", meta.getTook()
        );
    }

    public BaseResponse updatePassenger(PassengerRequest request) {

        PassengerEntity entity = getPassengerOrThrow(request.getPassengerId());

        validateNotDuplicateOnUpdate(request, entity);

        entity.setFullName(request.getFullName().trim());
        entity.setEmail(request.getEmail().trim().toLowerCase());
        entity.setPhoneNumber(request.getPhoneNumber());
        entity.setIdCardNumber(request.getIdCardNumber());
        entity.setNote(request.getNote());

        passengerRepository.save(entity);

        return new BaseResponse(200, toResponse(entity), "Update passenger successful", null, null);
    }

    public BaseResponse deletePassenger(PassengerRequest request) {

        PassengerEntity entity = getPassengerOrThrow(request.getPassengerId());

        passengerRepository.delete(entity);

        return new BaseResponse(204, null, "Delete passenger successful", null, null);
    }

    public BaseResponseNew<List<PassengerResponse>> searchPassenger(PassengerSearchRequest request) {
        long start = System.currentTimeMillis();

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());

        String fullName = (request.getFullName() == null || request.getFullName().isBlank()) ? null : request.getFullName().trim();
        String phoneNumber = (request.getPhoneNumber() == null || request.getPhoneNumber().isBlank()) ? null : request.getPhoneNumber().trim();
        String email = (request.getEmail() == null || request.getEmail().isBlank()) ? null : request.getEmail().trim().toLowerCase();
        String idCardNumber = (request.getIdCardNumber() == null || request.getIdCardNumber().isBlank()) ? null : request.getIdCardNumber().trim();

        Page<PassengerEntity> page = passengerRepository.searchPassengers(fullName, phoneNumber, email, idCardNumber, pageable);

        List<PassengerResponse> data = page.getContent()
                .stream()
                .map(this::toResponse)
                .toList();

        PageMeta pageMeta = new PageMeta(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );

        Meta meta = new Meta(pageMeta, System.currentTimeMillis() - start);
        String message = "Search passenger successful";

        return new BaseResponseNew<>(
                200, message, data, meta, null, "Success", meta.getTook()
        );
    }

    // ------------------ validate ------------------
    private void validateNotDuplicateOnCreate(PassengerRequest request) {
        if (passengerRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        if (passengerRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new RuntimeException("Phone number already exists");
        }
        if (passengerRepository.existsByIdCardNumber(request.getIdCardNumber())) {
            throw new RuntimeException("ID number already exists");
        }
    }

    private void validateNotDuplicateOnUpdate(PassengerRequest request, PassengerEntity entity) {
        // check duplicate email (trừ chính nó)
        if (!entity.getEmail().equals(request.getEmail().trim().toLowerCase())
                && passengerRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new RuntimeException("Email already exists");
        }

        // check duplicate phone (trừ chính nó)
        if (!entity.getPhoneNumber().equals(request.getPhoneNumber())
                && passengerRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new RuntimeException("Phone number already exists");
        }

        // check duplicate idCardNumber (trừ chính nó)
        if (!entity.getIdCardNumber().equals(request.getIdCardNumber())
                && passengerRepository.existsByIdCardNumber(request.getIdCardNumber())) {
            throw new RuntimeException("ID card number already exists");
        }
    }

    // ------------------ helper ------------------
    private PassengerEntity getPassengerOrThrow(UUID passengerId) {
        return passengerRepository.findById(passengerId)
                .orElseThrow(() -> new RuntimeException("Passenger not found"));
    }

    private PassengerResponse toResponse(PassengerEntity entity) {
        if (entity == null) return null;

        PassengerResponse response = new PassengerResponse();
        response.setPassengerId(entity.getPassengerId());
        response.setFullName(entity.getFullName());
        response.setPhoneNumber(entity.getPhoneNumber());
        response.setEmail(entity.getEmail());
        response.setIdCardNumber(entity.getIdCardNumber());
        response.setNote(entity.getNote());
        response.setCreatedBy(entity.getCreatedBy());
        response.setUpdatedBy(entity.getUpdatedBy());

        return response;
    }
}
