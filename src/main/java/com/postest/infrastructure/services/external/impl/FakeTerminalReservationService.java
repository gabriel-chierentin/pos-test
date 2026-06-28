package com.postest.infrastructure.services.external.impl;

import com.postest.application.exceptions.TerminalUnavailableException;
import com.postest.infrastructure.repositories.TerminalRepository;
import com.postest.infrastructure.services.external.ITerminalReservationService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class FakeTerminalReservationService implements ITerminalReservationService {

    private final TerminalRepository terminalRepository;

    public FakeTerminalReservationService(TerminalRepository terminalRepository) {
        this.terminalRepository = terminalRepository;
    }

    @Override
    public void reserveTerminal(UUID terminalId, String customerId) {
        var terminal = terminalRepository.findById(terminalId)
                .orElseThrow(() -> new TerminalUnavailableException("Terminal not found"));

        if (!terminal.getIsAvailable()) {
            throw new TerminalUnavailableException("Terminal is already reserved");
        }

        terminal.setIsAvailable(false);
        terminal.setReservedBy(customerId);
        terminalRepository.save(terminal);
    }
}

