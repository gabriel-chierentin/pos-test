package com.postest.infrastructure.adapters.input.web;

import com.postest.application.dtos.CreateTerminalRequestDto;
import com.postest.application.dtos.TerminalRequestDto;
import com.postest.application.ports.input.CreateTerminalRequestUseCase;
import com.postest.application.ports.input.GetTerminalRequestUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/terminal-requests")
public class TerminalRequestController {

    private final CreateTerminalRequestUseCase createTerminalRequestUseCase;
    private final GetTerminalRequestUseCase getTerminalRequestUseCase;

    public TerminalRequestController(
            CreateTerminalRequestUseCase createTerminalRequestUseCase,
            GetTerminalRequestUseCase getTerminalRequestUseCase) {
        this.createTerminalRequestUseCase = createTerminalRequestUseCase;
        this.getTerminalRequestUseCase = getTerminalRequestUseCase;
    }

    @PostMapping
    public ResponseEntity<TerminalRequestDto> createTerminalRequest(@Valid @RequestBody CreateTerminalRequestDto dto) {
        TerminalRequestDto response = createTerminalRequestUseCase.createTerminalRequest(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TerminalRequestDto> getTerminalRequest(@PathVariable UUID id) {
        TerminalRequestDto response = getTerminalRequestUseCase.getTerminalRequest(id);
        return ResponseEntity.ok(response);
    }
}

