package com.vtn.dto.response;

import com.vtn.enumdef.TripStatusEnum;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
public class TripResponse implements Serializable {
    //Trip
    private UUID tripId;
    private String tripCode;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private BigDecimal price;
    private TripStatusEnum status;
    //Route
    private String fromStation;
    private String toStation;
    //Vehicle
    private Integer totalSeat;
    private String licensePlate;
    //Trip-Employee
    private List<String> driverNames;
    private List<String> assistantNames;
}
