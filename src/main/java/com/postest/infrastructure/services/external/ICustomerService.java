package com.postest.infrastructure.services.external;

import com.postest.application.dtos.CustomerDto;

public interface ICustomerService {
    CustomerDto validateCustomer(String customerId);
}

