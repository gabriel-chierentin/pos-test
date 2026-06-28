package com.postest.infrastructure.services.external.impl;

import com.postest.application.dtos.DeliveryScheduleDto;
import com.postest.application.exceptions.LogisticsException;
import com.postest.infrastructure.services.external.FakeLogisticsService;
import org.springframework.stereotype.Service;
import com.postest.domain.entities.Address;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class FakeLogisticsServiceImpl implements FakeLogisticsService {

    private final AtomicInteger callCounter = new AtomicInteger(0);

    @Override
    public DeliveryScheduleDto scheduleDelivery(UUID terminalId, String customerId, Address address, LocalDateTime scheduledDate) {
        // Falha para datas no passado
        if (scheduledDate.isBefore(LocalDateTime.now())) {
            throw new LogisticsException("Scheduled date cannot be in the past");
        }

        // Simula falha a cada 3 chamadas (para testes)
        // Descomente a linha abaixo para ativar essa simulação
        // if (callCounter.incrementAndGet() % 3 == 0) {
        //     throw new LogisticsException("Simulated logistics failure");
        // }

        return DeliveryScheduleDto.builder()
                .id(UUID.randomUUID())
                .terminalId(terminalId)
                .customerId(customerId)
                .address(address)
                .scheduledDate(scheduledDate)
                .build();
    }
}

