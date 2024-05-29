package com.orders.api.controller;

import com.orders.api.configuration.security.UserAdapter;
import com.orders.api.dto.request.OrderCreateDTO;
import com.orders.api.dto.request.RegistrationRequest;
import com.orders.api.dto.response.OrderDTO;
import com.orders.api.dto.response.OrderDetailsDTO;
import com.orders.api.dto.response.OrderHistoryDTO;
import com.orders.api.dto.response.UserDTO;
import com.orders.api.service.OrdersApiService;
import com.orders.api.service.UserDetailsServiceImp;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
public class OrdersApiController
{
    private final OrdersApiService ordersApiService;
    private final UserDetailsServiceImp userDetailsService;

    @Operation(summary = "Get all variants of orders")
    @ApiResponse(responseCode = "200",
        description = "All variants of orders",
        content = @Content(
            schema = @Schema(implementation = OrderDetailsDTO.class),
            examples = @ExampleObject(
                value = "{\"karaokeDTOS\":[{\"room_num\":1,\"seats\":2," +
                    "\"booked\":true}],\"realTableDTOS\":[{\"table_num\":2," +
                    "\"seats\":4,\"booked\":false}]}")))

    @GetMapping("/orders/variants")
    public ResponseEntity<OrderDetailsDTO> getOrderVariants()
    {
        return new ResponseEntity<>(ordersApiService.getOrderVariants(),HttpStatus.OK);
    }

    @Operation(summary = "Register new user")
    @ApiResponse(responseCode = "200", description = "User registered", content = @Content)
    @ApiResponse(responseCode = "400", description = "Wrong role or user already registered", content = @Content)

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegistrationRequest registrationRequest) {
        return userDetailsService.register(registrationRequest);
    }

    @Operation(summary = "Check security configuration, authorization required",
            security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponse(responseCode = "200", description = "Correct", content = @Content)
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)

    @GetMapping("/check/authorities")
    public String checkAuthorities() {
        return "Correct";
    }

    @Operation(summary = "Get registered users, ADMIN authority required",
            security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponse(responseCode = "200",
            description = "All users",
            content = @Content(
                    schema = @Schema(implementation = UserDTO.class),
                    examples = @ExampleObject(
                            value = "[{\"name\":\"vadim\",\"email\":\"email@gmail.com\"," +
                                    "\"password\":\"\",\"role\":\"USER\"}]")))
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    @ApiResponse(responseCode = "403", description = "Wrong authority", content = @Content)

    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getUsers(){
        return new ResponseEntity<>(ordersApiService.getUsers(),HttpStatus.OK);
    }

    @Operation(summary = "Order creation, authorization required",
            security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponse(responseCode = "200",
            description = "Created order",
            content = @Content(
                    schema = @Schema(implementation = OrderHistoryDTO.class),
                    examples = @ExampleObject(
                            value = "{\"status\":false,\"people_count\":1,\"timestamp\":\"2024-05-19T09:01:06\"," +
                                    "\"karaokeDTO\":{\"room_num\":1,\"seats\":2,\"booked\":true}," +
                                    "\"realTableDTO\":{\"table_num\":1,\"seats\":3,\"booked\":true}}")))
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)

    @PostMapping("/order/create")
    public ResponseEntity<OrderHistoryDTO> createOrder(@Valid @RequestBody OrderCreateDTO orderCreateDTO,
                                       @AuthenticationPrincipal UserAdapter user) {
        return new ResponseEntity<>(ordersApiService.createOrder(orderCreateDTO, user.getUser()), HttpStatus.OK);
    }

    @Operation(summary = "Get orders that needs confirmation, WAITER authority required",
            security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponse(responseCode = "200",
            description = "All orders to confirm",
            content = @Content(
                    schema = @Schema(implementation = OrderDTO.class),
                    examples = @ExampleObject(
                            value = "[{\"id\":1,\"timestamp\":\"2024-05-19T09:01:06\",\"people_count\":1,\"status\":false," +
                                    "\"karaoke\":{\"room_num\":1,\"seats\":2,\"booked\":true}," +
                                    "\"table\":{\"table_num\":1,\"seats\":3,\"booked\":true}}]")))
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    @ApiResponse(responseCode = "403", description = "Wrong authority", content = @Content)

    @GetMapping("/orders/toConfirm")
    public ResponseEntity<List<OrderDTO>> getOrdersToConfirm() {
        return new ResponseEntity<>(ordersApiService.getOrdersToConfirm(), HttpStatus.OK);
    }

    @Operation(summary = "Confirm order, WAITER authority required",
            security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponse(responseCode = "200", description = "Order confirmed", content = @Content)
    @ApiResponse(responseCode = "400", description = "Order has been already confirmed or wrong id", content = @Content)
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    @ApiResponse(responseCode = "403", description = "Wrong authority", content = @Content)
    @PostMapping("/order/confirm") //link looks like /order/confirm?id=1
    public ResponseEntity<String> confirmOrder(@Parameter(description = "Order ID for confirmation")
                                                   @RequestParam Long id) {
        return ordersApiService.confirmOrder(id);
    }

    @Operation(summary = "Cancel order, ADMIN authority required",
            security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponse(responseCode = "200", description = "Order canceled", content = @Content)
    @ApiResponse(responseCode = "400", description = "Wrong id", content = @Content)
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    @ApiResponse(responseCode = "403", description = "Wrong authority", content = @Content)
    @PostMapping("/order/cancel") //link looks like /order/cancel?id=1
    public ResponseEntity<String> cancelOrder(@Parameter(description = "Order ID for cancellation")
                                                  @RequestParam Long id) {
        return ordersApiService.cancelOrder(id);
    }

    @Operation(summary = "Get all orders, ADMIN authority required",
            security = @SecurityRequirement(name = "basicAuth"))
    @ApiResponse(responseCode = "200",
            description = "All orders",
            content = @Content(
                    schema = @Schema(implementation = OrderDTO.class),
                    examples = @ExampleObject(
                            value = "[{\"id\":1,\"timestamp\":\"2024-05-19T09:01:06\",\"people_count\":1,\"status\":true," +
                                    "\"karaoke\":{\"room_num\":1,\"seats\":2,\"booked\":true}," +
                                    "\"table\":{\"table_num\":1,\"seats\":3,\"booked\":true}}]")))
    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    @ApiResponse(responseCode = "403", description = "Wrong authority", content = @Content)
    @GetMapping("/orders")
    public ResponseEntity<List<OrderDTO>> getOrders() {
        return new ResponseEntity<>(ordersApiService.getOrders(), HttpStatus.OK);
    }

    // http://localhost:8080/swagger-ui/index.html to access swagger
}
