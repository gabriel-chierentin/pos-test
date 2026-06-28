package com.postest.application.services;

import com.postest.application.dtos.AddressDto;
import com.postest.application.dtos.CreateTerminalRequestDto;
import com.postest.application.dtos.TerminalRequestDto;
import com.postest.application.exceptions.CustomerNotFoundException;
import com.postest.application.exceptions.LogisticsException;
import com.postest.application.exceptions.TerminalRequestNotFoundException;
import com.postest.application.exceptions.TerminalUnavailableException;
import com.postest.domain.entities.Address;
import com.postest.domain.entities.TerminalRequest;
import com.postest.domain.enums.TerminalRequestStatus;
import com.postest.infrastructure.repositories.TerminalRepository;
import com.postest.infrastructure.repositories.TerminalRequestRepository;
import com.postest.infrastructure.services.external.ICustomerService;
import com.postest.infrastructure.services.external.ILogisticsService;
import com.postest.infrastructure.services.external.ITerminalReservationService;
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

    @Value("${terminal.request.step.delay:0}")
    @SuppressWarnings("all")
    private long stepDelay;

    public TerminalRequestService(
            TerminalRequestRepository terminalRequestRepository,
            TerminalRepository terminalRepository,
            ICustomerService customerService,
            ITerminalReservationService terminalReservationService,
            ILogisticsService logisticsService) {
        this.terminalRequestRepository = terminalRequestRepository;
        this.terminalRepository = terminalRepository;
        this.customerService = customerService;
        this.terminalReservationService = terminalReservationService;
        this.logisticsService = logisticsService;
    }

    public TerminalRequestDto createTerminalRequest(CreateTerminalRequestDto dto) {
        Address address = new Address(
                dto.getAddress().getStreet(),
                dto.getAddress().getNumber(),
                dto.getAddress().getCity(),
                dto.getAddress().getState(),
                dto.getAddress().getZipCode()
        );

        TerminalRequest request = TerminalRequest.builder()
                .customerId(dto.getCustomerId())
                .terminalType(dto.getTerminalType())
                .address(address)
                .status(TerminalRequestStatus.SOLICITADO)
                .build();

        TerminalRequest saved = terminalRequestRepository.save(request);

        // Step 1: Validate customer
        sleepIfConfigured();
        try {
            var customer = customerService.validateCustomer(saved.getCustomerId());

            if (!customer.getActive()) {
                saved.setStatus(TerminalRequestStatus.REJEITADO);
                terminalRequestRepository.save(saved);
                return mapToDto(saved);
            }

            saved.setStatus(TerminalRequestStatus.VALIDADO);
            terminalRequestRepository.save(saved);

            // Step 2: Reserve terminal
            sleepIfConfigured();
            try {
                var terminal = terminalRepository
                        .findByTerminalTypeAndIsAvailableTrue(saved.getTerminalType())
                        .orElseThrow(() -> new TerminalUnavailableException(
                                "No available terminal of type " + saved.getTerminalType()));

                terminalReservationService.reserveTerminal(terminal.getId(), saved.getCustomerId());
                saved.setReservedTerminalId(terminal.getId());
                saved.setStatus(TerminalRequestStatus.RESERVADO);
                terminalRequestRepository.save(saved);

                // Step 3: Schedule delivery
                sleepIfConfigured();
                try {
                    LocalDateTime scheduledDate = LocalDateTime.now().plusDays(3);
                    logisticsService.scheduleDelivery(
                            terminal.getId(),
                            saved.getCustomerId(),
                            saved.getAddress(),
                            scheduledDate);

                    saved.setStatus(TerminalRequestStatus.AGENDADO);
                    terminalRequestRepository.save(saved);

                } catch (LogisticsException e) {
                    saved.setStatus(TerminalRequestStatus.ERRO_AGENDAMENTO);
                    terminalRequestRepository.save(saved);
                }

            } catch (TerminalUnavailableException e) {
                saved.setStatus(TerminalRequestStatus.ERRO_RESERVA);
                terminalRequestRepository.save(saved);
            }

        } catch (CustomerNotFoundException e) {
            saved.setStatus(TerminalRequestStatus.REJEITADO);
            terminalRequestRepository.save(saved);
        }

        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public TerminalRequestDto getTerminalRequest(UUID requestId) {
        TerminalRequest request = terminalRequestRepository.findById(requestId)
                .orElseThrow(() -> new TerminalRequestNotFoundException(requestId.toString()));
        return mapToDto(request);
    }

    private void sleepIfConfigured() {
        if (stepDelay > 0) {
            try {
                Thread.sleep(stepDelay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }


    private TerminalRequestDto mapToDto(TerminalRequest entity) {
        return TerminalRequestDto.builder()
                .id(entity.getId())
                .customerId(entity.getCustomerId())
                .terminalType(entity.getTerminalType())
                .address(mapAddressToDto(entity.getAddress()))
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private AddressDto mapAddressToDto(Address address) {
        return new AddressDto(
                address.getStreet(),
                address.getNumber(),
                address.getCity(),
                address.getState(),
                address.getZipCode()
        );
    }
}

