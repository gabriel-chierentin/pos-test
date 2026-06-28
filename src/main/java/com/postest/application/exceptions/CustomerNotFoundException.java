package com.postest.application.exceptions;

public class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException(String customerId) {
        super("Customer not found: " + customerId);
    }
}

