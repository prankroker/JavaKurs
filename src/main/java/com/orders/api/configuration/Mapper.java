package com.orders.api.configuration;

import com.orders.api.dto.response.*;
import com.orders.api.model.Karaoke;
import com.orders.api.model.Order;
import com.orders.api.model.RealTable;
import com.orders.api.model.User;

public class Mapper {
    public static KaraokeDTO mapToKaraokeDTO(Karaoke karaoke) {
        return new KaraokeDTO(karaoke.getRoom_num(),
                karaoke.getSeats(),
                karaoke.getBooked());
    }

    public static RealTableDTO mapToRealTableDTO(RealTable realTable) {
        return RealTableDTO.builder()
                .table_num(realTable.getTable_num())
                .seats(realTable.getSeats())
                .booked(realTable.getBooked())
                .build();
    }

    public static OrderHistoryDTO mapToOrderHistory(Order order) {
        return OrderHistoryDTO.builder()
                .status(order.getStatus())
                .people_count(order.getPeople_count())
                .timestamp(order.getTimestamp())
                .karaokeDTO(order.getKaraoke() == null ? null : mapToKaraokeDTO(order.getKaraoke()))
                .realTableDTO(order.getRealTable() == null ? null : mapToRealTableDTO(order.getRealTable()))
                .build();
    }

    public static UserDTO mapToUserDTO(User user){
        return UserDTO.builder()
                .name(user.getName())
                .email(user.getEmail())
                .password(user.getPassword())
                .role(String.valueOf(user.getRole()))
                .build();
    }

    public static OrderDTO mapToOrderDTO(Order order) {
        return OrderDTO.builder()
                .id(order.getOrder_id())
                .timestamp(order.getTimestamp())
                .people_count(order.getPeople_count())
                .status(order.getStatus())
                .karaoke(order.getKaraoke() == null ? null : mapToKaraokeDTO(order.getKaraoke()))
                .table(order.getRealTable() == null ? null : mapToRealTableDTO(order.getRealTable()))
                .build();
    }
}
