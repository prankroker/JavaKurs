package com.orders.api.service;

import com.orders.api.configuration.Mapper;
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

import lombok.AllArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class OrdersApiService
{
    private final UserRepository userRepository;
    private final RealTableRepository realTableRepository;
    private final KaraokeRepository karaokeRepository;
    private final OrderRepository orderRepository;

    public OrderDetailsDTO getOrderVariants()
    {
        List<KaraokeDTO> karaokeDTOS = karaokeRepository.findAll()
                .stream()
                .map(Mapper::mapToKaraokeDTO)
                .toList();
        List<RealTableDTO> realTableDTOS = realTableRepository.findAll()
                .stream()
                .map(Mapper::mapToRealTableDTO)
                .toList();

        return new OrderDetailsDTO(karaokeDTOS,realTableDTOS);
    }

    public List<UserDTO> getUsers(){
        return userRepository.findAll()
                .stream()
                .map(Mapper::mapToUserDTO)
                .toList();
    }

    @Transactional
    public OrderHistoryDTO createOrder(OrderCreateDTO orderCreateDTO, User user)
    {
        Order order = orderFactory(orderCreateDTO);
        order.setUser(user);
        order.setTimestamp(orderCreateDTO.getTimestamp());
        order.setPeople_count(orderCreateDTO.getPeople_count());
        order.setStatus(false);

        //don't like this part
        orderRepository.save(order);

        return Mapper.mapToOrderHistory(order);
    }

    private Order orderFactory(OrderCreateDTO orderCreateDTO)
    {
        Order order = new Order();

        for (String option : orderCreateDTO.getOrder())
        {
            if ("karaoke".equals(option) && order.getKaraoke() == null)
            {
                Karaoke karaoke = karaokeRepository.findAvailable(orderCreateDTO.getPeople_count())
                        .orElseThrow(() -> new CreateOrderException("There are no free karaoke seats!"));
                karaoke.setBooked(true);
                order.setKaraoke(karaoke);
                karaokeRepository.save(karaoke);
            } else if ("table".equals(option) && order.getRealTable() == null)
            {
                RealTable table = realTableRepository.findAvailable(orderCreateDTO.getPeople_count())
                        .orElseThrow(() -> new CreateOrderException("There are no free table seats!"));
                table.setBooked(true);
                order.setRealTable(table);
                realTableRepository.save(table);
            } else
            {
                throw new CreateOrderException("The wrong option was selected or" +
                        " the same option was selected twice!");
            }
        }

        return order;
    }

    public List<OrderDTO> getOrdersToConfirm() {
        List<Order> ordersToConfirm = orderRepository.ordersToConfirm();
        if (ordersToConfirm.isEmpty()) {
            throw new GetOrdersException("No order needs confirmation!");
        }

        return ordersToConfirm.stream()
                .map(Mapper::mapToOrderDTO)
                .toList();
    }

    public ResponseEntity<String> confirmOrder(Long id) {
        Optional<Order> potentialOrder = orderRepository.findById(id);
        //potentially we don't need this if statements
        if (potentialOrder.isEmpty()) {
            return new ResponseEntity<>("The order with this ID does not exist!", HttpStatus.BAD_REQUEST);
        }

        Order order = potentialOrder.get();
        if (order.getStatus()) {
            return new ResponseEntity<>("The order has already been confirmed!", HttpStatus.BAD_REQUEST);
        }

        order.setStatus(true);
        orderRepository.save(order);

        return new ResponseEntity<>("Order successfully confirmed!", HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<String> cancelOrder(Long id){
        Optional<Order> orderToCancel = orderRepository.findById(id);

        if (orderToCancel.isEmpty()){
            return new ResponseEntity<>("The order with this ID does not exist", HttpStatus.BAD_REQUEST);
        }

        Order order = orderToCancel.get();

        if (order.getKaraoke() != null){
            order.getKaraoke().setBooked(false);
            karaokeRepository.save(order.getKaraoke());
        }
        if (order.getRealTable() != null){
            order.getRealTable().setBooked(false);
            realTableRepository.save(order.getRealTable());
        }
        orderRepository.delete(order);
        return new ResponseEntity<>("Order canceled",HttpStatus.OK);
    }

    public List<OrderDTO> getOrders() {
        List<Order> orders = orderRepository.findAll();
        if (orders.isEmpty()) {
            throw new GetOrdersException("No orders!");
        }

        return orders.stream()
                .map(Mapper::mapToOrderDTO)
                .toList();
    }
}
