package com.orders.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class RealTableDTO
{
    private Integer table_num;
    private Integer seats;
    private Boolean booked;
}
