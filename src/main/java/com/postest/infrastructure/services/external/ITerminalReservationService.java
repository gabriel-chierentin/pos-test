package com.postest.infrastructure.services.external;

import java.util.UUID;

public interface ITerminalReservationService {
    void reserveTerminal(UUID terminalId, String customerId);
}

