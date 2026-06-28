package com.postest.application.services;

import com.postest.application.exceptions.LogisticsException;
import com.postest.domain.entities.TerminalRequest;
import com.postest.domain.enums.TerminalRequestStatus;
import com.postest.infrastructure.repositories.TerminalRequestRepository;
import com.postest.infrastructure.services.external.FakeLogisticsService;
import com.postest.infrastructure.utils.SleepUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class DeliverySchedulingService {

    private final FakeLogisticsService logisticsService;
    private final TerminalRequestRepository terminalRequestRepository;

    @Value("${terminal.request.step.delay:0}")
    private long stepDelay;

    public DeliverySchedulingService(
            FakeLogisticsService logisticsService,
            TerminalRequestRepository terminalRequestRepository) {
        this.logisticsService = logisticsService;
        this.terminalRequestRepository = terminalRequestRepository;
    }

    /**
     * Agenda a entrega do terminal reservado para 3 dias a partir de agora.
     * Atualiza o status do request para AGENDADO.
     *
     * @param request o terminal request com o terminal reservado
     * @throws LogisticsException se houver erro ao agendar a entrega
     */
    public void schedule(TerminalRequest request) throws LogisticsException {
        SleepUtil.sleepIfConfigured(stepDelay);

        LocalDateTime scheduledDate = LocalDateTime.now().plusDays(3);
        logisticsService.scheduleDelivery(
                request.getReservedTerminalId(),
                request.getCustomerId(),
                request.getAddress(),
                scheduledDate);

        request.setStatus(TerminalRequestStatus.AGENDADO);
        terminalRequestRepository.save(request);
    }
}

