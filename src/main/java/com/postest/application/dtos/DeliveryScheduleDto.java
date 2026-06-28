package com.postest.application.dtos;

import com.postest.domain.entities.Address;
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
public class DeliveryScheduleDto {
    private UUID id;
    private UUID terminalId;
    private String customerId;
    private LocalDateTime scheduledDate;
    private Address address;
}

