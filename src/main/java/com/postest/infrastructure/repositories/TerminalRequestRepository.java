package com.postest.infrastructure.repositories;

import com.postest.domain.entities.TerminalRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TerminalRequestRepository extends JpaRepository<TerminalRequest, UUID> {
}

