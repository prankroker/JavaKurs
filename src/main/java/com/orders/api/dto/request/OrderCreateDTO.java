package com.orders.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
@Getter
public class OrderCreateDTO
{
    @Schema(example = "[\n\"karaoke\",\"table\"\n]")
    @Size(min = 1, message = "Choose at least one option!")
    private List<String> order;//обрані опції
    @Schema(example = "2024-12-12T12:12:12")
    @NotNull(message = "Choose the date and time of your reservation!")
    private LocalDateTime timestamp;
    @Min(value = 1, message = "At least one person must be present!")
    private Integer people_count;
}
