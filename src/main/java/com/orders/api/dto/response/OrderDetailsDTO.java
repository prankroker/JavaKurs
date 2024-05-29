package com.orders.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class OrderDetailsDTO
{
    private List<KaraokeDTO> karaokeDTOS;
    private List<RealTableDTO> realTableDTOS;
}
