package com.postest.application.dtos;

import com.postest.domain.enums.TerminalType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTerminalRequestDto {
    @NotBlank(message = "Customer ID is required")
    private String customerId;

    @NotNull(message = "Terminal Type is required")
    private TerminalType terminalType;

    @Valid
    @NotNull(message = "Address is required")
    private AddressDto address;
}

