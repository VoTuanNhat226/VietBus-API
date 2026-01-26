package com.vtn.controller;

import com.vtn.constant.APIConstants;
import com.vtn.dto.request.PaymentRequest;
import com.vtn.service.PaymentService;
import com.vtn.utils.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentController {
    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping(value = APIConstants.API_GET_ALL_PAYMENT)
    public ResponseEntity<BaseResponse> getAllPayment(@RequestBody PaymentRequest request) {
        long beginTime = System.currentTimeMillis();
        BaseResponse response = paymentService.getAllPayments(request);
        response.setTook(System.currentTimeMillis() - beginTime);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
