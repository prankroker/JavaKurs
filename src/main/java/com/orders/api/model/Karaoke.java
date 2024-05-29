package com.orders.api.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "karaoke")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Karaoke
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long karaoke_id;
    private Integer room_num;
    private Integer seats;
    private Boolean booked;
}
