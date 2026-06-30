package com.postest.application.ports.output;

import com.postest.domain.enums.TerminalType;

import java.util.UUID;

public interface TerminalReservationServicePort {
    UUID reserveTerminal(TerminalType terminalType, String customerId);
}

