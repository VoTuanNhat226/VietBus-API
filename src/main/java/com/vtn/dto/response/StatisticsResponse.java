package com.vtn.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StatisticsResponse {
    private BigDecimal revenue;
    private BigDecimal revenuePrev;
    private BigDecimal growthPercent;

    private BigDecimal totalTicket;
    private BigDecimal totalTicketPrev;
    private BigDecimal growthTicketPercent;

    private BigDecimal totalTrip;
    private BigDecimal totalTripPrev;
    private BigDecimal growthTripPercent;

    private BigDecimal totalPassenger;
    private BigDecimal totalPassengerPrev;
    private BigDecimal growthPassengerPercent;

    private BigDecimal totalVehicleActive;
    private BigDecimal totalVehicleInActive;
}
