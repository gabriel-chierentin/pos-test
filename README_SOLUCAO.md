# Reserva de Terminal POS - Solução Técnica

## Visão Geral

Esta é uma aplicação Spring Boot que implementa um sistema de reserva de terminais POS com fluxo de negócio completo, validação de clientes, reserva de terminais e agendamento de entrega.

## Arquitetura

A aplicação segue o padrão **Controller → Service → Repository**:

- **Controllers** (`presentation.controllers`): Endpoints REST
- **Services** (`application.services`): Lógica de negócio
- **Repositories** (`infrastructure.repositories`): Acesso a dados
- **Domain** (`domain`): Entidades e enums
- **External Services** (`infrastructure.services.external`): Integrações simuladas

## Pré-requisitos

- Java 21+
- Maven 3.8+
- PostgreSQL 12+ (ou Docker)

## Instalação e Configuração

### 1. Clonar/Preparar o Projeto

```bash
cd pos_test
```

### 2. Criar Banco de Dados PostgreSQL

#### Opção A: Usar Docker (Recomendado)

```powershell
docker run --name postgres-pos `
  -e POSTGRES_PASSWORD=postgres `
  -e POSTGRES_DB=postest `
  -p 5432:5432 `
  -d postgres:latest
```

#### Opção B: PostgreSQL Instalado Localmente

Criar banco de dados:

```sql
CREATE DATABASE postest;
```

Certifique-se que as credenciais correspondem ao arquivo `application.properties`:
- URL: `jdbc:postgresql://localhost:5432/postest`
- Username: `postgres`
- Password: `postgres`

### 3. Compilar o Projeto

```bash
mvn clean install
```

### 4. Executar a Aplicação

```bash
mvn spring-boot:run
```

A aplicação estará disponível em: `http://localhost:8080/api`

Os terminais de teste serão inseridos automaticamente no banco através do arquivo `data.sql`.

## Endpoints da API

### 1. Criar Solicitação de Terminal

**POST** `/api/terminal-requests`

Body:
```json
{
  "customerId": "CUST-123",
  "terminalType": "POS_WIFI",
  "address": {
    "street": "Rua Exemplo",
    "number": "100",
    "city": "São Paulo",
    "state": "SP",
    "zipCode": "01000-000"
  }
}
```

Response (201 Created):
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "customerId": "CUST-123",
  "terminalType": "POS_WIFI",
  "address": {
    "street": "Rua Exemplo",
    "number": "100",
    "city": "São Paulo",
    "state": "SP",
    "zipCode": "01000-000"
  },
  "status": "SOLICITADO",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

### 2. Processar Solicitação

**POST** `/api/terminal-requests/{id}/process`

Response (200 OK):
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "customerId": "CUST-123",
  "terminalType": "POS_WIFI",
  "address": {...},
  "status": "AGENDADO",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:35:00"
}
```

### 3. Consultar Solicitação

**GET** `/api/terminal-requests/{id}`

Response (200 OK):
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "customerId": "CUST-123",
  "terminalType": "POS_WIFI",
  "address": {...},
  "status": "AGENDADO",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:35:00"
}
```

## Clientes Simulados (Mock)

A aplicação inclui clientes pré-configurados para testes:

| Customer ID   | Status    | Resultado Esperado |
|---------------|-----------|-------------------|
| CUST-123      | Ativo     | AGENDADO          |
| CUST-456      | Ativo     | AGENDADO          |
| CUST-INACTIVE | Inativo   | REJEITADO         |
| CUST-INVALID  | Não existe| REJEITADO         |

## Fluxo de Negócio

```
1. SOLICITADO (criação)
   ↓
2. Validar Cliente
   ├─ Inválido/Inativo → REJEITADO
   └─ Válido → VALIDADO
   ↓
3. Reservar Terminal
   ├─ Não disponível → ERRO_RESERVA
   └─ Disponível → RESERVADO
   ↓
4. Agendar Entrega
   ├─ Falha → ERRO_AGENDAMENTO
   └─ Sucesso → AGENDADO
```

## Testes

### Executar Testes Unitários

```bash
mvn test
```

### Executar Teste Específico

```bash
mvn test -Dtest=TerminalRequestServiceTest
```

### Executar Testes de Integração

```bash
mvn test -Dtest=TerminalRequestControllerIntegrationTest
```

### Testes Implementados

✅ **Cliente válido, terminal disponível, agendamento sucesso → AGENDADO**
- Valida o fluxo completo de sucesso

✅ **Cliente inexistente → REJEITADO**
- Simula cliente não encontrado

✅ **Cliente inativo → REJEITADO**
- Valida cliente existe mas está inativo

✅ **Cliente válido, sem terminal disponível → ERRO_RESERVA**
- Simula falta de terminais do tipo solicitado

✅ **Cliente válido, terminal reservado, falha agendamento → ERRO_AGENDAMENTO**
- Testa falha no serviço de logística

✅ **Consulta solicitação existente → Retorna dados**
- Valida recuperação de dados

✅ **Consulta solicitação inexistente → NOT FOUND**
- Testa tratamento de erro 404

✅ **Fluxo completo via API REST**
- Testa endpoints do controller

## Estrutura de Pastas

```
src/
├── main/
│   ├── java/com/postest/
│   │   ├── domain/
│   │   │   ├── entities/          (TerminalRequest, Terminal, Address)
│   │   │   └── enums/             (TerminalRequestStatus, TerminalType)
│   │   ├── application/
│   │   │   ├── services/          (TerminalRequestService)
│   │   │   ├── dtos/              (CreateTerminalRequestDto, etc)
│   │   │   └── exceptions/        (Custom exceptions)
│   │   ├── infrastructure/
│   │   │   ├── repositories/      (TerminalRequestRepository, TerminalRepository)
│   │   │   └── services/external/ (FakeCustomerService, etc)
│   │   ├── presentation/
│   │   │   ├── controllers/       (TerminalRequestController)
│   │   │   └── handlers/          (GlobalExceptionHandler)
│   │   └── PostestApplication.java
│   └── resources/
│       ├── application.properties
│       └── data.sql               (Dados iniciais)
└── test/
    ├── java/com/postest/
    │   ├── application/services/  (TerminalRequestServiceTest)
    │   └── TerminalRequestControllerIntegrationTest.java
    └── resources/
        └── application-test.properties
```

## Decisões Técnicas

### 1. Padrão MVC com Camadas
- **Vantagem**: Separação clara de responsabilidades
- **Desvantagem**: Mais arquivos em projetos pequenos
- **Justificativa**: Escalabilidade e manutenibilidade

### 2. Serviços Externos Simulados (Fake)
- **Alternativa ao enum com contador**: Usando `@Service` com implementação fake
- **Vantagem**: Fácil trocar por implementação real
- **Desvantagem**: Menos controle fino sobre cenários

### 3. Transações na Service
- **@Transactional**: Garante persistência de mudanças de status
- **Rollback automático** em caso de exceção

### 4. DTOs para Requisição/Resposta
- **Vantagem**: Desacoplamento entre API e modelo de dados
- **Segurança**: Evita exposição de campos internos

### 5. GlobalExceptionHandler
- **Vantagem**: Tratamento centralizado de exceções
- **Resultado**: APIs consistentes com HTTP status corretos

## Pontos a Melhorar (com Mais Tempo)

### 1. Cache
```java
@Cacheable("customers")
public CustomerDto validateCustomer(String customerId)
```
Reduziria chamadas repetidas ao serviço de clientes.

### 2. Circuit Breaker
```java
@CircuitBreaker(name = "logistics", fallbackMethod = "fallback")
```
Proteção contra falhas em cascata de serviços externos.

### 3. Retry Logic
```java
@Retry(maxAttempts = 3, delay = 1000)
```
Retentar em caso de falhas transientes.

### 4. Auditoria
Rastrear todas as mudanças de status com timestamp e usuário.

### 5. Logging Estruturado
```java
log.info("Request processed", MDC.put("requestId", id));
```
Facilita rastreamento em sistemas distribuídos.

### 6. Paginação
```java
@GetMapping
public Page<TerminalRequestDto> listRequests(Pageable pageable)
```

### 7. Validações Customizadas
```java
@CustomValidation
private String customerId;
```

### 8. Eventos de Domínio
```java
applicationEventPublisher.publishEvent(new TerminalReservedEvent(...));
```

### 9. Testes com TestContainers
```java
@Testcontainers
class IntegrationTest {
    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer<>();
}
```

### 10. Documentação Swagger/OpenAPI
```java
@OpenAPIDefinition
```

## Troubleshooting

### Erro: "Connection refused" ao banco
- Certifique-se que PostgreSQL/Docker está rodando
- Verifique credenciais em `application.properties`

### Erro: "No terminals available"
- Verifique se `data.sql` foi executado
- Consulte tabela `terminals` no banco

### Erro ao compilar
- Execute `mvn clean install`
- Verifique Java 21+ está instalado: `java -version`

## Contato

Para dúvidas sobre a solução, refira-se aos comentários de código e testes.

---

**Última atualização**: Janeiro 2024
**Versão**: 1.0.0

