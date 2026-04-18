package com.vtn.controller;

import com.vtn.constant.APIConstants;
import com.vtn.dto.request.TicketRequest;
import com.vtn.service.TicketService;
import com.vtn.utils.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

    @PostMapping(value = APIConstants.API_GET_ALL_TICKET_UNPAID)
    public  ResponseEntity<BaseResponse> getAllTicketUnpaid(@RequestBody TicketRequest ticketRequest) {
        long beginTime = System.currentTimeMillis();
        BaseResponse response = ticketService.getAllTicketsUnpaid(ticketRequest);
        response.setTook(System.currentTimeMillis() - beginTime);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(value = APIConstants.API_GET_ALL_TICKET_BY_TRIP_ID)
    public  ResponseEntity<BaseResponse> getAllTicketByTripId(@RequestBody TicketRequest ticketRequest) {
        long beginTime = System.currentTimeMillis();
        BaseResponse response = ticketService.getAllTicketsByTripId(ticketRequest);
        response.setTook(System.currentTimeMillis() - beginTime);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(value =APIConstants.API_CREATE_TICKET)
    public ResponseEntity<BaseResponse> create(@RequestBody TicketRequest request) {
        long beginTime = System.currentTimeMillis();
        BaseResponse response = ticketService.createTicket(request);
        response.setTook(System.currentTimeMillis() - beginTime);

        return ResponseEntity
                .status(response.getStatusCode())
                .body(response);
    }

    @PostMapping(value =APIConstants.API_UPDATE_TICKET)
    public ResponseEntity<BaseResponse> update(@RequestBody TicketRequest request) {
        long beginTime = System.currentTimeMillis();
        BaseResponse response = ticketService.updateTicket(request);
        response.setTook(System.currentTimeMillis() - beginTime);

        return ResponseEntity
                .status(response.getStatusCode())
                .body(response);
    }
}
