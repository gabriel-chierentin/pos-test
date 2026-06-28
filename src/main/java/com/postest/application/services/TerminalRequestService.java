package com.postest.application.services;

import com.postest.application.dtos.CreateTerminalRequestDto;
import com.postest.application.dtos.CustomerDto;
import com.postest.application.dtos.TerminalRequestDto;
import com.postest.application.exceptions.CustomerNotFoundException;
import com.postest.application.exceptions.LogisticsException;
import com.postest.application.exceptions.TerminalRequestNotFoundException;
import com.postest.application.exceptions.TerminalUnavailableException;
import com.postest.application.mappers.TerminalRequestMapper;
import com.postest.domain.entities.Terminal;
import com.postest.domain.entities.TerminalRequest;
import com.postest.domain.enums.TerminalRequestStatus;
import com.postest.infrastructure.repositories.TerminalRepository;
import com.postest.infrastructure.repositories.TerminalRequestRepository;
import com.postest.infrastructure.services.external.ICustomerService;
import com.postest.infrastructure.services.external.ILogisticsService;
import com.postest.infrastructure.services.external.ITerminalReservationService;
import com.postest.infrastructure.utils.SleepUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class TerminalRequestService {

    private final TerminalRequestRepository terminalRequestRepository;
    private final TerminalRepository terminalRepository;
    private final ICustomerService customerService;
    private final ITerminalReservationService terminalReservationService;
    private final ILogisticsService logisticsService;
    private final TerminalRequestMapper terminalRequestMapper;

    @Value("${terminal.request.step.delay:0}")
    @SuppressWarnings("all")
    private long stepDelay;

    public TerminalRequestService(
            TerminalRequestRepository terminalRequestRepository,
            TerminalRepository terminalRepository,
            ICustomerService customerService,
            ITerminalReservationService terminalReservationService,
            ILogisticsService logisticsService,
            TerminalRequestMapper terminalRequestMapper) {
        this.terminalRequestRepository = terminalRequestRepository;
        this.terminalRepository = terminalRepository;
        this.customerService = customerService;
        this.terminalReservationService = terminalReservationService;
        this.logisticsService = logisticsService;
        this.terminalRequestMapper = terminalRequestMapper;
    }

    public TerminalRequestDto createTerminalRequest(CreateTerminalRequestDto dto) {
        TerminalRequest request = terminalRequestMapper.fromCreateDto(dto);
        TerminalRequest saved = terminalRequestRepository.save(request);

        try {
            // Step 1: Validate customer
            SleepUtil.sleepIfConfigured(stepDelay);
            CustomerDto customer = customerService.validateCustomer(saved.getCustomerId());

            if (!customer.getActive()) {
                saved.setStatus(TerminalRequestStatus.REJEITADO);
                terminalRequestRepository.save(saved);
                return terminalRequestMapper.toDto(saved);
            }

            saved.setStatus(TerminalRequestStatus.VALIDADO);
            terminalRequestRepository.save(saved);

            // Step 2: Reserve terminal
            SleepUtil.sleepIfConfigured(stepDelay);
            Terminal terminal = terminalRepository
                    .findByTerminalTypeAndIsAvailableTrue(saved.getTerminalType())
                    .orElseThrow(() -> new TerminalUnavailableException(
                            "No available terminal of type " + saved.getTerminalType()));

            terminalReservationService.reserveTerminal(terminal.getId(), saved.getCustomerId());
            saved.setReservedTerminalId(terminal.getId());
            saved.setStatus(TerminalRequestStatus.RESERVADO);
            terminalRequestRepository.save(saved);

            // Step 3: Schedule delivery
            SleepUtil.sleepIfConfigured(stepDelay);
            LocalDateTime scheduledDate = LocalDateTime.now().plusDays(3);
            logisticsService.scheduleDelivery(terminal.getId(), saved.getCustomerId(), saved.getAddress(), scheduledDate);

            saved.setStatus(TerminalRequestStatus.AGENDADO);
            terminalRequestRepository.save(saved);

        } catch (CustomerNotFoundException e) {
            saved.setStatus(TerminalRequestStatus.REJEITADO);
            terminalRequestRepository.save(saved);
        } catch (TerminalUnavailableException e) {
            saved.setStatus(TerminalRequestStatus.ERRO_RESERVA);
            terminalRequestRepository.save(saved);
        } catch (LogisticsException e) {
            saved.setStatus(TerminalRequestStatus.ERRO_AGENDAMENTO);
            terminalRequestRepository.save(saved);
        }

        return terminalRequestMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public TerminalRequestDto getTerminalRequest(UUID requestId) {
        TerminalRequest request = terminalRequestRepository.findById(requestId)
                .orElseThrow(() -> new TerminalRequestNotFoundException(requestId.toString()));
        return terminalRequestMapper.toDto(request);
    }
}

