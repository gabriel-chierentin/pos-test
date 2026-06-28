package com.postest.infrastructure.repositories;

import com.postest.domain.entities.Terminal;
import com.postest.domain.enums.TerminalType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TerminalRepository extends JpaRepository<Terminal, UUID> {
    Optional<Terminal> findByTerminalTypeAndIsAvailableTrue(TerminalType terminalType);
}

