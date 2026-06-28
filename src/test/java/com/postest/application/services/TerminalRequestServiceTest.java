package com.postest.application.services;

import com.postest.application.dtos.AddressDto;
import com.postest.application.dtos.CreateTerminalRequestDto;
import com.postest.application.dtos.CustomerDto;
import com.postest.application.dtos.DeliveryScheduleDto;
import com.postest.application.dtos.TerminalRequestDto;
import com.postest.domain.entities.Terminal;
import com.postest.domain.entities.TerminalRequest;
import com.postest.domain.enums.TerminalRequestStatus;
import com.postest.domain.enums.TerminalType;
import com.postest.infrastructure.repositories.TerminalRepository;
import com.postest.infrastructure.repositories.TerminalRequestRepository;
import com.postest.infrastructure.services.external.ICustomerService;
import com.postest.infrastructure.services.external.ITerminalReservationService;
import com.postest.infrastructure.services.external.ILogisticsService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TerminalRequestServiceTest {

    private TerminalRequestService terminalRequestService;

    @Mock
    private TerminalRequestRepository terminalRequestRepository;

    @Mock
    private TerminalRepository terminalRepository;

    @Mock
    private ICustomerService customerService;

    @Mock
    private ITerminalReservationService terminalReservationService;

    @Mock
    private ILogisticsService logisticsService;

    @BeforeEach
    void setUp() {
        terminalRequestService = new TerminalRequestService(
                terminalRequestRepository,
                terminalRepository,
                customerService,
                terminalReservationService,
                logisticsService
        );
    }

    // Teste 1: Cliente válido, terminal disponível e logística disponível deve resultar em AGENDADO
    @Test
    void testCreateTerminalRequest_ValidCustomerWithAvailableTerminalAndLogistics_ShouldReturn_AGENDADO() {
        // Arrange
        CreateTerminalRequestDto dto = new CreateTerminalRequestDto();
        dto.setCustomerId("CUST-123");
        dto.setTerminalType(TerminalType.POS_WIFI);
        dto.setAddress(new AddressDto("Rua Exemplo", "100", "São Paulo", "SP", "01000-000"));

        UUID terminalId = UUID.randomUUID();
        Terminal availableTerminal = Terminal.builder()
                .id(terminalId)
                .terminalType(TerminalType.POS_WIFI)
                .isAvailable(true)
                .build();

        UUID requestId = UUID.randomUUID();
        TerminalRequest savedRequest = TerminalRequest.builder()
                .id(requestId)
                .customerId("CUST-123")
                .terminalType(TerminalType.POS_WIFI)
                .status(TerminalRequestStatus.AGENDADO)
                .build();

        CustomerDto customerDto = new CustomerDto();
        customerDto.setActive(true);

        DeliveryScheduleDto deliveryScheduleDto = new DeliveryScheduleDto();

        // Mock comportamentos
        when(terminalRequestRepository.save(any())).thenReturn(savedRequest);
        when(customerService.validateCustomer("CUST-123")).thenReturn(customerDto);
        when(terminalRepository.findByTerminalTypeAndIsAvailableTrue(TerminalType.POS_WIFI))
                .thenReturn(Optional.of(availableTerminal));
        when(logisticsService.scheduleDelivery(eq(terminalId), eq("CUST-123"), any(), any()))
                .thenReturn(deliveryScheduleDto);

        // Act
        TerminalRequestDto result = terminalRequestService.createTerminalRequest(dto);

        // Assert
        assertNotNull(result);
        assertEquals(TerminalRequestStatus.AGENDADO, result.getStatus());
        assertEquals("CUST-123", result.getCustomerId());
        verify(customerService).validateCustomer("CUST-123");
        verify(terminalRepository).findByTerminalTypeAndIsAvailableTrue(TerminalType.POS_WIFI);
        verify(logisticsService).scheduleDelivery(eq(terminalId), eq("CUST-123"), any(), any());
    }

    // Teste 2: Cliente inexistente ou inativo deve resultar em REJEITADO
    @Test
    void testCreateTerminalRequest_InvalidOrInactiveCustomer_ShouldReturn_REJEITADO() {
        // Arrange
        CreateTerminalRequestDto dto = new CreateTerminalRequestDto();
        dto.setCustomerId("CUST-INVALID");
        dto.setTerminalType(TerminalType.POS_WIFI);
        dto.setAddress(new AddressDto("Rua Exemplo", "100", "São Paulo", "SP", "01000-000"));

        UUID requestId = UUID.randomUUID();
        TerminalRequest savedRequest = TerminalRequest.builder()
                .id(requestId)
                .customerId("CUST-INVALID")
                .terminalType(TerminalType.POS_WIFI)
                .status(TerminalRequestStatus.REJEITADO)
                .build();

        CustomerDto customerDto = new CustomerDto();
        customerDto.setActive(false);

        // Mock comportamentos
        when(terminalRequestRepository.save(any())).thenReturn(savedRequest);
        when(customerService.validateCustomer("CUST-INVALID")).thenReturn(customerDto);

        // Act
        TerminalRequestDto result = terminalRequestService.createTerminalRequest(dto);

        // Assert
        assertNotNull(result);
        assertEquals(TerminalRequestStatus.REJEITADO, result.getStatus());
        verify(customerService).validateCustomer("CUST-INVALID");
        verify(terminalRepository, never()).findByTerminalTypeAndIsAvailableTrue(any());
        verify(logisticsService, never()).scheduleDelivery(any(), any(), any(), any());
    }

    // Teste 3: Cliente válido sem terminal disponível deve resultar em ERRO_RESERVA
    @Test
    void testCreateTerminalRequest_ValidCustomerWithoutAvailableTerminal_ShouldReturn_ERRO_RESERVA() {
        // Arrange
        CreateTerminalRequestDto dto = new CreateTerminalRequestDto();
        dto.setCustomerId("CUST-123");
        dto.setTerminalType(TerminalType.POS_4G);
        dto.setAddress(new AddressDto("Rua Exemplo", "100", "São Paulo", "SP", "01000-000"));

        UUID requestId = UUID.randomUUID();
        TerminalRequest savedRequest = TerminalRequest.builder()
                .id(requestId)
                .customerId("CUST-123")
                .terminalType(TerminalType.POS_4G)
                .status(TerminalRequestStatus.ERRO_RESERVA)
                .build();

        CustomerDto customerDto = new CustomerDto();
        customerDto.setActive(true);

        // Mock comportamentos
        when(terminalRequestRepository.save(any())).thenReturn(savedRequest);
        when(customerService.validateCustomer("CUST-123")).thenReturn(customerDto);
        when(terminalRepository.findByTerminalTypeAndIsAvailableTrue(TerminalType.POS_4G))
                .thenReturn(Optional.empty());

        // Act
        TerminalRequestDto result = terminalRequestService.createTerminalRequest(dto);

        // Assert
        assertNotNull(result);
        assertEquals(TerminalRequestStatus.ERRO_RESERVA, result.getStatus());
        verify(customerService).validateCustomer("CUST-123");
        verify(terminalRepository).findByTerminalTypeAndIsAvailableTrue(TerminalType.POS_4G);
        verify(logisticsService, never()).scheduleDelivery(any(), any(), any(), any());
    }

    // Teste 4: Cliente válido com terminal reservado, mas falha no agendamento, deve resultar em ERRO_AGENDAMENTO
    @Test
    void testCreateTerminalRequest_ValidCustomerButLogisticsFailure_ShouldReturn_ERRO_AGENDAMENTO() {
        // Arrange
        CreateTerminalRequestDto dto = new CreateTerminalRequestDto();
        dto.setCustomerId("CUST-123");
        dto.setTerminalType(TerminalType.POS_WIFI);
        dto.setAddress(new AddressDto("Rua Exemplo", "100", "São Paulo", "SP", "01000-000"));

        UUID terminalId = UUID.randomUUID();
        Terminal availableTerminal = Terminal.builder()
                .id(terminalId)
                .terminalType(TerminalType.POS_WIFI)
                .isAvailable(true)
                .build();

        UUID requestId = UUID.randomUUID();
        TerminalRequest savedRequest = TerminalRequest.builder()
                .id(requestId)
                .customerId("CUST-123")
                .terminalType(TerminalType.POS_WIFI)
                .status(TerminalRequestStatus.ERRO_AGENDAMENTO)
                .build();

        CustomerDto customerDto = new CustomerDto();
        customerDto.setActive(true);

        // Mock comportamentos
        when(terminalRequestRepository.save(any())).thenReturn(savedRequest);
        when(customerService.validateCustomer("CUST-123")).thenReturn(customerDto);
        when(terminalRepository.findByTerminalTypeAndIsAvailableTrue(TerminalType.POS_WIFI))
                .thenReturn(Optional.of(availableTerminal));
        when(logisticsService.scheduleDelivery(eq(terminalId), eq("CUST-123"), any(), any()))
                .thenThrow(new RuntimeException("Logistics failure"));

        // Act
        TerminalRequestDto result = terminalRequestService.createTerminalRequest(dto);

        // Assert
        assertNotNull(result);
        assertEquals(TerminalRequestStatus.ERRO_AGENDAMENTO, result.getStatus());
        verify(customerService).validateCustomer("CUST-123");
        verify(terminalRepository).findByTerminalTypeAndIsAvailableTrue(TerminalType.POS_WIFI);
        verify(logisticsService).scheduleDelivery(eq(terminalId), eq("CUST-123"), any(), any());
    }

    // Teste 5: Consulta de solicitação existente deve retornar os dados corretamente
    @Test
    void testGetTerminalRequest_ExistingRequest_ShouldReturnCorrectData() {
        // Arrange
        UUID requestId = UUID.randomUUID();
        TerminalRequest existingRequest = TerminalRequest.builder()
                .id(requestId)
                .customerId("CUST-123")
                .terminalType(TerminalType.POS_WIFI)
                .status(TerminalRequestStatus.AGENDADO)
                .build();

        // Mock comportamento
        when(terminalRequestRepository.findById(requestId)).thenReturn(Optional.of(existingRequest));

        // Act
        TerminalRequestDto result = terminalRequestService.getTerminalRequest(requestId);

        // Assert
        assertNotNull(result);
        assertEquals(requestId, result.getId());
        assertEquals("CUST-123", result.getCustomerId());
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
        assertThrows(RuntimeException.class, () -> terminalRequestService.getTerminalRequest(nonExistentId));

        verify(terminalRequestRepository).findById(nonExistentId);
    }
}

