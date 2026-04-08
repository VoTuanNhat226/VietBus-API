package com.vtn.dto.response;

import com.vtn.enumdef.TripSeatStatusEnum;
import com.vtn.enumdef.TripStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class TripSeatResponse {
    //TripSeat
    private UUID tripSeatId;
    private TripSeatStatusEnum tripSeatStatus;

    //Seat
    private UUID seatId;
    private String seatNumber;
    private Integer seatFloor;
    private Integer seatRow;
    private String seatColumn;

    //Trip
    private UUID tripId;
    private String tripCode;
    private String tripFromStation;
    private String tripToStation;
    private LocalDateTime tripDepartureTime;
    private LocalDateTime tripArrivalTime;
    private BigDecimal tripPrice;
    private TripStatusEnum tripStatus;
}
