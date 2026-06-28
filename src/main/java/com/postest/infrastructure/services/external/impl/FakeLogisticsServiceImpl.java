package com.postest.infrastructure.services.external.impl;

import com.postest.application.dtos.DeliveryScheduleDto;
import com.postest.application.exceptions.LogisticsException;
import com.postest.infrastructure.services.external.FakeLogisticsService;
import org.springframework.stereotype.Service;
import com.postest.domain.entities.Address;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class FakeLogisticsServiceImpl implements FakeLogisticsService {

    @Override
    public DeliveryScheduleDto scheduleDelivery(UUID terminalId, String customerId, Address address, LocalDateTime scheduledDate) {
        // Validação básica
        if (scheduledDate == null || customerId == null || terminalId == null || address == null) {
            throw new LogisticsException("Invalid delivery parameters");
        }

        // Simula falha de logística para cliente específico (para testes)
        if ("CUST-321".equals(customerId)) {
            throw new LogisticsException("Logistics service unavailable for this region");
        }

        // Retorna agendamento mockado para outros casos
        return DeliveryScheduleDto.builder()
                .id(UUID.randomUUID())
                .terminalId(terminalId)
                .customerId(customerId)
                .address(address)
                .scheduledDate(scheduledDate)
                .build();
    }
}

