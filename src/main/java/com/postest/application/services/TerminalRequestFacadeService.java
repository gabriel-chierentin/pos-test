package com.postest.application.services;

import com.postest.application.dtos.CreateTerminalRequestDto;
import com.postest.application.dtos.TerminalRequestDto;
import com.postest.application.exceptions.CustomerNotFoundException;
import com.postest.application.exceptions.LogisticsException;
import com.postest.application.exceptions.TerminalRequestNotFoundException;
import com.postest.application.exceptions.TerminalUnavailableException;
import com.postest.application.mappers.TerminalRequestMapper;
import com.postest.domain.entities.TerminalRequest;
import com.postest.domain.enums.TerminalRequestStatus;
import com.postest.infrastructure.repositories.TerminalRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class TerminalRequestFacadeService {

    private final TerminalRequestRepository terminalRequestRepository;
    private final TerminalRequestMapper terminalRequestMapper;
    private final CustomerValidationService customerValidationService;
    private final TerminalReservationService terminalReservationService;
    private final DeliverySchedulingService deliverySchedulingService;

    public TerminalRequestFacadeService(
            TerminalRequestRepository terminalRequestRepository,
            TerminalRequestMapper terminalRequestMapper,
            CustomerValidationService customerValidationService,
            TerminalReservationService terminalReservationService,
            DeliverySchedulingService deliverySchedulingService) {
        this.terminalRequestRepository = terminalRequestRepository;
        this.terminalRequestMapper = terminalRequestMapper;
        this.customerValidationService = customerValidationService;
        this.terminalReservationService = terminalReservationService;
        this.deliverySchedulingService = deliverySchedulingService;
    }

    public TerminalRequestDto createTerminalRequest(CreateTerminalRequestDto dto) {
        TerminalRequest request = terminalRequestMapper.fromCreateDto(dto);
        TerminalRequest saved = terminalRequestRepository.save(request);

        try {
            customerValidationService.validate(saved);
            terminalReservationService.reserve(saved);
            deliverySchedulingService.schedule(saved);

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

