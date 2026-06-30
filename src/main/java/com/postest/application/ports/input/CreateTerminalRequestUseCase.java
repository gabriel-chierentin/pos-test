package com.postest.application.ports.input;

import com.postest.application.dtos.CreateTerminalRequestDto;
import com.postest.application.dtos.TerminalRequestDto;

public interface CreateTerminalRequestUseCase {
    TerminalRequestDto createTerminalRequest(CreateTerminalRequestDto dto);
}

