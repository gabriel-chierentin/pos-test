package com.postest.application.ports.output;

import com.postest.domain.entities.TerminalRequest;

import java.util.Optional;
import java.util.UUID;

public interface TerminalRequestRepositoryPort {
    TerminalRequest save(TerminalRequest request);
    Optional<TerminalRequest> findById(UUID id);
}

