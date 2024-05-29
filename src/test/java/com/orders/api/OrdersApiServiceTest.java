package com.orders.api;

import com.orders.api.configuration.security.Role;
import com.orders.api.dto.request.OrderCreateDTO;
import com.orders.api.dto.response.*;
import com.orders.api.exception.CreateOrderException;
import com.orders.api.exception.GetOrdersException;
import com.orders.api.model.Karaoke;
import com.orders.api.model.Order;
import com.orders.api.model.RealTable;
import com.orders.api.model.User;
import com.orders.api.repository.KaraokeRepository;
import com.orders.api.repository.OrderRepository;
import com.orders.api.repository.RealTableRepository;
import com.orders.api.repository.UserRepository;
import com.orders.api.service.OrdersApiService;

import org.assertj.core.data.Index;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
public class OrdersApiServiceTest {
    @Mock
    UserRepository userRepository;
    @Mock
    OrderRepository orderRepository;
    @Mock
    KaraokeRepository karaokeRepository;
    @Mock
    RealTableRepository realTableRepository;

    @InjectMocks
    OrdersApiService ordersApiService;

    @Test
    @DisplayName("Test for getOrderVariants() method")
    void testGetOrderVariants() {
        var expect = new OrderDetailsDTO(
                List.of(new KaraokeDTO(1, 1, false),
                        new KaraokeDTO(2, 2, true)),
                List.of(new RealTableDTO(1, 1, true))
        );

        when(karaokeRepository.findAll())
                .thenReturn(List.of(
                        Karaoke.builder().room_num(1).seats(1).booked(false).build(),
                        Karaoke.builder().room_num(2).seats(2).booked(true).build()
                ));
        when(realTableRepository.findAll())
                .thenReturn(List.of(
                        RealTable.builder().table_num(1).seats(1).booked(true).build()
                ));

        assertThat(ordersApiService.getOrderVariants())
                .isEqualTo(expect);
    }

    @Test
    @DisplayName("Test for getUsers() method")
    void testGetUsers() {
        var expect = List.of(UserDTO.builder()
                .name("John")
                .email("email@gmail.com")
                .password("1234")
                .role("ADMIN")
                .build());

        when(userRepository.findAll())
                .thenReturn(List.of(
                        User.builder()
                            .name("John")
                            .email("email@gmail.com")
                            .password("1234")
                            .role(Role.ADMIN)
                            .build()));

        assertThat(ordersApiService.getUsers())
                .isEqualTo(expect);
    }

    @Test
    @DisplayName("Test for createOrder() method")
    void testCreateOrder() {
        var orderCreate = OrderCreateDTO.builder()
                .order(List.of("karaoke", "table"))
                .timestamp(LocalDateTime.parse("2024-12-12T12:12:12"))
                .people_count(1)
                .build();
        var user = User.builder()
                .name("John")
                .email("email@gmail.com")
                .password("1234")
                .role(Role.ADMIN)
                .build();
        var expect = OrderHistoryDTO.builder()
                .status(false)
                .people_count(1)
                .timestamp(LocalDateTime.parse("2024-12-12T12:12:12"))
                .karaokeDTO(new KaraokeDTO(1, 2, true))
                .realTableDTO(new RealTableDTO(2, 1, true))
                .build();

        when(karaokeRepository.findAvailable(1))
                .thenReturn(Optional.ofNullable(Karaoke.builder().room_num(1).seats(2).booked(false).build()));
        when(realTableRepository.findAvailable(1))
                .thenReturn(Optional.ofNullable(RealTable.builder().table_num(2).seats(1).booked(false).build()));

        assertThat(ordersApiService.createOrder(orderCreate, user))
                .isEqualTo(expect);
    }

    @Test
    @DisplayName("Test for createOrder() method(no free seats)")
    void testCreateOrder_NoFreeSeats() {
        var orderCreate = OrderCreateDTO.builder()
                .order(List.of("karaoke"))
                .timestamp(LocalDateTime.parse("2024-12-12T12:12:12"))
                .people_count(1)
                .build();
        var user = User.builder()
                .name("John")
                .email("email@gmail.com")
                .password("1234")
                .role(Role.ADMIN)
                .build();

        assertThatThrownBy(() -> ordersApiService.createOrder(orderCreate, user))
                .isInstanceOf(CreateOrderException.class)
                .hasMessage("There are no free karaoke seats!");
    }

    @Test
    @DisplayName("Test for createOrder() method(wrong option)")
    void testCreateOrder_WrongOption() {
        var orderCreate = OrderCreateDTO.builder()
                .order(List.of("cinema"))
                .timestamp(LocalDateTime.parse("2024-12-12T12:12:12"))
                .people_count(1)
                .build();
        var user = User.builder()
                .name("John")
                .email("email@gmail.com")
                .password("1234")
                .role(Role.ADMIN)
                .build();

        assertThatThrownBy(() -> ordersApiService.createOrder(orderCreate, user))
                .isInstanceOf(CreateOrderException.class)
                .hasMessage("The wrong option was selected or" +
                        " the same option was selected twice!");
    }

    @Test
    @DisplayName("Test for getOrdersToConfirm() method")
    void testGetOrdersToConfirm() {
        var expect = OrderDTO.builder()
                .id(1L)
                .status(false)
                .people_count(1)
                .timestamp(LocalDateTime.parse("2024-12-12T12:12:12"))
                .karaoke(new KaraokeDTO(1, 2, true))
                .table(new RealTableDTO(2, 1, true))
                .build();

        when(orderRepository.ordersToConfirm())
                .thenReturn(List.of(Order.builder()
                        .order_id(1L)
                        .status(false)
                        .people_count(1)
                        .timestamp(LocalDateTime.parse("2024-12-12T12:12:12"))
                        .karaoke(Karaoke.builder().room_num(1).seats(2).booked(true).build())
                        .realTable(RealTable.builder().table_num(2).seats(1).booked(true).build())
                        .build()));

        assertThat(ordersApiService.getOrdersToConfirm())
                .hasOnlyElementsOfType(OrderDTO.class)
                .hasSize(1)
                .contains(expect, Index.atIndex(0));
    }

    @Test
    @DisplayName("Test for confirmOrder() method")
    void testConfirmOrder() {
        var expect = new ResponseEntity<>("Order successfully confirmed!", HttpStatus.OK);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.ofNullable(Order.builder()
                        .order_id(1L)
                        .status(false)
                        .people_count(1)
                        .timestamp(LocalDateTime.parse("2024-12-12T12:12:12"))
                        .karaoke(Karaoke.builder().room_num(1).seats(2).booked(true).build())
                        .realTable(RealTable.builder().table_num(2).seats(1).booked(true).build())
                        .build()));

        assertThat(ordersApiService.confirmOrder(1L))
                .isEqualTo(expect);
    }

    @Test
    @DisplayName("Test for confirmOrder() method(incorrect id)")
    void testConfirmOrder_IncorrectId() {
        var expect = new ResponseEntity<>("The order with this ID does not exist!", HttpStatus.BAD_REQUEST);

        assertThat(ordersApiService.confirmOrder(1L))
                .isEqualTo(expect);
    }

    @Test
    @DisplayName("Test for confirmOrder() method(order already confirmed)")
    void testConfirmOrder_AlreadyConfirmed() {
        var expect = new ResponseEntity<>("The order has already been confirmed!", HttpStatus.BAD_REQUEST);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.ofNullable(Order.builder()
                        .order_id(1L)
                        .status(true)
                        .people_count(1)
                        .timestamp(LocalDateTime.parse("2024-12-12T12:12:12"))
                        .karaoke(Karaoke.builder().room_num(1).seats(2).booked(true).build())
                        .realTable(RealTable.builder().table_num(2).seats(1).booked(true).build())
                        .build()));

        assertThat(ordersApiService.confirmOrder(1L))
                .isEqualTo(expect);
    }

    @Test
    @DisplayName("Test for getOrdersToConfirm() method(no orders to confirm)")
    void testGetOrdersToConfirm_NoOrdersToConfirm() {
        assertThatThrownBy(() -> ordersApiService.getOrdersToConfirm())
                .isInstanceOf(GetOrdersException.class)
                .hasMessage("No order needs confirmation!");
    }

    @Test
    @DisplayName("Test for getOrders() method")
    void testGetOrders() {
        var expect = OrderDTO.builder()
                .id(1L)
                .status(true)
                .people_count(1)
                .timestamp(LocalDateTime.parse("2024-12-12T12:12:12"))
                .karaoke(new KaraokeDTO(1, 2, true))
                .table(new RealTableDTO(2, 1, true))
                .build();

        when(orderRepository.ordersToConfirm())
                .thenReturn(List.of(Order.builder()
                        .order_id(1L)
                        .status(true)
                        .people_count(1)
                        .timestamp(LocalDateTime.parse("2024-12-12T12:12:12"))
                        .karaoke(Karaoke.builder().room_num(1).seats(2).booked(true).build())
                        .realTable(RealTable.builder().table_num(2).seats(1).booked(true).build())
                        .build()));

        assertThat(ordersApiService.getOrdersToConfirm())
                .hasOnlyElementsOfType(OrderDTO.class)
                .hasSize(1)
                .contains(expect, Index.atIndex(0));
    }

    @Test
    @DisplayName("Test for getOrders() method(no orders)")
    void testGetOrders_NoOrders() {
        assertThatThrownBy(() -> ordersApiService.getOrders())
                .isInstanceOf(GetOrdersException.class)
                .hasMessage("No orders!");
    }

    @Test
    @DisplayName("Test for cancelOrder() method")
    void testCancelOrder() {
        var expect = new ResponseEntity<>("Order canceled", HttpStatus.OK);

        when(orderRepository.findById(1L))
                .thenReturn(Optional.ofNullable(Order.builder()
                        .order_id(1L)
                        .status(false)
                        .people_count(1)
                        .timestamp(LocalDateTime.parse("2024-12-12T12:12:12"))
                        .karaoke(Karaoke.builder().room_num(1).seats(2).booked(true).build())
                        .realTable(RealTable.builder().table_num(2).seats(1).booked(true).build())
                        .build()));

        assertThat(ordersApiService.cancelOrder(1L))
                .isEqualTo(expect);
    }

    @Test
    @DisplayName("Test for cancelOrder() method(incorrect id)")
    void testCancelOrder_IncorrectId() {
        var expect = new ResponseEntity<>("The order with this ID does not exist", HttpStatus.BAD_REQUEST);

        assertThat(ordersApiService.cancelOrder(1L))
                .isEqualTo(expect);
    }
}
