package com.postest.application.services;

import com.postest.application.exceptions.TerminalUnavailableException;
import com.postest.domain.entities.Terminal;
import com.postest.domain.entities.TerminalRequest;
import com.postest.domain.enums.TerminalRequestStatus;
import com.postest.infrastructure.repositories.TerminalRepository;
import com.postest.infrastructure.repositories.TerminalRequestRepository;
import com.postest.infrastructure.services.external.FakeTerminalReservationService;
import com.postest.infrastructure.utils.SleepUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TerminalReservationService {

    private final TerminalRepository terminalRepository;
    private final TerminalRequestRepository terminalRequestRepository;
    private final FakeTerminalReservationService externalReservationService;

    @Value("${terminal.request.step.delay:0}")
    private long stepDelay;

    public TerminalReservationService(
            TerminalRepository terminalRepository,
            TerminalRequestRepository terminalRequestRepository,
            FakeTerminalReservationService externalReservationService) {
        this.terminalRepository = terminalRepository;
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

        Terminal terminal = terminalRepository
                .findByTerminalTypeAndIsAvailableTrue(request.getTerminalType())
                .orElseThrow(() -> new TerminalUnavailableException(
                        "No available terminal of type " + request.getTerminalType()));

        externalReservationService.reserveTerminal(terminal.getId(), request.getCustomerId());
        request.setReservedTerminalId(terminal.getId());
        request.setStatus(TerminalRequestStatus.RESERVADO);
        terminalRequestRepository.save(request);
    }
}

