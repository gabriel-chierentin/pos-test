package com.postest.application.ports.output;

import com.postest.application.dtos.CustomerDto;

public interface CustomerServicePort {
    CustomerDto validateCustomer(String customerId);
}

