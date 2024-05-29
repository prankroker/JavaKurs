package com.orders.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegistrationRequest {
    @Schema(example = "John")
    @NotBlank(message = "Write down your name!")
    private String name;
    @Schema(example = "email@gmail.com")
    @NotBlank(message = "Write down your email!")
    //@Email // for email validation
    private String email;
    @Schema(example = "1234")
    @NotBlank(message = "Write down your password!")
    private String password;
    @Schema(example = "User")
    @NotBlank(message = "Write down your role!")
    private String role;
}
