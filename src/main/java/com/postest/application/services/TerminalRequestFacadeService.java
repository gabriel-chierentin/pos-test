package com.postest.application.services;

import com.postest.application.dtos.CreateTerminalRequestDto;
import com.postest.application.dtos.TerminalRequestDto;
import com.postest.application.exceptions.CustomerNotFoundException;
import com.postest.application.exceptions.LogisticsException;
import com.postest.application.exceptions.TerminalRequestNotFoundException;
import com.postest.application.exceptions.TerminalUnavailableException;
import com.postest.application.mappers.TerminalRequestMapper;
import com.postest.application.ports.input.CreateTerminalRequestUseCase;
import com.postest.application.ports.input.GetTerminalRequestUseCase;
import com.postest.application.ports.output.TerminalRequestRepositoryPort;
import com.postest.domain.entities.TerminalRequest;
import com.postest.domain.enums.TerminalRequestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class TerminalRequestFacadeService implements CreateTerminalRequestUseCase, GetTerminalRequestUseCase {

    private static final Logger log = LoggerFactory.getLogger(TerminalRequestFacadeService.class);

    private final TerminalRequestRepositoryPort terminalRequestRepository;
    private final TerminalRequestMapper terminalRequestMapper;
    private final CustomerValidationService customerValidationService;
    private final TerminalReservationService terminalReservationService;
    private final DeliverySchedulingService deliverySchedulingService;

    public TerminalRequestFacadeService(
            TerminalRequestRepositoryPort terminalRequestRepository,
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

    @Override
    public TerminalRequestDto createTerminalRequest(CreateTerminalRequestDto dto) {
        TerminalRequest request = terminalRequestMapper.fromCreateDto(dto);
        TerminalRequest saved = terminalRequestRepository.save(request);

        log.info("[Request={}] Status: {} | Solicitação criada para customerId={}, terminalType={}",
                saved.getId(), saved.getStatus(), saved.getCustomerId(), saved.getTerminalType());

        try {
            customerValidationService.validate(saved);
            terminalReservationService.reserve(saved);
            deliverySchedulingService.schedule(saved);

            log.info("[Request={}] Status: {} | Fluxo concluído com sucesso",
                    saved.getId(), saved.getStatus());

        } catch (CustomerNotFoundException e) {
            saved.setStatus(TerminalRequestStatus.REJEITADO);
            terminalRequestRepository.save(saved);
            log.warn("[Request={}] Status: {} | Cliente não encontrado ou inativo: {}",
                    saved.getId(), saved.getStatus(), e.getMessage());
        } catch (TerminalUnavailableException e) {
            saved.setStatus(TerminalRequestStatus.ERRO_RESERVA);
            terminalRequestRepository.save(saved);
            log.warn("[Request={}] Status: {} | Erro na reserva do terminal: {}",
                    saved.getId(), saved.getStatus(), e.getMessage());
        } catch (LogisticsException e) {
            saved.setStatus(TerminalRequestStatus.ERRO_AGENDAMENTO);
            terminalRequestRepository.save(saved);
            log.warn("[Request={}] Status: {} | Erro no agendamento de entrega: {}",
                    saved.getId(), saved.getStatus(), e.getMessage());
        }

        return terminalRequestMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TerminalRequestDto getTerminalRequest(UUID requestId) {
        TerminalRequest request = terminalRequestRepository.findById(requestId)
                .orElseThrow(() -> new TerminalRequestNotFoundException(requestId.toString()));
        return terminalRequestMapper.toDto(request);
    }
}

