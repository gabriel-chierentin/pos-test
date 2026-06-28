package com.postest.infrastructure.services.external.impl;

import com.postest.application.dtos.CustomerDto;
import com.postest.application.exceptions.CustomerNotFoundException;
import com.postest.infrastructure.services.external.FakeCustomerService;
import org.springframework.stereotype.Service;

@Service
public class FakeCustomerServiceImpl implements FakeCustomerService {

    @Override
    public CustomerDto validateCustomer(String customerId) {
        if ("CUST-123".equals(customerId)) {
            return CustomerDto.builder()
                    .id(customerId)
                    .name("Customer One")
                    .active(true)
                    .email("customer1@example.com")
                    .build();
        }

        if ("CUST-456".equals(customerId)) {
            return CustomerDto.builder()
                    .id(customerId)
                    .name("Customer Two (inactive)")
                    .active(false)
                    .email("customer2@example.com")
                    .build();
        }

        if ("CUST-789".equals(customerId)) {
            return CustomerDto.builder()
                    .id(customerId)
                    .name("Customer Three")
                    .active(true)
                    .email("customer3@example.com")
                    .build();
        }

        if ("CUST-321".equals(customerId)) {
            return CustomerDto.builder()
                    .id(customerId)
                    .name("Customer Four")
                    .active(true)
                    .email("customer4@example.com")
                    .build();
        }

        // Cliente não encontrado
        throw new CustomerNotFoundException(customerId);
    }
}

