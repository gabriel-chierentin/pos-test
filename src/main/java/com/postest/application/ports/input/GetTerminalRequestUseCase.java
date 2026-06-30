package com.postest.application.ports.input;

import com.postest.application.dtos.TerminalRequestDto;

import java.util.UUID;

public interface GetTerminalRequestUseCase {
    TerminalRequestDto getTerminalRequest(UUID requestId);
}

