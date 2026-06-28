package com.postest.application.services;

import com.postest.application.exceptions.TerminalUnavailableException;
import com.postest.domain.entities.TerminalRequest;
import com.postest.domain.enums.TerminalRequestStatus;
import com.postest.infrastructure.repositories.TerminalRequestRepository;
import com.postest.infrastructure.services.external.FakeTerminalReservationService;
import com.postest.infrastructure.utils.SleepUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TerminalReservationService {

    private static final Logger log = LoggerFactory.getLogger(TerminalReservationService.class);

    private final TerminalRequestRepository terminalRequestRepository;
    private final FakeTerminalReservationService externalReservationService;

    @Value("${terminal.request.step.delay:0}")
    private long stepDelay;

    public TerminalReservationService(
            TerminalRequestRepository terminalRequestRepository,
            FakeTerminalReservationService externalReservationService) {
        this.terminalRequestRepository = terminalRequestRepository;
        this.externalReservationService = externalReservationService;
    }

    /**
     * Reserva um terminal disponível do tipo solicitado.
     * Atualiza o status do request para RESERVADO.
     *
     * @param request o terminal request com o tipo de terminal desejado
     * @throws TerminalUnavailableException se nenhum terminal disponível for encontrado
     */
    public void reserve(TerminalRequest request) throws TerminalUnavailableException {
        SleepUtil.sleepIfConfigured(stepDelay);

        log.info("[Request={}] Status: VALIDADO → Iniciando reserva de terminal tipo={}",
                request.getId(), request.getTerminalType());

        UUID terminalId = externalReservationService.reserveTerminal(request.getTerminalType(), request.getCustomerId());

        request.setReservedTerminalId(terminalId);
        request.setStatus(TerminalRequestStatus.RESERVADO);
        terminalRequestRepository.save(request);

        log.info("[Request={}] Status: VALIDADO → RESERVADO | Terminal reservado: terminalId={}",
                request.getId(), terminalId);
    }
}

