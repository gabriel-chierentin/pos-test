package com.postest.application.dtos;

import com.postest.domain.enums.TerminalRequestStatus;
import com.postest.domain.enums.TerminalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TerminalRequestDto {
    private UUID id;
    private String customerId;
    private TerminalType terminalType;
    private AddressDto address;
    private TerminalRequestStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

