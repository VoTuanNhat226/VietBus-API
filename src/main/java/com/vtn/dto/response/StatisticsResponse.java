package com.vtn.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
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
}
