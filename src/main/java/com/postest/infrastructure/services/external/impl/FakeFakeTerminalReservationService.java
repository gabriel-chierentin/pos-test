package com.postest.infrastructure.services.external.impl;

import com.postest.application.exceptions.TerminalUnavailableException;
import com.postest.domain.entities.Terminal;
import com.postest.infrastructure.repositories.TerminalRepository;
import com.postest.infrastructure.services.external.FakeTerminalReservationService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class FakeFakeTerminalReservationService implements FakeTerminalReservationService {

    private final TerminalRepository terminalRepository;

    public FakeFakeTerminalReservationService(TerminalRepository terminalRepository) {
        this.terminalRepository = terminalRepository;
    }

    @Override
    public void reserveTerminal(UUID terminalId, String customerId) {
        Terminal terminal = terminalRepository.findById(terminalId)
                .orElseThrow(() -> new TerminalUnavailableException("Terminal not found"));

        if (!terminal.getIsAvailable()) {
            throw new TerminalUnavailableException("Terminal is already reserved");
        }

        terminal.setIsAvailable(false);
        terminal.setReservedBy(customerId);
        terminalRepository.save(terminal);
    }
}

