package com.postest.application.exceptions;

public class TerminalRequestNotFoundException extends RuntimeException {
    public TerminalRequestNotFoundException(String id) {
        super("Terminal request not found: " + id);
    }
}

