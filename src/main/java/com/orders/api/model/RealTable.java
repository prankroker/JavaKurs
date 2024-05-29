package com.orders.api.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "realtable")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class RealTable
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long table_id;
    private Integer table_num;
    private Integer seats;
    private Boolean booked;
}
