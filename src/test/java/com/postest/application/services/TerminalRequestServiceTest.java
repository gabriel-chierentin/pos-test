package com.postest.application.services;

import com.postest.application.dtos.AddressDto;
import com.postest.application.dtos.CreateTerminalRequestDto;
import com.postest.application.dtos.TerminalRequestDto;
import com.postest.application.mappers.TerminalRequestMapper;
import com.postest.application.ports.output.TerminalRequestRepositoryPort;
import com.postest.domain.entities.TerminalRequest;
import com.postest.domain.enums.TerminalRequestStatus;
import com.postest.domain.enums.TerminalType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TerminalRequestServiceTest {

    private TerminalRequestFacadeService facadeService;

    @Mock
    private TerminalRequestRepositoryPort terminalRequestRepository;

    @Mock
    private TerminalRequestMapper terminalRequestMapper;

    @Mock
    private CustomerValidationService customerValidationService;

    @Mock
    private TerminalReservationService terminalReservationService;

    @Mock
    private DeliverySchedulingService deliverySchedulingService;

    @BeforeEach
    void setUp() {
        facadeService = new TerminalRequestFacadeService(
                terminalRequestRepository,
                terminalRequestMapper,
                customerValidationService,
                terminalReservationService,
                deliverySchedulingService
        );
    }

    // Teste 1: Cliente válido, terminal disponível e logística disponível deve resultar em AGENDADO
    @Test
    void testCreateTerminalRequest_ValidCustomerWithAvailableTerminalAndLogistics_ShouldReturn_AGENDADO() throws Exception {
        // Arrange
        CreateTerminalRequestDto dto = new CreateTerminalRequestDto();
        String customerId = "CUST-001";
        dto.setCustomerId(customerId);
        dto.setTerminalType(TerminalType.POS_WIFI);
        dto.setAddress(new AddressDto("Rua Exemplo", "100", "São Paulo", "SP", "01000-000"));

        UUID requestId = UUID.randomUUID();
        TerminalRequest savedRequest = TerminalRequest.builder()
                .id(requestId)
                .customerId(customerId)
                .terminalType(TerminalType.POS_WIFI)
                .status(TerminalRequestStatus.VALIDADO)
                .build();

        TerminalRequestDto expectedDto = new TerminalRequestDto();
        expectedDto.setId(requestId);
        expectedDto.setCustomerId(customerId);
        expectedDto.setStatus(TerminalRequestStatus.AGENDADO);

        // Mock comportamentos
        when(terminalRequestRepository.save(any())).thenReturn(savedRequest);
        when(terminalRequestMapper.fromCreateDto(dto)).thenReturn(savedRequest);
        when(terminalRequestMapper.toDto(any())).thenReturn(expectedDto);

        doNothing().when(customerValidationService).validate(any());
        doNothing().when(terminalReservationService).reserve(any());
        doNothing().when(deliverySchedulingService).schedule(any());

        // Act
        TerminalRequestDto result = facadeService.createTerminalRequest(dto);

        // Assert
        assertNotNull(result);
        assertEquals(TerminalRequestStatus.AGENDADO, result.getStatus());
        verify(customerValidationService).validate(any());
        verify(terminalReservationService).reserve(any());
        verify(deliverySchedulingService).schedule(any());
    }

    // Teste 2: Cliente inexistente ou inativo deve resultar em REJEITADO
    @Test
    void testCreateTerminalRequest_InvalidOrInactiveCustomer_ShouldReturn_REJEITADO() throws Exception {
        // Arrange
        CreateTerminalRequestDto dto = new CreateTerminalRequestDto();
        String customerId = "CUST-002";
        dto.setCustomerId(customerId);
        dto.setTerminalType(TerminalType.POS_WIFI);
        dto.setAddress(new AddressDto("Rua Exemplo", "100", "São Paulo", "SP", "01000-000"));

        UUID requestId = UUID.randomUUID();
        TerminalRequest savedRequest = TerminalRequest.builder()
                .id(requestId)
                .customerId(customerId)
                .terminalType(TerminalType.POS_WIFI)
                .status(TerminalRequestStatus.REJEITADO)
                .build();

        TerminalRequestDto expectedDto = new TerminalRequestDto();
        expectedDto.setStatus(TerminalRequestStatus.REJEITADO);

        // Mock comportamentos
        when(terminalRequestRepository.save(any())).thenReturn(savedRequest);
        when(terminalRequestMapper.fromCreateDto(dto)).thenReturn(savedRequest);
        when(terminalRequestMapper.toDto(any())).thenReturn(expectedDto);

        doThrow(new com.postest.application.exceptions.CustomerNotFoundException("Customer not found"))
                .when(customerValidationService).validate(any());

        // Act
        TerminalRequestDto result = facadeService.createTerminalRequest(dto);

        // Assert
        assertNotNull(result);
        assertEquals(TerminalRequestStatus.REJEITADO, result.getStatus());
        verify(customerValidationService).validate(any());
        verify(terminalReservationService, never()).reserve(any());
        verify(deliverySchedulingService, never()).schedule(any());
    }

    // Teste 3: Cliente válido sem terminal disponível deve resultar em ERRO_RESERVA
    @Test
    void testCreateTerminalRequest_ValidCustomerWithoutAvailableTerminal_ShouldReturn_ERRO_RESERVA() throws Exception {
        // Arrange
        CreateTerminalRequestDto dto = new CreateTerminalRequestDto();
        String customerId = "CUST-003";
        dto.setCustomerId(customerId);
        dto.setTerminalType(TerminalType.POS_4G);
        dto.setAddress(new AddressDto("Rua Exemplo", "100", "São Paulo", "SP", "01000-000"));

        UUID requestId = UUID.randomUUID();
        TerminalRequest savedRequest = TerminalRequest.builder()
                .id(requestId)
                .customerId(customerId)
                .terminalType(TerminalType.POS_4G)
                .status(TerminalRequestStatus.ERRO_RESERVA)
                .build();

        TerminalRequestDto expectedDto = new TerminalRequestDto();
        expectedDto.setStatus(TerminalRequestStatus.ERRO_RESERVA);

        // Mock comportamentos
        when(terminalRequestRepository.save(any())).thenReturn(savedRequest);
        when(terminalRequestMapper.fromCreateDto(dto)).thenReturn(savedRequest);
        when(terminalRequestMapper.toDto(any())).thenReturn(expectedDto);

        doNothing().when(customerValidationService).validate(any());
        doThrow(new com.postest.application.exceptions.TerminalUnavailableException("No terminals available"))
                .when(terminalReservationService).reserve(any());

        // Act
        TerminalRequestDto result = facadeService.createTerminalRequest(dto);

        // Assert
        assertNotNull(result);
        assertEquals(TerminalRequestStatus.ERRO_RESERVA, result.getStatus());
        verify(customerValidationService).validate(any());
        verify(terminalReservationService).reserve(any());
        verify(deliverySchedulingService, never()).schedule(any());
    }

    // Teste 4: Cliente válido com terminal reservado, mas falha no agendamento, deve resultar em ERRO_AGENDAMENTO
    @Test
    void testCreateTerminalRequest_ValidCustomerButLogisticsFailure_ShouldReturn_ERRO_AGENDAMENTO() throws Exception {
        // Arrange
        CreateTerminalRequestDto dto = new CreateTerminalRequestDto();
        String customerId = "CUST-004";
        dto.setCustomerId(customerId);
        dto.setTerminalType(TerminalType.POS_WIFI);
        dto.setAddress(new AddressDto("Rua Exemplo", "100", "São Paulo", "SP", "01000-000"));

        UUID requestId = UUID.randomUUID();
        TerminalRequest savedRequest = TerminalRequest.builder()
                .id(requestId)
                .customerId(customerId)
                .terminalType(TerminalType.POS_WIFI)
                .status(TerminalRequestStatus.ERRO_AGENDAMENTO)
                .build();

        TerminalRequestDto expectedDto = new TerminalRequestDto();
        expectedDto.setStatus(TerminalRequestStatus.ERRO_AGENDAMENTO);

        // Mock comportamentos
        when(terminalRequestRepository.save(any())).thenReturn(savedRequest);
        when(terminalRequestMapper.fromCreateDto(dto)).thenReturn(savedRequest);
        when(terminalRequestMapper.toDto(any())).thenReturn(expectedDto);

        doNothing().when(customerValidationService).validate(any());
        doNothing().when(terminalReservationService).reserve(any());
        doThrow(new com.postest.application.exceptions.LogisticsException("Logistics failure"))
                .when(deliverySchedulingService).schedule(any());

        // Act
        TerminalRequestDto result = facadeService.createTerminalRequest(dto);

        // Assert
        assertNotNull(result);
        assertEquals(TerminalRequestStatus.ERRO_AGENDAMENTO, result.getStatus());
        verify(customerValidationService).validate(any());
        verify(terminalReservationService).reserve(any());
        verify(deliverySchedulingService).schedule(any());
    }

    // Teste 5: Consulta de solicitação existente deve retornar os dados corretamente
    @Test
    void testGetTerminalRequest_ExistingRequest_ShouldReturnCorrectData() {
        // Arrange
        UUID requestId = UUID.randomUUID();
        String customerId = "CUST-005";

        TerminalRequest existingRequest = TerminalRequest.builder()
                .id(requestId)
                .customerId(customerId)
                .terminalType(TerminalType.POS_WIFI)
                .status(TerminalRequestStatus.AGENDADO)
                .build();

        TerminalRequestDto expectedDto = new TerminalRequestDto();
        expectedDto.setId(requestId);
        expectedDto.setCustomerId(customerId);
        expectedDto.setTerminalType(TerminalType.POS_WIFI);
        expectedDto.setStatus(TerminalRequestStatus.AGENDADO);

        // Mock comportamento
        when(terminalRequestRepository.findById(requestId)).thenReturn(Optional.of(existingRequest));
        when(terminalRequestMapper.toDto(existingRequest)).thenReturn(expectedDto);

        // Act
        TerminalRequestDto result = facadeService.getTerminalRequest(requestId);

        // Assert
        assertNotNull(result);
        assertEquals(requestId, result.getId());
        assertEquals(customerId, result.getCustomerId());
        assertEquals(TerminalType.POS_WIFI, result.getTerminalType());
        assertEquals(TerminalRequestStatus.AGENDADO, result.getStatus());
        verify(terminalRequestRepository).findById(requestId);
    }

    // Teste 6: Consulta de solicitação inexistente deve retornar erro adequado
    @Test
    void testGetTerminalRequest_NonExistentRequest_ShouldThrowException() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();

        // Mock comportamento
        when(terminalRequestRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(com.postest.application.exceptions.TerminalRequestNotFoundException.class,
                () -> facadeService.getTerminalRequest(nonExistentId));

        verify(terminalRequestRepository).findById(nonExistentId);
    }
}

