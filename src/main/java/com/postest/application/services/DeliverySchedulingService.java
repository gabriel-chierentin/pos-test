package com.postest.application.services;

import com.postest.application.exceptions.LogisticsException;
import com.postest.application.ports.output.LogisticsServicePort;
import com.postest.application.ports.output.TerminalRequestRepositoryPort;
import com.postest.domain.entities.TerminalRequest;
import com.postest.domain.enums.TerminalRequestStatus;
import com.postest.infrastructure.utils.SleepUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class DeliverySchedulingService {

    private static final Logger log = LoggerFactory.getLogger(DeliverySchedulingService.class);

    private final LogisticsServicePort logisticsService;
    private final TerminalRequestRepositoryPort terminalRequestRepository;

    @Value("${terminal.request.step.delay:0}")
    private long stepDelay;

    public DeliverySchedulingService(
            LogisticsServicePort logisticsService,
            TerminalRequestRepositoryPort terminalRequestRepository) {
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

        log.info("[Request={}] Status: RESERVADO → Iniciando agendamento de entrega para terminalId={}",
                request.getId(), request.getReservedTerminalId());

        LocalDateTime scheduledDate = LocalDateTime.now().plusDays(3);
        logisticsService.scheduleDelivery(
                request.getReservedTerminalId(),
                request.getCustomerId(),
                request.getAddress(),
                scheduledDate);

        request.setStatus(TerminalRequestStatus.AGENDADO);
        terminalRequestRepository.save(request);

        log.info("[Request={}] Status: RESERVADO → AGENDADO | Entrega agendada para: {}",
                request.getId(), scheduledDate);
    }
}

