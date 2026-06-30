package com.postest.application.ports.output;

import com.postest.application.dtos.DeliveryScheduleDto;
import com.postest.domain.entities.Address;

import java.time.LocalDateTime;
import java.util.UUID;

public interface LogisticsServicePort {
    DeliveryScheduleDto scheduleDelivery(UUID terminalId, String customerId, Address address, LocalDateTime scheduledDate);
}

