package com.postest.infrastructure.adapters.output.external;

import com.postest.application.exceptions.TerminalUnavailableException;
import com.postest.application.ports.output.TerminalReservationServicePort;
import com.postest.domain.enums.TerminalType;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class FakeTerminalReservationAdapter implements TerminalReservationServicePort {

    @Override
    public UUID reserveTerminal(TerminalType terminalType, String customerId) {
        if (terminalType == null || customerId == null) {
            throw new TerminalUnavailableException("Invalid terminal type or customer");
        }

        if ("CUST-789".equals(customerId)) {
            throw new TerminalUnavailableException("No available terminal of type " + terminalType);
        }

        return UUID.randomUUID();
    }
}

