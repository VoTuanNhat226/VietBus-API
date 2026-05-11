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
        passengerEntity.setIdCardNumber(request.getIdCardNumber());
        passengerEntity.setNote(request.getNote());
        passengerEntity.setPhoneNumber(request.getPhoneNumber());

        passengerRepository.save(passengerEntity);

        return new BaseResponse(200, passengerEntity, "Create a passenger successful", null, null);
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

    public BaseResponse updatePassenger(PassengerRequest request) {

        // check tồn tại
        PassengerEntity entity = passengerRepository.findById(request.getPassengerId())
                .orElseThrow(() -> new RuntimeException("Passenger not found"));

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

        entity.setFullName(request.getFullName().trim());
        entity.setEmail(request.getEmail().trim().toLowerCase());
        entity.setPhoneNumber(request.getPhoneNumber());
        entity.setIdCardNumber(request.getIdCardNumber());
        entity.setNote(request.getNote());

        passengerRepository.save(entity);

        return new BaseResponse(200, toResponse(entity), "Update passenger successful", null, null);
    }

    public BaseResponse deletePassenger(PassengerRequest request) {

        // check tồn tại
        PassengerEntity entity = passengerRepository.findById(request.getPassengerId())
                .orElseThrow(() -> new RuntimeException("Passenger not found"));

        passengerRepository.delete(entity);

        return new BaseResponse(200, null, "Delete passenger successful", null, null);
    }

    public BaseResponseNew<List<PassengerResponse>> searchPassenger(PassengerSearchRequest request) {
        long start = System.currentTimeMillis();

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());

        String fullName = (request.getFullName() == null || request.getFullName().isBlank())
                ? null : request.getFullName().trim();
        String phoneNumber = (request.getPhoneNumber() == null || request.getPhoneNumber().isBlank())
                ? null : request.getPhoneNumber().trim();
        String email = (request.getEmail() == null || request.getEmail().isBlank())
                ? null : request.getEmail().trim().toLowerCase();
        String idCardNumber = (request.getIdCardNumber() == null || request.getIdCardNumber().isBlank())
                ? null : request.getIdCardNumber().trim();

        Page<PassengerEntity> page = passengerRepository.searchPassengers(
                fullName, phoneNumber, email, idCardNumber, pageable
        );

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
        String message = "Search passenger successfully";

        return new BaseResponseNew<>(
                200, message, data, meta, null, "Success", meta.getTook()
        );
    }
}
