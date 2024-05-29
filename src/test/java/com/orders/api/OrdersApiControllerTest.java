package com.orders.api;

import com.orders.api.configuration.security.Role;
import com.orders.api.configuration.security.SecurityConfig;
import com.orders.api.configuration.security.UserAdapter;
import com.orders.api.controller.OrdersApiController;
import com.orders.api.dto.request.OrderCreateDTO;
import com.orders.api.dto.request.RegistrationRequest;
import com.orders.api.dto.response.*;
import com.orders.api.exception.CreateOrderException;
import com.orders.api.exception.GetOrdersException;
import com.orders.api.model.User;
import com.orders.api.service.OrdersApiService;
import com.orders.api.service.UserDetailsServiceImp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrdersApiController.class)
@Import(SecurityConfig.class)
public class OrdersApiControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    OrdersApiService ordersApiService;
    @MockBean
    UserDetailsServiceImp userDetailsService;

    @Test
    @DisplayName("Test for POST /register endpoint")
    void testRegisterEndpoint() throws Exception {
        var registrationRequest = new RegistrationRequest(
                "John",
                "email@gmail.com",
                "1234",
                "user"
        );
        var responseEntity = new ResponseEntity<>("Successfully registered", HttpStatus.OK);

        when(userDetailsService.register(registrationRequest))
                .thenReturn(responseEntity);

        var requestBuilder = post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "name":"John",
                        "email":"email@gmail.com",
                        "password":"1234",
                        "role":"user"
                        }""");
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().bytes("Successfully registered".getBytes()));
    }

    @Test
    @DisplayName("Test for POST /register endpoint(wrong role provided)")
    void testRegisterEndpoint_WrongRole() throws Exception {
        var registrationRequest = new RegistrationRequest(
                "John",
                "email@gmail.com",
                "1234",
                "godUser"
        );
        var responseEntity = new ResponseEntity<>("Wrong role provided",
                HttpStatus.BAD_REQUEST);

        when(userDetailsService.register(registrationRequest))
                .thenReturn(responseEntity);

        var requestBuilder = post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "name":"John",
                        "email":"email@gmail.com",
                        "password":"1234",
                        "role":"godUser"
                        }""");
        mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(content().bytes("Wrong role provided".getBytes()));
    }

    @Test
    @DisplayName("Test for POST /register endpoint(such a user already registered)")
    void testRegisterEndpoint_AlreadyRegistered() throws Exception {
        var registrationRequest = new RegistrationRequest(
                "John",
                "email@gmail.com",
                "1234",
                "user"
        );
        var responseEntity = new ResponseEntity<>("\"Such a user already exists!",
                HttpStatus.BAD_REQUEST);

        when(userDetailsService.register(registrationRequest))
                .thenReturn(responseEntity);

        var requestBuilder = post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "name":"John",
                        "email":"email@gmail.com",
                        "password":"1234",
                        "role":"user"
                        }""");
        mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(content().bytes("\"Such a user already exists!".getBytes()));
    }

    @Test
    @WithMockUser(username = "email@gmail.com", password = "1234", authorities = "USER")
    @DisplayName("Test for GET /check/authorities")
    void testCheckAuthoritiesEndpoint() throws Exception {
        var requestBuilder = get("/check/authorities");
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().bytes("Correct".getBytes()));
    }

    @Test
    @DisplayName("Test for GET /orders/variants")
    void testGetOrderVariantsEndpoint() throws Exception {
        List<KaraokeDTO> karaokeDTOList = List.of(new KaraokeDTO(1, 1, false),
                new KaraokeDTO(2, 2, true));
        List<RealTableDTO> realTableDTOList = List.of(new RealTableDTO(1, 4, false));
        var expect = new OrderDetailsDTO(karaokeDTOList, realTableDTOList);

        when(ordersApiService.getOrderVariants()).thenReturn(expect);

        var requestBuilder = get("/orders/variants");
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.karaokeDTOS[0].room_num").value(1))
                .andExpect(jsonPath("$.karaokeDTOS[0].seats").value(1))
                .andExpect(jsonPath("$.karaokeDTOS[0].booked").value(false))
                .andExpect(jsonPath("$.karaokeDTOS[1].room_num").value(2))
                .andExpect(jsonPath("$.karaokeDTOS[1].seats").value(2))
                .andExpect(jsonPath("$.karaokeDTOS[1].booked").value(true))
                .andExpect(jsonPath("$.realTableDTOS[0].table_num").value(1))
                .andExpect(jsonPath("$.realTableDTOS[0].seats").value(4))
                .andExpect(jsonPath("$.realTableDTOS[0].booked").value(false));
    }

    @Test
    @WithMockUser(username = "email@gmail.com", password = "1234", authorities = "ADMIN")
    @DisplayName("Test for GET /users")
    void testGetUsersEndpoint() throws Exception {
        List<UserDTO> expect = List.of(UserDTO.builder()
                        .name("John")
                        .email("email@gmail.com")
                        .password("1234")
                        .role("ADMIN")
                        .build());

        when(ordersApiService.getUsers()).thenReturn(expect);

        var requestBuilder = get("/users");
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].name").value("John"))
                .andExpect(jsonPath("$.[0].email").value("email@gmail.com"))
                .andExpect(jsonPath("$.[0].password").value("1234"))
                .andExpect(jsonPath("$.[0].role").value("ADMIN"));
    }

    @Test
    @DisplayName("Test for GET /order/create")
    void testCreateOrderEndpoint() throws Exception {
        var orderCreate = OrderCreateDTO.builder()
                .order(List.of("karaoke"))
                .timestamp(LocalDateTime.parse("2024-12-12T12:12:12"))
                .people_count(1)
                .build();
        var user = User.builder()
                .name("John")
                .email("email@gmail.com")
                .password("1234")
                .role(Role.WAITER)
                .build();
        var orderHistory = OrderHistoryDTO.builder()
                .timestamp(LocalDateTime.parse("2024-12-12T12:12:12"))
                .status(false)
                .people_count(1)
                .karaokeDTO(new KaraokeDTO(1, 1, true))
                .build();

        when(ordersApiService.createOrder(orderCreate, user)).thenReturn(orderHistory);
        SecurityContextHolder.getContext().setAuthentication(new PreAuthenticatedAuthenticationToken(
                new UserAdapter(user), null, List.of(new SimpleGrantedAuthority(user.getRole().toString()))
        )); //pre-populate the context with the user, alternative to @WithMockUser

        var requestBuilder = post("/order/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"order\":[\"karaoke\"],\"timestamp\":\"2024-12-12T12:12:12\",\"people_count\":1}");
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.people_count").value(1))
                .andExpect(jsonPath("$.timestamp").value("2024-12-12T12:12:12"))
                .andExpect(jsonPath("$.karaokeDTO.room_num").value(1))
                .andExpect(jsonPath("$.karaokeDTO.seats").value(1))
                .andExpect(jsonPath("$.karaokeDTO.booked").value(true));
    }

    @Test
    @DisplayName("Test for GET /order/create(when no free seats)")
    void testCreateOrderEndpoint_NoFreeSeats() throws Exception {
        var orderCreate = OrderCreateDTO.builder()
                .order(List.of("karaoke"))
                .timestamp(LocalDateTime.parse("2024-12-12T12:12:12"))
                .people_count(1)
                .build();
        var user = User.builder()
                .name("John")
                .email("email@gmail.com")
                .password("1234")
                .role(Role.WAITER)
                .build();

        when(ordersApiService.createOrder(orderCreate, user))
                .thenThrow(new CreateOrderException("There are no free karaoke seats!"));
        SecurityContextHolder.getContext().setAuthentication(new PreAuthenticatedAuthenticationToken(
                new UserAdapter(user), null, List.of(new SimpleGrantedAuthority(user.getRole().toString()))
        )); //pre-populate the context with the user, alternative to @WithMockUser

        var requestBuilder = post("/order/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"order\":[\"karaoke\"],\"timestamp\":\"2024-12-12T12:12:12\",\"people_count\":1}");
        mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("There are no free karaoke seats!"));
    }

    @Test
    @DisplayName("Test for GET /order/create(wrong option selected)")
    void testCreateOrderEndpoint_WrongOption() throws Exception {
        var orderCreate = OrderCreateDTO.builder()
                .order(List.of("cinema"))
                .timestamp(LocalDateTime.parse("2024-12-12T12:12:12"))
                .people_count(1)
                .build();
        var user = User.builder()
                .name("John")
                .email("email@gmail.com")
                .password("1234")
                .role(Role.WAITER)
                .build();

        when(ordersApiService.createOrder(orderCreate, user))
                .thenThrow(new CreateOrderException("The wrong option was selected or" +
                        " the same option was selected twice!"));
        SecurityContextHolder.getContext().setAuthentication(new PreAuthenticatedAuthenticationToken(
                new UserAdapter(user), null, List.of(new SimpleGrantedAuthority(user.getRole().toString()))
        )); //pre-populate the context with the user, alternative to @WithMockUser

        var requestBuilder = post("/order/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"order\":[\"cinema\"],\"timestamp\":\"2024-12-12T12:12:12\",\"people_count\":1}");
        mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("The wrong option was selected or" +
                        " the same option was selected twice!"));
    }

    @Test
    @WithMockUser(username = "email@gmail.com", password = "1234", authorities = "WAITER")
    @DisplayName("Test for GET /orders/toConfirm")
    void testGetOrdersToConfirmEndpoint() throws Exception {
        var orders = List.of(OrderDTO.builder()
                        .id(1L)
                        .status(false)
                        .people_count(1)
                        .timestamp(LocalDateTime.parse("2024-12-12T12:12:12"))
                        .karaoke(new KaraokeDTO(1, 1, true))
                        .build());

        when(ordersApiService.getOrdersToConfirm()).thenReturn(orders);

        var requestBuilder = get("/orders/toConfirm");
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id").value(1))
                .andExpect(jsonPath("$.[0].timestamp").value("2024-12-12T12:12:12"))
                .andExpect(jsonPath("$.[0].people_count").value(1))
                .andExpect(jsonPath("$.[0].status").value(false))
                .andExpect(jsonPath("$.[0].karaoke.room_num").value(1))
                .andExpect(jsonPath("$.[0].karaoke.seats").value(1))
                .andExpect(jsonPath("$.[0].karaoke.booked").value(true));
    }

    @Test
    @WithMockUser(username = "email@gmail.com", password = "1234", authorities = "WAITER")
    @DisplayName("Test for GET /orders/toConfirm(no orders to confirm)")
    void testGetOrdersToConfirmEndpoint_NoOrdersToConfirm() throws Exception {
        when(ordersApiService.getOrdersToConfirm())
                .thenThrow(new GetOrdersException("No order needs confirmation!"));

        var requestBuilder = get("/orders/toConfirm");
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.note").value("No order needs confirmation!"));
    }

    @Test
    @WithMockUser(username = "email@gmail.com", password = "1234", authorities = "WAITER")
    @DisplayName("Test for POST /order/confirm")
    void testConfirmOrderEndpoint() throws Exception {
        when(ordersApiService.confirmOrder(1L))
                .thenReturn(new ResponseEntity<>("Order successfully confirmed!", HttpStatus.OK));

        var requestBuilder = post("/order/confirm?id=1");
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().bytes("Order successfully confirmed!".getBytes()));
    }

    @Test
    @WithMockUser(username = "email@gmail.com", password = "1234", authorities = "WAITER")
    @DisplayName("Test for POST /order/confirm(incorrect id)")
    void testConfirmOrderEndpoint_IncorrectId() throws Exception {
        when(ordersApiService.confirmOrder(1L))
                .thenReturn(new ResponseEntity<>("The order with this ID does not exist!", HttpStatus.BAD_REQUEST));

        var requestBuilder = post("/order/confirm?id=1");
        mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(content().bytes("The order with this ID does not exist!".getBytes()));
    }

    @Test
    @WithMockUser(username = "email@gmail.com", password = "1234", authorities = "WAITER")
    @DisplayName("Test for POST /order/confirm(order already confirmed)")
    void testConfirmOrderEndpoint_AlreadyConfirmed() throws Exception {
        when(ordersApiService.confirmOrder(1L))
                .thenReturn(new ResponseEntity<>("The order has already been confirmed!", HttpStatus.BAD_REQUEST));

        var requestBuilder = post("/order/confirm?id=1");
        mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(content().bytes("The order has already been confirmed!".getBytes()));
    }

    @Test
    @WithMockUser(username = "email@gmail.com", password = "1234", authorities = "ADMIN")
    @DisplayName("Test for GET /orders")
    void testGetOrdersEndpoint() throws Exception {
        var orders = List.of(OrderDTO.builder()
                .id(1L)
                .status(true)
                .people_count(3)
                .timestamp(LocalDateTime.parse("2024-12-12T12:12:12"))
                .karaoke(new KaraokeDTO(2, 3, true))
                .build());

        when(ordersApiService.getOrders()).thenReturn(orders);

        var requestBuilder = get("/orders");
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id").value(1))
                .andExpect(jsonPath("$.[0].timestamp").value("2024-12-12T12:12:12"))
                .andExpect(jsonPath("$.[0].people_count").value(3))
                .andExpect(jsonPath("$.[0].status").value(true))
                .andExpect(jsonPath("$.[0].karaoke.room_num").value(2))
                .andExpect(jsonPath("$.[0].karaoke.seats").value(3))
                .andExpect(jsonPath("$.[0].karaoke.booked").value(true));
    }

    @Test
    @WithMockUser(username = "email@gmail.com", password = "1234", authorities = "ADMIN")
    @DisplayName("Test for GET /orders(no orders)")
    void testGetOrdersEndpoint_NoOrders() throws Exception {
        when(ordersApiService.getOrders())
                .thenThrow(new GetOrdersException("No orders!"));

        var requestBuilder = get("/orders");
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.note").value("No orders!"));
    }

    @Test
    @WithMockUser(username = "email@gmail.com", password = "1234", authorities = "ADMIN")
    @DisplayName("Test for POST /order/cancel")
    void testCancelOrderEndpoint() throws Exception {
        when(ordersApiService.cancelOrder(1L))
                .thenReturn(new ResponseEntity<>("Order canceled", HttpStatus.OK));

        var requestBuilder = post("/order/cancel?id=1");
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().bytes("Order canceled".getBytes()));
    }

    @Test
    @WithMockUser(username = "email@gmail.com", password = "1234", authorities = "ADMIN")
    @DisplayName("Test for POST /order/cancel(incorrect id)")
    void testCancelOrderEndpoint_IncorrectId() throws Exception {
        when(ordersApiService.cancelOrder(1L))
                .thenReturn(new ResponseEntity<>("The order with this ID does not exist", HttpStatus.BAD_REQUEST));

        var requestBuilder = post("/order/cancel?id=1");
        mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest())
                .andExpect(content().bytes("The order with this ID does not exist".getBytes()));
    }

    @Test
    @WithMockUser(username = "email@gmail.com", password = "1234", authorities = "ADMIN")
    @DisplayName("Validation test")
    void testValidation() throws Exception {
        var requestNoOptions = post("/order/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"order\":[],\"timestamp\":\"2024-12-12T12:12:12\",\"people_count\":1}");
        mockMvc.perform(requestNoOptions)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Choose at least one option!"));

        var requestNoTime = post("/order/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"order\":[\"karaoke\"],\"people_count\":1}");
        mockMvc.perform(requestNoTime)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Choose the date and time of your reservation!"));

        var requestZeroPeople = post("/order/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"order\":[\"karaoke\"],\"timestamp\":\"2024-12-12T12:12:12\",\"people_count\":0}");
        mockMvc.perform(requestZeroPeople)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("At least one person must be present!"));

        var requestBlank = post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "email":"email@gmail.com",
                        "password":"1234",
                        "role":"user"
                        }""");
        mockMvc.perform(requestBlank)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Write down your name!"));
    }
}
