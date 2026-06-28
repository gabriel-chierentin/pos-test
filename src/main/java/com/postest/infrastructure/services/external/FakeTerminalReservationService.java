package com.postest.infrastructure.services.external;

import java.util.UUID;

public interface FakeTerminalReservationService {
    void reserveTerminal(UUID terminalId, String customerId);
}

