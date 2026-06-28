package com.postest.application.exceptions;

public class LogisticsException extends RuntimeException {
    public LogisticsException(String message) {
        super(message);
    }

    public LogisticsException(String message, Throwable cause) {
        super(message, cause);
    }
}

