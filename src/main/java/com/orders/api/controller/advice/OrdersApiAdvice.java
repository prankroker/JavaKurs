package com.orders.api.controller.advice;

import com.orders.api.exception.CreateOrderException;
import com.orders.api.exception.GetOrdersException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.Optional;

@RestControllerAdvice
public class OrdersApiAdvice {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleArgumentNotValid(MethodArgumentNotValidException exception) {
        Optional<String> message = Optional.ofNullable(
                exception.getBindingResult().getFieldErrors().get(0).getDefaultMessage());
        return Map.of("error", message.orElse(exception.getMessage()));
    }

    @ExceptionHandler(CreateOrderException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleCreateOrderException(CreateOrderException exception) {
        return Map.of("error", exception.getMessage());
    }

    @ExceptionHandler(GetOrdersException.class)
    @ResponseStatus(HttpStatus.OK)
    public Map<String, String> handleGetOrderException(GetOrdersException exception) {
        return Map.of("note", exception.getMessage());
    }
}
