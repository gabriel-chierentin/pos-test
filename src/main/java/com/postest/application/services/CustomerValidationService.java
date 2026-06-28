package com.postest.application.services;

import com.postest.application.dtos.CustomerDto;
import com.postest.application.exceptions.CustomerNotFoundException;
import com.postest.domain.entities.TerminalRequest;
import com.postest.domain.enums.TerminalRequestStatus;
import com.postest.infrastructure.repositories.TerminalRequestRepository;
import com.postest.infrastructure.services.external.FakeCustomerService;
import com.postest.infrastructure.utils.SleepUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CustomerValidationService {

    private final FakeCustomerService customerService;
    private final TerminalRequestRepository terminalRequestRepository;

    @Value("${terminal.request.step.delay:0}")
    private long stepDelay;

    public CustomerValidationService(
            FakeCustomerService customerService,
            TerminalRequestRepository terminalRequestRepository) {
        this.customerService = customerService;
        this.terminalRequestRepository = terminalRequestRepository;
    }

    /**
     * Valida se o cliente existe e está ativo.
     * Atualiza o status do request para VALIDADO ou REJEITADO.
     *
     * @param request o terminal request a ser validado
     * @throws CustomerNotFoundException se o cliente não for encontrado
     */
    public void validate(TerminalRequest request) throws CustomerNotFoundException {
        SleepUtil.sleepIfConfigured(stepDelay);

        CustomerDto customer = customerService.validateCustomer(request.getCustomerId());

        if (!customer.getActive()) {
            request.setStatus(TerminalRequestStatus.REJEITADO);
            terminalRequestRepository.save(request);
            throw new CustomerNotFoundException(request.getCustomerId());
        }

        request.setStatus(TerminalRequestStatus.VALIDADO);
        terminalRequestRepository.save(request);
    }
}

