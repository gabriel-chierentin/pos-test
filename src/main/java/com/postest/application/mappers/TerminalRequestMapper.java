package com.postest.application.mappers;

import com.postest.application.dtos.CreateTerminalRequestDto;
import com.postest.application.dtos.TerminalRequestDto;
import com.postest.domain.entities.Address;
import com.postest.domain.entities.TerminalRequest;
import com.postest.domain.enums.TerminalRequestStatus;
import org.springframework.stereotype.Component;

@Component
public class TerminalRequestMapper {

    private final AddressMapper addressMapper;

    public TerminalRequestMapper(AddressMapper addressMapper) {
        this.addressMapper = addressMapper;
    }

    public TerminalRequestDto toDto(TerminalRequest entity) {
        if (entity == null) {
            return null;
        }

        return TerminalRequestDto.builder()
                .id(entity.getId())
                .customerId(entity.getCustomerId())
                .terminalType(entity.getTerminalType())
                .address(addressMapper.toDto(entity.getAddress()))
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public TerminalRequest toDomain(TerminalRequestDto dto) {
        if (dto == null) {
            return null;
        }

        return TerminalRequest.builder()
                .id(dto.getId())
                .customerId(dto.getCustomerId())
                .terminalType(dto.getTerminalType())
                .address(addressMapper.toDomain(dto.getAddress()))
                .status(dto.getStatus())
                .build();
    }

    public TerminalRequest fromCreateDto(CreateTerminalRequestDto dto) {
        if (dto == null) {
            return null;
        }

        Address address = addressMapper.toDomain(dto.getAddress());

        return TerminalRequest.builder()
                .customerId(dto.getCustomerId())
                .terminalType(dto.getTerminalType())
                .address(address)
                .status(TerminalRequestStatus.SOLICITADO)
                .build();
    }
}

