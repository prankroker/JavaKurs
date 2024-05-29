package com.orders.api;

import com.orders.api.configuration.security.Role;
import com.orders.api.model.Karaoke;
import com.orders.api.model.Order;
import com.orders.api.model.RealTable;
import com.orders.api.model.User;
import com.orders.api.repository.KaraokeRepository;
import com.orders.api.repository.OrderRepository;
import com.orders.api.repository.RealTableRepository;
import com.orders.api.repository.UserRepository;

import org.assertj.core.data.Index;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(locations = {"classpath:testApp.properties"})
public class OrdersApiRepositoryTest {
    @Autowired
    OrderRepository orderRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RealTableRepository realTableRepository;

    @Autowired
    KaraokeRepository karaokeRepository;

    @Autowired
    TestEntityManager entityManager;

    @Test
    @DisplayName("Test for save() method in UserRepository")
    void saveTest_UserRepository() {
        User expect = User.builder()
                .name("John")
                .email("email@gmail.com")
                .password("1234")
                .role(Role.USER)
                .build();
        userRepository.save(expect);

        entityManager.clear(); //to clear cache

        Optional<User> actual = userRepository.findById(expect.getUser_id());
        assertThat(actual)
                .isPresent()
                .get()
                .isEqualTo(expect);
    }

    @Test
    @DisplayName("Test for findByEmail() method in UserRepository")
    @Sql(statements = {"INSERT INTO user(user_id, name, email, password, role) " +
            "VALUES (1, 'John', 'email@gmail.com', '1234', 0)"})
    void findByEmailTest_UserRepository() {
        User expect = User.builder()
                .user_id(1L)
                .name("John")
                .email("email@gmail.com")
                .password("1234")
                .role(Role.USER)
                .build();

        Optional<User> actual = userRepository.findUserByEmail("email@gmail.com");
        assertThat(actual)
                .isPresent()
                .get()
                .isEqualTo(expect);
    }

    @Test
    @DisplayName("Test for save() method in OrderRepository")
    @Sql(statements = {"UPDATE karaoke SET booked = true WHERE karaoke_id = 1"})
    void saveTest_OrderRepository() {
        var expect = Order.builder()
                .status(false)
                .timestamp(LocalDateTime.parse("2024-12-12T12:12:12"))
                .people_count(2)
                .karaoke(Karaoke.builder().karaoke_id(1L).room_num(1).seats(2).booked(true).build())
                .build();
        orderRepository.save(expect);

        entityManager.clear(); //to clear cache

        Optional<Order> actual = orderRepository.findById(expect.getOrder_id());
        assertThat(actual)
                .isPresent()
                .get()
                .isEqualTo(expect);
    }

    @Test
    @DisplayName("Test for ordersToConfirm() method in OrderRepository")
    @Sql(statements = {"INSERT INTO user(user_id, name, email, password, role) " +
                "VALUES (2, 'John', 'email@gmail.com', '1234', 0)",
            "INSERT INTO orders(order_id, people_count, status, timestamp, karaoke_id, table_id, user_id) " +
                "VALUES (1, 2, true, '2024-12-12T12:12:12', null, null, null), (2, 2, false, '2024-12-12T12:12:12', 1, 2, 2)",
            "UPDATE karaoke SET booked = true WHERE karaoke_id = 1",
            "UPDATE realtable SET booked = true WHERE table_id = 2",})
    void ordersToConfirmTest_OrderRepository() {
        var expect = Order.builder()
                .order_id(2L)
                .status(false)
                .timestamp(LocalDateTime.parse("2024-12-12T12:12:12"))
                .people_count(2)
                .karaoke(Karaoke.builder().karaoke_id(1L).room_num(1).seats(2).booked(true).build())
                .realTable(RealTable.builder().table_id(2L).table_num(2).seats(2).booked(true).build())
                .user(User.builder()
                        .user_id(2L)
                        .name("John")
                        .email("email@gmail.com")
                        .password("1234")
                        .role(Role.USER)
                        .build())
                .build();

        List<Order> actual = orderRepository.ordersToConfirm();
        assertThat(actual)
                .hasOnlyElementsOfType(Order.class)
                .hasSize(1)
                .contains(expect, Index.atIndex(0));
    }

    @Test
    @DisplayName("Test for save() method in KaraokeRepository")
    void saveTest_KaraokeRepository() {
        var expect = Karaoke.builder()
                .karaoke_id(1L)
                .room_num(1)
                .seats(2)
                .booked(true)
                .build();
        karaokeRepository.saveAndFlush(expect);

        entityManager.clear(); //to clear cache

        Optional<Karaoke> actual = karaokeRepository.findById(expect.getKaraoke_id());
        assertThat(actual)
                .isPresent()
                .get()
                .isEqualTo(expect);
    }

    @Test
    @DisplayName("Test for findAvailable() method in KaraokeRepository")
    @Sql(statements = {"UPDATE karaoke SET booked = true WHERE karaoke_id = 1"})
    void findAvailableTest_KaraokeRepository() {
        var expect = Karaoke.builder()
                .karaoke_id(2L)
                .room_num(2)
                .seats(3)
                .booked(false)
                .build();

        Optional<Karaoke> actual = karaokeRepository.findAvailable(1);
        assertThat(actual)
                .isPresent()
                .get()
                .isEqualTo(expect);
    }

    @Test
    @DisplayName("Test for save() method in RealTableRepository")
    void saveTest_RealTableRepository() {
        var expect = RealTable.builder()
                .table_id(1L)
                .table_num(1)
                .seats(3)
                .booked(true)
                .build();
        realTableRepository.saveAndFlush(expect);

        entityManager.clear(); //to clear cache

        Optional<RealTable> actual = realTableRepository.findById(expect.getTable_id());
        assertThat(actual)
                .isPresent()
                .get()
                .isEqualTo(expect);
    }

    @Test
    @DisplayName("Test for findAvailable() method in RealTableRepository")
    @Sql(statements = {"UPDATE realtable SET booked = true WHERE table_id = 1"})
    void findAvailableTest_RealTableRepository() {
        var expect = RealTable.builder()
                .table_id(2L)
                .table_num(2)
                .seats(2)
                .booked(false)
                .build();

        Optional<RealTable> actual = realTableRepository.findAvailable(1);
        assertThat(actual)
                .isPresent()
                .get()
                .isEqualTo(expect);
    }
}
