package com.vtn.dto.response;

import com.vtn.enumdef.TripStatusEnum;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TripHistoryResponse {
    UUID id;
    TripStatusEnum status;
    String changeBy;
    LocalDateTime changeAt;
    UUID tripId;
}