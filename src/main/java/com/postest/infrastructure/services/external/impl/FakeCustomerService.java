package com.postest.infrastructure.services.external.impl;

import com.postest.application.dtos.CustomerDto;
import com.postest.application.exceptions.CustomerNotFoundException;
import com.postest.infrastructure.services.external.ICustomerService;
import org.springframework.stereotype.Service;

@Service
public class FakeCustomerService implements ICustomerService {

    @Override
    public CustomerDto validateCustomer(String customerId) {
        // Clientes válidos e ativos
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
                    .name("Customer Two")
                    .active(true)
                    .email("customer2@example.com")
                    .build();
        }

        // Cliente inativo
        if ("CUST-INACTIVE".equals(customerId)) {
            return CustomerDto.builder()
                    .id(customerId)
                    .name("Inactive Customer")
                    .active(false)
                    .email("inactive@example.com")
                    .build();
        }

        // Cliente não encontrado
        throw new CustomerNotFoundException(customerId);
    }
}

