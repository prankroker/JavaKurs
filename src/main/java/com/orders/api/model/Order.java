package com.orders.api.model;

import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Order
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long order_id;
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime timestamp;
    private Integer people_count;
    private Boolean status;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @OneToOne
    @JoinColumn(name = "table_id")
    private RealTable realTable;
    @OneToOne
    @JoinColumn(name = "karaoke_id")
    private Karaoke karaoke;
}
