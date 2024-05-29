package com.orders.api.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDTO
{
    private String name;
    private String email;
    private String password;
    private String role;
}
