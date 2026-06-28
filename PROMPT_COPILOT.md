# Script para GitHub Copilot - Desafio Reserva de Terminal POS

## CONTEXTO DO PROJETO
- Linguagem: Java 21
- Framework: Spring Boot 4.1.0
- Banco de Dados: PostgreSQL (local)
- Arquitetura: Controller → Service → Repository
- Padrão de Testes: JUnit 5 + Mockito

---

## PARTE 1: Entidades e Domain Model

Crie as seguintes entidades JPA:

1. **TerminalRequest** - Solicitação de terminal
   - Campos: id (UUID), customerId (String), terminalType (enum), address (embedded), status (enum), createdAt, updatedAt
   - Status possíveis: SOLICITADO, VALIDADO, RESERVADO, AGENDADO, REJEITADO, ERRO_RESERVA, ERRO_AGENDAMENTO

2. **Address** - Endereço (Embeddable)
   - Campos: street, number, city, state, zipCode

3. **Terminal** - Terminal disponível
   - Campos: id (UUID), terminalType (enum), isAvailable (boolean), reservedBy (String - customerId)

Implemente com Lombok para reduzir boilerplate.

---

## PARTE 2: Enums

Crie os seguintes enums:

1. **TerminalRequestStatus** com todos os status mencionados
2. **TerminalType** (POS_WIFI, POS_4G, POS_MANUAL, etc)

---

## PARTE 3: Repositories

Crie interfaces extends JpaRepository para:
1. TerminalRequestRepository
2. TerminalRepository com método findByTerminalTypeAndIsAvailableTrue()

---

## PARTE 4: Serviços Externos (Interfaces e Implementações Fake)

Crie interfaces para simular integrações externas:

1. **CustomerService** - Interface
   - Método: validateCustomer(customerId: String): CustomerDto
   - CustomerDto: {id, name, active, email}
   
   Implementação Fake/Mock:
   - Retorne clientes válidos para IDs como "CUST-123", "CUST-456"
   - Retorne inativo ou não encontrado para "CUST-INVALID", "CUST-INACTIVE"

2. **TerminalReservationService** - Interface
   - Método: reserveTerminal(terminalId: UUID, customerId: String): void
   - Pode lançar TerminalAlreadyReservedException
   
   Implementação Fake/Mock:
   - Simule indisponibilidade aleatória OU use uma estratégia com contador

3. **LogisticsService** - Interface
   - Método: scheduleDelivery(terminalId: UUID, address: Address, scheduledDate: LocalDateTime): DeliverySchedule
   - DeliverySchedule: {id, terminalId, customerId, scheduledDate, address}
   
   Implementação Fake/Mock:
   - Falhe para dates no passado
   - Falhe de forma controlada (ex: a cada 3 chamadas)

---

## PARTE 5: Service Principal (TerminalRequestService)

Implemente a lógica do fluxo de negócio:

1. **createTerminalRequest(dto: CreateTerminalRequestDto)**: 
   - Cria request com status SOLICITADO
   - Persiste no banco
   - Retorna DTO com ID

2. **processTerminalRequest(requestId: UUID)**:
   - Busca a solicitação
   - Chama CustomerService.validateCustomer()
   - Se inválido → status = REJEITADO
   - Se válido → status = VALIDADO, tenta reservar terminal
   - Se não houver terminal → status = ERRO_RESERVA
   - Se reservar → status = RESERVADO, tenta agendar
   - Se agendar falhar → status = ERRO_AGENDAMENTO
   - Se agendar com sucesso → status = AGENDADO
   - Persiste mudanças em cada etapa

3. **getTerminalRequest(requestId: UUID)**:
   - Busca e retorna TerminalRequestDto
   - Lança ResourceNotFoundException se não existir

4. **Trate exceções apropriadamente**:
   - CustomerNotFoundException → REJEITADO
   - TerminalUnavailableException → ERRO_RESERVA
   - LogisticsException → ERRO_AGENDAMENTO

---

## PARTE 6: Controllers

1. **TerminalRequestController**:
   - POST /terminal-requests → createTerminalRequest()
   - POST /terminal-requests/{id}/process → processTerminalRequest()
   - GET /terminal-requests/{id} → getTerminalRequest()

2. Implemente validação com @Valid nas DTOs
3. Retorne ResponseEntity com status HTTP apropriados
4. Trate GlobalExceptionHandler para erros

---

## PARTE 7: DTOs

Crie DTOs para requisição e resposta:

1. **CreateTerminalRequestDto**:
   - customerId, terminalType, address

2. **AddressDto**:
   - street, number, city, state, zipCode

3. **TerminalRequestDto** (resposta):
   - id, customerId, terminalType, address, status, createdAt, updatedAt

4. **ErrorResponseDto**:
   - message, timestamp, statusCode

---

## PARTE 8: Configuração do Banco (application.properties)

```
spring.datasource.url=jdbc:postgresql://localhost:5432/postest
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

---

## PARTE 9: Testes Unitários

Implemente testes para TerminalRequestService:

1. **Test: Cliente válido, terminal disponível, agendamento sucesso → AGENDADO**
   - Mock: CustomerService retorna cliente ativo
   - Mock: TerminalRepository retorna terminal disponível
   - Mock: LogisticsService agenda com sucesso
   - Assert: status = AGENDADO

2. **Test: Cliente inexistente → REJEITADO**
   - Mock: CustomerService lança CustomerNotFoundException
   - Assert: status = REJEITADO

3. **Test: Cliente válido, sem terminal disponível → ERRO_RESERVA**
   - Mock: CustomerService retorna cliente ativo
   - Mock: TerminalRepository.findByTerminalTypeAndIsAvailableTrue() retorna vazio
   - Assert: status = ERRO_RESERVA

4. **Test: Cliente válido, terminal reservado, falha agendamento → ERRO_AGENDAMENTO**
   - Mock: CustomerService retorna cliente ativo
   - Mock: TerminalRepository retorna terminal e reserva sucede
   - Mock: LogisticsService lança LogisticsException
   - Assert: status = ERRO_AGENDAMENTO

5. **Test: Consulta solicitação existente**
   - Crie uma solicitação real no banco
   - Busque via getTerminalRequest()
   - Assert: dados retornados corretamente

6. **Test: Consulta solicitação inexistente**
   - Busque ID inexistente
   - Assert: lança ResourceNotFoundException

---

## PARTE 10: Testes de Integração

Implemente testes com @SpringBootTest:

1. **Test: Fluxo completo até AGENDADO**
   - POST /terminal-requests
   - POST /terminal-requests/{id}/process
   - GET /terminal-requests/{id}
   - Assert: status chain correto (SOLICITADO → VALIDADO → RESERVADO → AGENDADO)

2. **Test: Diferentes cenários de falha**

---

## PARTE 11: Dados Iniciais (Data.sql ou Liquibase)

Insira terminais de teste no banco:

```sql
INSERT INTO terminals (id, terminal_type, is_available) VALUES
('550e8400-e29b-41d4-a716-446655440000', 'POS_WIFI', true),
('550e8400-e29b-41d4-a716-446655440001', 'POS_4G', true),
('550e8400-e29b-41d4-a716-446655440002', 'POS_WIFI', false);
```

---

## INSTRUÇÕES PARA USAR ESTE SCRIPT

1. **Copie cada PARTE sequencialmente no GitHub Copilot**
2. **Para cada PARTE, use um prompt como**:
   ```
   [COPIE O CONTEÚDO DA PARTE X AQUI]
   
   Implemente exatamente como descrito acima, 
   seguindo a arquitetura Controller → Service → Repository,
   com padrão Java/Spring Boot profissional.
   ```

3. **Após gerar cada arquivo, ajuste imports e correções se necessário**

4. **Ordem recomendada de implementação**:
   - Parte 2 (Enums)
   - Parte 1 (Entidades)
   - Parte 3 (Repositories)
   - Parte 4 (Serviços Externos)
   - Parte 5 (Service Principal)
   - Parte 6 (Controllers)
   - Parte 7 (DTOs)
   - Parte 11 (Dados)
   - Parte 9 (Testes)
   - Parte 10 (Testes Integração)

---

## CONSIDERAÇÕES ADICIONAIS

### Sobre Mocking (sua pergunta):
Use **@MockBean** do Spring para testes:
```java
@MockBean
private CustomerService customerService;

@Before
void setup() {
    when(customerService.validateCustomer("CUST-123"))
        .thenReturn(new CustomerDto(...));
}
```

### Sobre PostgreSQL:
Certifique-se que está rodando:
```powershell
docker run --name postgres-pos -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=postest -p 5432:5432 -d postgres
```

### Pontos a Melhorar (para mencionar na explicação):
- Implementar cache para validação de clientes
- Adicionar retry logic para chamadas a serviços externos
- Implementar Circuit Breaker pattern
- Adicionar logging estruturado com SLF4J
- Implementar auditoria de mudanças de status
- Adicionar paginação nas buscas
- Implementar soft delete para solicitações


