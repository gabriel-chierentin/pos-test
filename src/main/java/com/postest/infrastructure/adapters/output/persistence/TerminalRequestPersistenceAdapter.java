package com.postest.infrastructure.adapters.output.persistence;

import com.postest.application.ports.output.TerminalRequestRepositoryPort;
import com.postest.domain.entities.TerminalRequest;
import com.postest.infrastructure.repositories.TerminalRequestRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class TerminalRequestPersistenceAdapter implements TerminalRequestRepositoryPort {

    private final TerminalRequestRepository jpaRepository;

    public TerminalRequestPersistenceAdapter(TerminalRequestRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public TerminalRequest save(TerminalRequest request) {
        return jpaRepository.save(request);
    }

    @Override
    public Optional<TerminalRequest> findById(UUID id) {
        return jpaRepository.findById(id);
    }
}

