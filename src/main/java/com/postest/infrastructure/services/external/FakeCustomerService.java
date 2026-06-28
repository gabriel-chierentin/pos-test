package com.postest.infrastructure.services.external;

import com.postest.application.dtos.CustomerDto;

public interface FakeCustomerService {
    CustomerDto validateCustomer(String customerId);
}

