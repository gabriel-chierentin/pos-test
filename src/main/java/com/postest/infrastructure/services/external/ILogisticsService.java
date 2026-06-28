package com.postest.infrastructure.services.external;

import com.postest.application.dtos.DeliveryScheduleDto;
import com.postest.domain.entities.Address;

import java.time.LocalDateTime;
import java.util.UUID;

public interface ILogisticsService {
    DeliveryScheduleDto scheduleDelivery(UUID terminalId, String customerId, Address address, LocalDateTime scheduledDate);
}

