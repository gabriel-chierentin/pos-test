package com.postest.infrastructure.services.external.impl;

import com.postest.application.exceptions.TerminalUnavailableException;
import com.postest.domain.enums.TerminalType;
import com.postest.infrastructure.services.external.FakeTerminalReservationService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class FakeFakeTerminalReservationService implements FakeTerminalReservationService {

    @Override
    public UUID reserveTerminal(TerminalType terminalType, String customerId) {
        // Simula reserva bem-sucedida para tipos de terminal e clientes válidos
        if (terminalType == null || customerId == null) {
            throw new TerminalUnavailableException("Invalid terminal type or customer");
        }

        // Simula cenário onde não há terminais disponíveis para determinados tipos
        if ("CUST-789".equals(customerId)) {
            throw new TerminalUnavailableException("No available terminal of type " + terminalType);
        }

        // Retorna um ID de terminal mockado para outros casos
        return UUID.randomUUID();
    }
}

