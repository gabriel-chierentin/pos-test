package com.postest.infrastructure.services.external;

import com.postest.domain.enums.TerminalType;

import java.util.UUID;

public interface FakeTerminalReservationService {
    UUID reserveTerminal(TerminalType terminalType, String customerId);
}

