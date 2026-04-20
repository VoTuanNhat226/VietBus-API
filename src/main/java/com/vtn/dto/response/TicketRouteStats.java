package com.vtn.dto.response;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TicketRouteStats {
    private Long total;
    private String fromStation;
    private String toStation;

    public TicketRouteStats(Long total, String fromStation, String toStation) {
        this.total = total;
        this.fromStation = fromStation;
        this.toStation = toStation;
    }
}
