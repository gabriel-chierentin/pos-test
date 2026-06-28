package com.postest.presentation.controllers;

import com.postest.application.dtos.CreateTerminalRequestDto;
import com.postest.application.dtos.TerminalRequestDto;
import com.postest.application.services.TerminalRequestFacadeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/terminal-requests")
public class TerminalRequestController {

    private final TerminalRequestFacadeService terminalRequestFacadeService;

    public TerminalRequestController(TerminalRequestFacadeService terminalRequestFacadeService) {
        this.terminalRequestFacadeService = terminalRequestFacadeService;
    }

    @PostMapping
    public ResponseEntity<TerminalRequestDto> createTerminalRequest(@Valid @RequestBody CreateTerminalRequestDto dto) {
        TerminalRequestDto response = terminalRequestFacadeService.createTerminalRequest(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @GetMapping("/{id}")
    public ResponseEntity<TerminalRequestDto> getTerminalRequest(@PathVariable UUID id) {
        TerminalRequestDto response = terminalRequestFacadeService.getTerminalRequest(id);
        return ResponseEntity.ok(response);
    }
}

