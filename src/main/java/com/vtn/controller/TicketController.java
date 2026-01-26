package com.vtn.controller;

import com.vtn.constant.APIConstants;
import com.vtn.dto.request.EmployeeRequest;
import com.vtn.dto.request.TicketRequest;
import com.vtn.service.TicketService;
import com.vtn.utils.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TicketController {
    private final TicketService ticketService;

    @Autowired
    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping(APIConstants.API_CREATE_TICKET)
    public ResponseEntity<BaseResponse> create(@RequestBody TicketRequest request) {
        long beginTime = System.currentTimeMillis();
        BaseResponse response = ticketService.createTicket(request);
        response.setTook(System.currentTimeMillis() - beginTime);

        return ResponseEntity
                .status(response.getStatusCode())
                .body(response);
    }
}
