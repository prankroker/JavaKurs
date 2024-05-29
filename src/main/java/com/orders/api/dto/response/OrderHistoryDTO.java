package com.orders.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
@Getter
public class OrderHistoryDTO
{
    private Boolean status;
    private Integer people_count;
    private LocalDateTime timestamp;
    private KaraokeDTO karaokeDTO;
    private RealTableDTO realTableDTO;
}
