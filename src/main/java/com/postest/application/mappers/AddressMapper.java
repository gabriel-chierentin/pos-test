package com.postest.application.mappers;

import com.postest.application.dtos.AddressDto;
import com.postest.domain.entities.Address;
import org.springframework.stereotype.Component;

@Component
public class AddressMapper {

    public AddressDto toDto(Address address) {
        if (address == null) {
            return null;
        }

        return new AddressDto(
                address.getStreet(),
                address.getNumber(),
                address.getCity(),
                address.getState(),
                address.getZipCode()
        );
    }

    public Address toDomain(AddressDto dto) {
        if (dto == null) {
            return null;
        }

        return new Address(
                dto.getStreet(),
                dto.getNumber(),
                dto.getCity(),
                dto.getState(),
                dto.getZipCode()
        );
    }
}

