package com.orders.api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OrderDTO
{
    private Long id;
    private LocalDateTime timestamp;
    private Integer people_count;
    private Boolean status;
    private KaraokeDTO karaoke;
    private RealTableDTO table;
}
