# Cloud Function — gerar-codigo-pagamento (Etapa 7)

Função **Gen2** acionada pelo tópico Pub/Sub `veiculos-eventos`. Processa eventos `RESERVA_CRIADA` publicados pelo monólito Spring Boot (`GooglePubSubPublisher`) e registra um código de pagamento fictício no **Cloud Logging**.

Equivalente GCP da Lambda em `infra/lambda/gerar-codigo-pagamento` (trilha AWS/LocalStack).

---

## Pré-requisitos

- [Google Cloud SDK](https://cloud.google.com/sdk/docs/install) (`gcloud`)
- APIs habilitadas no projeto:
  ```powershell
  gcloud services enable cloudfunctions.googleapis.com `
    run.googleapis.com `
    eventarc.googleapis.com `
    pubsub.googleapis.com `
    cloudbuild.googleapis.com `
    --project=SEU_PROJETO
  ```
- Cloud Run já publicando no tópico (`GCP_PUBSUB_ENABLED=true`)

---

## Deploy (ordem recomendada)

### 1. Criar tópico Pub/Sub + permissão de publish

```powershell
cd infra\gcp
.\setup-pubsub.ps1 -ProjectId SEU_PROJETO
```

### 2. Implantar a Cloud Function

```powershell
.\deploy-function.ps1 -ProjectId SEU_PROJETO -Region us-central1
```

Parâmetros opcionais: `-Topic veiculos-eventos`

---

## Teste end-to-end

1. API no Cloud Run com profile `gcp` e Pub/Sub habilitado
2. Autentique e crie uma **reserva** (`POST /reservas-vendas/reservar`)
3. Verifique logs da função:

```powershell
gcloud functions logs read gerar-codigo-pagamento `
  --gen2 --region=us-central1 --limit=10 --project=SEU_PROJETO
```

Saída esperada (JSON estruturado):

```json
{
  "severity": "INFO",
  "origem": "gerar-codigo-pagamento",
  "tipo": "RESERVA_CRIADA",
  "reservaId": 42,
  "veiculoId": 15,
  "codigoAlternativo": "PAG-GCF-XXXXXXXX",
  "mensagem": "Código fictício gerado pela Cloud Function (demonstração FASE 5 GCP)"
}
```

> `clienteId` **não** é logado (LGPD).

---

## Teste local (opcional)

```powershell
cd infra\gcp\functions\gerar-codigo-pagamento
npm install
npm start
```

Em outro terminal, simule um CloudEvent Pub/Sub (requer `curl`):

```powershell
$payload = Get-Content sample-event.json -Raw
$bytes = [System.Text.Encoding]::UTF8.GetBytes($payload)
$b64 = [Convert]::ToBase64String($bytes)

curl -X POST http://localhost:8081 `
  -H "Content-Type: application/json" `
  -d "{ `"message`": { `"data`": `"$b64`", `"messageId`": `"test-1`", `"attributes`": { `"tipo`": `"RESERVA_CRIADA`" } } }"
```

---

## Arquitetura

```
Cloud Run (Spring Boot)
    │ GooglePubSubPublisher
    ▼
Pub/Sub tópico: veiculos-eventos
    │ trigger (Eventarc)
    ▼
Cloud Function: gerar-codigo-pagamento
    │ console.log JSON
    ▼
Cloud Logging
```

A lógica de negócio (pagamento real, SAGA) permanece no monólito. A função é **demonstração serverless** — igual à Lambda AWS.

---

## Troubleshooting

| Problema | Solução |
|----------|---------|
| Função não dispara | Confirme reserva criada e `GCP_PUBSUB_ENABLED=true` no Cloud Run |
| `Permission denied` publish | Rode `setup-pubsub.ps1` para grant `pubsub.publisher` |
| Deploy falha API não habilitada | Rode `gcloud services enable` acima |
| Logs vazios | Aguarde ~30s; filtre por `origem=gerar-codigo-pagamento` no Cloud Logging |

---

## Referências

- Código: `infra/gcp/functions/gerar-codigo-pagamento/`
- Publisher Java: `GooglePubSubPublisher.java`
- Segredos/deploy API: `infra/gcp/SECRETS.md`, `cloud-run.env.example`
