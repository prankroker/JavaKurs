package com.orders.api.repository;

import com.orders.api.model.RealTable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RealTableRepository extends JpaRepository<RealTable,Long>
{
    default Optional<RealTable> findAvailable(int peopleCount) {
        return findAll()
                .stream()
                .filter(x -> !x.getBooked() && x.getSeats() >= peopleCount)
                .findFirst();
    }
}
