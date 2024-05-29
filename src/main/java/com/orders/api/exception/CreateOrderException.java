package com.orders.api.exception;

public class CreateOrderException extends RuntimeException {
    public CreateOrderException(String msg) {
        super(msg);
    }
}
