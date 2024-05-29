package com.orders.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class KaraokeDTO
{
    private Integer room_num;
    private Integer seats;
    private Boolean booked;
}
