package com.orders.api.repository;

import com.orders.api.model.Order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order,Long>
{
    default List<Order> ordersToConfirm() {
        return findAll()
                .stream()
                .filter(x -> !x.getStatus())
                .toList();
    }
}
