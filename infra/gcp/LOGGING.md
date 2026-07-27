# Logs estruturados — Etapa 8

Observabilidade da SAGA com **correlationId** e **sagaId**, compatível com **Google Cloud Logging**, sem expor PII.

---

## Campos nos logs

| Campo | Origem | Descrição |
|-------|--------|-----------|
| `correlationId` | Header `X-Correlation-Id` ou UUID gerado | Rastreia uma requisição HTTP ponta a ponta |
| `sagaId` | `reservaId` da SAGA | Rastreia o fluxo de compra (reserva → pagamento → venda) |
| `severity` | Nível do log (profile `gcp`) | Mapeado para Cloud Logging |
| `service` | Constante `veiculos-api` | Identifica o serviço no GCP |

**Nunca logados:** CPF, nome, e-mail, telefone, `clienteId` (LGPD).

---

## Profile `gcp` — JSON no stdout

Cloud Run envia stdout para Cloud Logging. Exemplo:

```json
{
  "timestamp": "2026-07-26T16:00:00.123-03:00",
  "severity": "INFO",
  "service": "veiculos-api",
  "correlationId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "sagaId": "42",
  "message": "Evento SAGA publicado tipo=RESERVA_CRIADA reservaId=42 veiculoId=15",
  "logger_name": "com.example.veiculo.messaging.SagaEventBus"
}
```

Consulta no Cloud Logging:

```
resource.type="cloud_run_revision"
jsonPayload.sagaId="42"
```

```
resource.type="cloud_run_revision"
jsonPayload.correlationId="a1b2c3d4-e5f6-7890-abcd-ef1234567890"
```

---

## Dev local / Docker / testes

Profile `!gcp` usa padrão legível:

```
2026-07-26T16:00:00.123-03:00 [a1b2c3d4-...] [42]  INFO --- [nio-8083-exec-1] c.e.v.messaging.SagaEventBus : Evento SAGA publicado ...
```

---

## Propagação

```
HTTP Request
  └─ CorrelationIdFilter → MDC correlationId
       └─ CompraSagaOrchestrator / SagaEventBus → MDC sagaId
            └─ SagaEvent.metadata { correlationId, sagaId }
                 └─ Pub/Sub → Cloud Function (lê sagaId do payload)
```

Cliente pode enviar header:

```http
X-Correlation-Id: meu-id-de-rastreio
```

A resposta devolve o mesmo header.

---

## Código

| Classe | Função |
|--------|--------|
| `CorrelationIdFilter` | MDC + header HTTP |
| `SagaLoggingContext` | Utilitário MDC |
| `SagaEventBus` | Enriquece eventos + log |
| `CompraSagaOrchestrator` | Log de transições SAGA |
| `logback-spring.xml` | JSON (gcp) vs texto (local) |

---

## SQL e LGPD

Hibernate SQL permanece em `WARN` (`application.yml`) para não vazar dados pessoais nos logs.
