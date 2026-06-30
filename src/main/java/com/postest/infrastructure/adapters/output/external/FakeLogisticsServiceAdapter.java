package com.postest.infrastructure.adapters.output.external;

import com.postest.application.dtos.DeliveryScheduleDto;
import com.postest.application.exceptions.LogisticsException;
import com.postest.application.ports.output.LogisticsServicePort;
import com.postest.domain.entities.Address;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class FakeLogisticsServiceAdapter implements LogisticsServicePort {

    @Override
    public DeliveryScheduleDto scheduleDelivery(UUID terminalId, String customerId, Address address, LocalDateTime scheduledDate) {
        if (scheduledDate == null || customerId == null || terminalId == null || address == null) {
            throw new LogisticsException("Invalid delivery parameters");
        }

        if ("CUST-321".equals(customerId)) {
            throw new LogisticsException("Logistics service unavailable for this region");
        }

        return DeliveryScheduleDto.builder()
                .id(UUID.randomUUID())
                .terminalId(terminalId)
                .customerId(customerId)
                .address(address)
                .scheduledDate(scheduledDate)
                .build();
    }
}

