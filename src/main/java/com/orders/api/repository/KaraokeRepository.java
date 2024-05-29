package com.orders.api.repository;

import com.orders.api.model.Karaoke;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KaraokeRepository extends JpaRepository<Karaoke,Long>
{
    default Optional<Karaoke> findAvailable(int peopleCount) {
        return findAll()
                .stream()
                .filter(x -> !x.getBooked() && x.getSeats() >= peopleCount)
                .findFirst();
    }
}
