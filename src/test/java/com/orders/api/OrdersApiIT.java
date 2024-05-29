package com.orders.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(locations = {"classpath:testApp.properties"})
class OrdersApiIT {
	@Autowired
	MockMvc mockMvc;

	final RequestPostProcessor postProcessor = SecurityMockMvcRequestPostProcessors
			.httpBasic("email@gmail.com", "1234");
	final String createUser = "INSERT INTO user(user_id, name, email, password, role) " +
			"VALUES (1, 'John', 'email@gmail.com', '$2a$10$Hzdg8upvCxY8wqZAyq79Ou1szV6sS6Xy55GmDyOqgz8ZKbMsklZ1C', 0)";
	final String createWaiter =  "INSERT INTO user(user_id, name, email, password, role) " +
			"VALUES (1, 'John', 'email@gmail.com', '$2a$10$Hzdg8upvCxY8wqZAyq79Ou1szV6sS6Xy55GmDyOqgz8ZKbMsklZ1C', 1)";
	final String createAdmin =  "INSERT INTO user(user_id, name, email, password, role) " +
			"VALUES (1, 'John', 'email@gmail.com', '$2a$10$Hzdg8upvCxY8wqZAyq79Ou1szV6sS6Xy55GmDyOqgz8ZKbMsklZ1C', 2)";

	@Test
	@DisplayName("Test for POST /register endpoint")
	void testRegisterEndpoint() throws Exception {
		var requestBuilder = post("/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
                        {
                        "name":"John",
                        "email":"email@gmail.com",
                        "password":"1234",
                        "role":"USER"
                        }""");
		mockMvc.perform(requestBuilder)
				.andExpect(status().isOk())
				.andExpect(content().bytes("Successfully registered, your email is your username".getBytes()));
	}

	@Test
	@DisplayName("Test for POST /register endpoint(wrong role provided)")
	void testRegisterEndpoint_WrongRole() throws Exception {
		var requestBuilder = post("/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
                        {
                        "name":"John",
                        "email":"email@gmail.com",
                        "password":"1234",
                        "role":"godUser"
                        }""");
		mockMvc.perform(requestBuilder)
				.andExpect(status().isBadRequest())
				.andExpect(content().bytes("Wrong role provided".getBytes()));
	}

	@Test
	@DisplayName("Test for POST /register endpoint(such a user already registered)")
	@Sql(statements = {createUser})
	void testRegisterEndpoint_AlreadyRegistered() throws Exception {
		var requestBuilder = post("/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
                        {
                        "name":"John",
                        "email":"email@gmail.com",
                        "password":"1234",
                        "role":"user"
                        }""");
		mockMvc.perform(requestBuilder)
				.andExpect(status().isBadRequest())
				.andExpect(content().bytes("Such a user already exists!".getBytes()));
	}

	@Test
	@DisplayName("Test for GET /check/authorities")
	@Sql(statements = {createUser})
	void testTestEndpoint() throws Exception {
		var requestBuilder = get("/check/authorities").with(postProcessor);
		mockMvc.perform(requestBuilder)
				.andExpect(status().isOk())
				.andExpect(content().bytes("Correct".getBytes()));
	}

	@Test
	@DisplayName("Test for GET /orders/variants")
	@Sql(statements = {"UPDATE karaoke SET booked = true WHERE karaoke_id = 1",
			"UPDATE realtable SET booked = true WHERE table_id IN (1, 5)"})
	void testGetOrderVariantsEndpoint() throws Exception {
		var requestBuilder = get("/orders/variants");
		mockMvc.perform(requestBuilder)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.karaokeDTOS[0].room_num").value(1))
				.andExpect(jsonPath("$.karaokeDTOS[0].seats").value(2))
				.andExpect(jsonPath("$.karaokeDTOS[0].booked").value(true))
				.andExpect(jsonPath("$.karaokeDTOS[1].room_num").value(2))
				.andExpect(jsonPath("$.karaokeDTOS[1].seats").value(3))
				.andExpect(jsonPath("$.karaokeDTOS[1].booked").value(false))
				.andExpect(jsonPath("$.karaokeDTOS[2].room_num").value(3))
				.andExpect(jsonPath("$.karaokeDTOS[2].seats").value(4))
				.andExpect(jsonPath("$.karaokeDTOS[2].booked").value(false))
				.andExpect(jsonPath("$.realTableDTOS[0].table_num").value(1))
				.andExpect(jsonPath("$.realTableDTOS[0].seats").value(3))
				.andExpect(jsonPath("$.realTableDTOS[0].booked").value(true))
				.andExpect(jsonPath("$.realTableDTOS[2].table_num").value(3))
				.andExpect(jsonPath("$.realTableDTOS[2].seats").value(9))
				.andExpect(jsonPath("$.realTableDTOS[2].booked").value(false))
				.andExpect(jsonPath("$.realTableDTOS[4].table_num").value(5))
				.andExpect(jsonPath("$.realTableDTOS[4].seats").value(6))
				.andExpect(jsonPath("$.realTableDTOS[4].booked").value(true));
	}

	@Test
	@DisplayName("Test for GET /users")
	@Sql(statements = {createAdmin})
	void testGetUsersEndpoint() throws Exception {
		var requestBuilder = get("/users").with(postProcessor);
		mockMvc.perform(requestBuilder)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.[0].name").value("John"))
				.andExpect(jsonPath("$.[0].email").value("email@gmail.com"))
				.andExpect(jsonPath("$.[0].password")
						.value("$2a$10$Hzdg8upvCxY8wqZAyq79Ou1szV6sS6Xy55GmDyOqgz8ZKbMsklZ1C"))
				.andExpect(jsonPath("$.[0].role").value("ADMIN"));
	}

	@Test
	@DisplayName("Test for GET /order/create")
	@Sql(statements = {createUser})
	void testCreateOrderEndpoint() throws Exception {
		var requestBuilder = post("/order/create")
				.with(postProcessor)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"order\":[\"karaoke\"],\"timestamp\":\"2024-12-12T12:12:12\",\"people_count\":1}");
		mockMvc.perform(requestBuilder)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value(false))
				.andExpect(jsonPath("$.people_count").value(1))
				.andExpect(jsonPath("$.timestamp").value("2024-12-12T12:12:12"))
				.andExpect(jsonPath("$.karaokeDTO.room_num").value(1))
				.andExpect(jsonPath("$.karaokeDTO.seats").value(2))
				.andExpect(jsonPath("$.karaokeDTO.booked").value(true));
	}

	@Test
	@DisplayName("Test for GET /order/create(when no free seats)")
	@Sql(statements = {createUser,
			"UPDATE karaoke SET booked = true"})
	void testCreateOrderEndpoint_NoFreeSeats() throws Exception {
		var requestBuilder = post("/order/create")
				.with(postProcessor)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"order\":[\"karaoke\"],\"timestamp\":\"2024-12-12T12:12:12\",\"people_count\":1}");
		mockMvc.perform(requestBuilder)
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value("There are no free karaoke seats!"));
	}

	@Test
	@DisplayName("Test for GET /order/create(wrong option selected)")
	@Sql(statements = {createUser})
	void testCreateOrderEndpoint_WrongOption() throws Exception {
		var requestBuilder = post("/order/create")
				.with(postProcessor)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"order\":[\"cinema\"],\"timestamp\":\"2024-12-12T12:12:12\",\"people_count\":1}");
		mockMvc.perform(requestBuilder)
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value("The wrong option was selected or" +
						" the same option was selected twice!"));
	}

	@Test
	@DisplayName("Test for GET /orders/toConfirm")
	@Sql(statements = {createWaiter,
			"INSERT INTO user(user_id, name, email, password, role) " +
					"VALUES (2, 'John', 'email1@gmail.com', '1234', 0)",
			"INSERT INTO orders(order_id, people_count, status, timestamp, karaoke_id, table_id, user_id) " +
					"VALUES (1, 2, true, '2024-12-12T12:12:12', null, null, null), (2, 2, false, '2024-12-12T12:12:12', 1, 2, 2)",
			"UPDATE karaoke SET booked = true WHERE karaoke_id = 1",
			"UPDATE realtable SET booked = true WHERE table_id = 2"})
	void testGetOrdersToConfirmEndpoint() throws Exception {
		var requestBuilder = get("/orders/toConfirm").with(postProcessor);
		mockMvc.perform(requestBuilder)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.[0].id").value(2))
				.andExpect(jsonPath("$.[0].timestamp").value("2024-12-12T12:12:12"))
				.andExpect(jsonPath("$.[0].people_count").value(2))
				.andExpect(jsonPath("$.[0].status").value(false))
				.andExpect(jsonPath("$.[0].karaoke.room_num").value(1))
				.andExpect(jsonPath("$.[0].karaoke.seats").value(2))
				.andExpect(jsonPath("$.[0].karaoke.booked").value(true))
				.andExpect(jsonPath("$.[0].table.table_num").value(2))
				.andExpect(jsonPath("$.[0].table.seats").value(2))
				.andExpect(jsonPath("$.[0].table.booked").value(true));
	}

	@Test
	@DisplayName("Test for GET /orders/toConfirm(no orders to confirm)")
	@Sql(statements = {createWaiter})
	void testGetOrdersToConfirmEndpoint_NoOrdersToConfirm() throws Exception {
		var requestBuilder = get("/orders/toConfirm").with(postProcessor);
		mockMvc.perform(requestBuilder)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.note").value("No order needs confirmation!"));
	}

	@Test
	@DisplayName("Test for POST /order/confirm")
	@Sql(statements = {createWaiter,
			"INSERT INTO user(user_id, name, email, password, role) " +
					"VALUES (2, 'John', 'email1@gmail.com', '1234', 0)",
			"INSERT INTO orders(order_id, people_count, status, timestamp, karaoke_id, table_id, user_id) " +
					"VALUES (1, 2, false, '2024-12-12T12:12:12', 1, 2, 2)",
			"UPDATE karaoke SET booked = true WHERE karaoke_id = 1",
			"UPDATE realtable SET booked = true WHERE table_id = 2"})
	void testConfirmOrderEndpoint() throws Exception {
		var requestBuilder = post("/order/confirm?id=1").with(postProcessor);
		mockMvc.perform(requestBuilder)
				.andExpect(status().isOk())
				.andExpect(content().bytes("Order successfully confirmed!".getBytes()));
	}

	@Test
	@DisplayName("Test for POST /order/confirm(incorrect id)")
	@Sql(statements = {createWaiter})
	void testConfirmOrderEndpoint_IncorrectId() throws Exception {
		var requestBuilder = post("/order/confirm?id=1").with(postProcessor);
		mockMvc.perform(requestBuilder)
				.andExpect(status().isBadRequest())
				.andExpect(content().bytes("The order with this ID does not exist!".getBytes()));
	}

	@Test
	@DisplayName("Test for POST /order/confirm(order already confirmed)")
	@Sql(statements = {createWaiter,
			"INSERT INTO user(user_id, name, email, password, role) " +
					"VALUES (2, 'John', 'email1@gmail.com', '1234', 0)",
			"INSERT INTO orders(order_id, people_count, status, timestamp, karaoke_id, table_id, user_id) " +
					"VALUES (1, 2, true, '2024-12-12T12:12:12', 1, 2, 2)",
			"UPDATE karaoke SET booked = true WHERE karaoke_id = 1",
			"UPDATE realtable SET booked = true WHERE table_id = 2"})
	void testConfirmOrderEndpoint_AlreadyConfirmed() throws Exception {
		var requestBuilder = post("/order/confirm?id=1").with(postProcessor);
		mockMvc.perform(requestBuilder)
				.andExpect(status().isBadRequest())
				.andExpect(content().bytes("The order has already been confirmed!".getBytes()));
	}

	@Test
	@DisplayName("Test for GET /orders")
	@Sql(statements = {createAdmin,
			"INSERT INTO user(user_id, name, email, password, role) " +
					"VALUES (2, 'John', 'email1@gmail.com', '1234', 0)",
			"INSERT INTO orders(order_id, people_count, status, timestamp, karaoke_id, user_id) " +
					"VALUES (1, 2, true, '2024-12-12T12:12:12', 1, 2)",
			"UPDATE karaoke SET booked = true WHERE karaoke_id = 1"})
	void testGetOrdersEndpoint() throws Exception {
		var requestBuilder = get("/orders").with(postProcessor);
		mockMvc.perform(requestBuilder)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.[0].id").value(1))
				.andExpect(jsonPath("$.[0].timestamp").value("2024-12-12T12:12:12"))
				.andExpect(jsonPath("$.[0].people_count").value(2))
				.andExpect(jsonPath("$.[0].status").value(true))
				.andExpect(jsonPath("$.[0].karaoke.room_num").value(1))
				.andExpect(jsonPath("$.[0].karaoke.seats").value(2))
				.andExpect(jsonPath("$.[0].karaoke.booked").value(true));
	}

	@Test
	@DisplayName("Test for GET /orders(no orders)")
	@Sql(statements = {createAdmin})
	void testGetOrdersEndpoint_NoOrders() throws Exception {
		var requestBuilder = get("/orders").with(postProcessor);
		mockMvc.perform(requestBuilder)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.note").value("No orders!"));
	}

	@Test
	@DisplayName("Test for POST /order/cancel")
	@Sql(statements = {createAdmin,
			"INSERT INTO user(user_id, name, email, password, role) " +
					"VALUES (2, 'John', 'email1@gmail.com', '1234', 0)",
			"INSERT INTO orders(order_id, people_count, status, timestamp, karaoke_id, user_id) " +
					"VALUES (1, 2, false, '2024-12-12T12:12:12', 1, 2)",
			"UPDATE karaoke SET booked = true WHERE karaoke_id = 1"})
	void testCancelOrderEndpoint() throws Exception {
		var requestBuilder = post("/order/cancel?id=1").with(postProcessor);
		mockMvc.perform(requestBuilder)
				.andExpect(status().isOk())
				.andExpect(content().bytes("Order canceled".getBytes()));
	}

	@Test
	@DisplayName("Test for POST /order/cancel(incorrect id)")
	@Sql(statements = {createAdmin})
	void testCancelOrderEndpoint_IncorrectId() throws Exception {
		var requestBuilder = post("/order/cancel?id=1").with(postProcessor);
		mockMvc.perform(requestBuilder)
				.andExpect(status().isBadRequest())
				.andExpect(content().bytes("The order with this ID does not exist".getBytes()));
	}
}
