# Deploy GCP — Veículos API (FASE 5)

Guia end-to-end: **Artifact Registry → Cloud Run → Cloud SQL → Secret Manager → Pub/Sub → Cloud Function**.

A aplicação permanece um **monólito Spring Boot**; apenas a infraestrutura de mensageria e deploy usa serviços GCP.

---

## Arquitetura

```
                    ┌─────────────────────────────────────┐
                    │         Cloud Run (API)              │
                    │  Spring Boot — profile gcp           │
                    │  PORT=8080 · logs JSON               │
                    └──────┬──────────┬──────────┬─────────┘
                           │          │          │
              Cloud SQL ◄──┘          │          └──► Pub/Sub (veiculos-eventos)
         (PostgreSQL 16)              │                      │
                           Secret Manager              Cloud Function
                           (JWT, DB pwd)            (gerar-codigo-pagamento)
                                                      Cloud Logging
```

| Componente local (Docker) | GCP produção |
|-----------------------------|--------------|
| Container `app` | **Cloud Run** |
| PostgreSQL | **Cloud SQL** |
| LocalStack SQS | **Pub/Sub** |
| Lambda Node.js | **Cloud Function Gen2** |
| env vars / `.env` | **Secret Manager** (+ montagem Cloud Run) |
| stdout | **Cloud Logging** (JSON estruturado) |

---

## Custos estimados (uso acadêmico / demo)

| Serviço | Tier sugerido | Custo aproximado/mês |
|---------|---------------|----------------------|
| Cloud Run | escala a zero, 512 MiB | US$ 0–5 (idle ≈ US$ 0) |
| Cloud SQL | `db-f1-micro` (recomendado demo) | US$ 7–12 |
| Pub/Sub | baixo volume | US$ 0 (free tier) |
| Cloud Function | Gen2, esporádica | US$ 0 (free tier) |
| Artifact Registry | 1 imagem ~500 MB | US$ 0,05–0,15 |
| Secret Manager | 2 segredos | US$ 0,10 |
| **Total (tier mínimo)** | | **~US$ 8–18/mês** |

> **Produção atual (`veiculos-pos-tech`):** instância `veiculos-pos-tech-project`, user `veiculos_user`, tier `db-custom-2-8192` (~US$ 50–70/mês só do SQL). Para reduzir custo, use `db-f1-micro` ou desligue quando idle: `gcloud sql instances patch veiculos-pos-tech-project --activation-policy=NEVER`.

---

## Pré-requisitos

1. Conta Google Cloud com billing habilitado
2. [Google Cloud SDK](https://cloud.google.com/sdk/docs/install) (`gcloud`) autenticado:
   ```powershell
   gcloud auth login
   gcloud auth application-default login
   ```
3. [Docker Desktop](https://www.docker.com/products/docker-desktop/) (build e push da imagem)
4. Projeto GCP criado (anote o **Project ID**)

---

## Passo 0 — Habilitar APIs

```powershell
cd c:\Cursos\Projetos\Java\veiculos\infra\gcp
.\setup-apis.ps1 -ProjectId SEU_PROJETO
```

---

## Passo 1 — Cloud SQL (PostgreSQL)

### Console (recomendado na 1ª vez)

1. **SQL** → Criar instância → **PostgreSQL 16**
2. ID: `veiculos-db` · Região: `us-central1` · Tier: **Shared core / db-f1-micro** (recomendado para demo)
3. Banco: `veiculos` · Usuário: `veiculos_user` (senha forte)
4. Conexões: **IP privado** ou **Cloud SQL Auth** (Cloud Run usa connector nativo)
5. Anote o **Connection name**: `SEU_PROJETO:us-central1:veiculos-db`

### CLI (alternativa)

```powershell
$PROJECT = "SEU_PROJETO"
$REGION = "us-central1"
$INSTANCE = "veiculos-db"

gcloud sql instances create $INSTANCE `
  --database-version=POSTGRES_16 `
  --tier=db-f1-micro `
  --region=$REGION `
  --storage-size=10GB `
  --storage-auto-increase

gcloud sql databases create veiculos --instance=$INSTANCE
gcloud sql users create veiculos_user --instance=$INSTANCE --password=SENHA_FORTE_AQUI
```

Connection name:

```powershell
gcloud sql instances describe veiculos-db --format="value(connectionName)"
```

---

## Passo 2 — Secret Manager

Siga [`SECRETS.md`](SECRETS.md) para criar `veiculos-db-password` e `veiculos-jwt-secret` e conceder IAM ao service account do Cloud Run.

---

## Passo 3 — Artifact Registry + build

```powershell
# Criar repositório e autenticar Docker
.\setup-artifact-registry.ps1 -ProjectId SEU_PROJETO -Region us-central1

# Build + push (a partir da raiz do repo via Dockerfile)
.\build-and-push.ps1 -ProjectId SEU_PROJETO -Region us-central1 -Tag latest
```

Imagem resultante:

```
us-central1-docker.pkg.dev/SEU_PROJETO/veiculos/api:latest
```

---

## Passo 4 — Pub/Sub + Cloud Function

```powershell
.\setup-pubsub.ps1 -ProjectId SEU_PROJETO
.\deploy-function.ps1 -ProjectId SEU_PROJETO -Region us-central1
```

Detalhes: [`FUNCTIONS.md`](FUNCTIONS.md)

---

## Passo 5 — Cloud Run (API)

```powershell
.\deploy-cloud-run.ps1 `
  -ProjectId SEU_PROJETO `
  -CloudSqlInstance "SEU_PROJETO:us-central1:veiculos-db" `
  -Region us-central1 `
  -AllowUnauthenticated
```

O script configura:
- Profile `gcp`, Cloud SQL connector, Pub/Sub habilitado
- Segredos montados via `--set-secrets` (modo B — recomendado)
- Porta **8080** (Cloud Run padrão)

Template de variáveis: [`cloud-run.env.example`](cloud-run.env.example)

---

## Passo 6 — Verificação (Etapa 9 — Actuator)

```powershell
$URL = gcloud run services describe veiculos-api --region us-central1 --format="value(status.url)"

# Health (deve retornar {"status":"UP"})
curl "$URL/actuator/health"

# Info
curl "$URL/actuator/info"

# Login
curl -X POST "$URL/auth/login" `
  -H "Content-Type: application/json" `
  -d '{"username":"admin","senha":"admin123"}'

# Swagger UI no browser
Start-Process "$URL/swagger-ui.html"
```

### Teste SAGA + serverless

1. Autentique como `vendedor` / `vendedor123`
2. `POST /reservas-vendas/reservar` — cria reserva
3. Cloud Logging → filtrar logs da API (`jsonPayload.sagaId`)
4. Logs da Cloud Function:
   ```powershell
   gcloud functions logs read gerar-codigo-pagamento --gen2 --region=us-central1 --limit=10
   ```

---

## Ordem resumida (checklist)

```
[ ] setup-apis.ps1
[ ] Cloud SQL criado + banco veiculos + usuário veiculos_user
[ ] SECRETS.md — segredos + IAM
[ ] setup-artifact-registry.ps1
[ ] build-and-push.ps1
[ ] setup-pubsub.ps1
[ ] deploy-function.ps1
[ ] deploy-cloud-run.ps1 -AllowUnauthenticated
[ ] curl /actuator/health → UP
[ ] reserva de teste + logs Pub/Sub + Cloud Function
```

---

## IAM mínimo (service account Cloud Run)

| Role | Motivo |
|------|--------|
| `roles/cloudsql.client` | Conexão Cloud SQL (automático ao anexar instância) |
| `roles/secretmanager.secretAccessor` | Ler segredos montados |
| `roles/pubsub.publisher` | Publicar eventos SAGA |

Concedido via `setup-pubsub.ps1` (Pub/Sub) e `SECRETS.md` (Secret Manager).

---

## Troubleshooting

| Sintoma | Causa provável | Ação |
|---------|----------------|------|
| Cloud Run não sobe | Imagem não encontrada | Rode `build-and-push.ps1` |
| `Connection refused` DB | Cloud SQL não anexado | `--add-cloudsql-instances` no deploy |
| `403 Secret Manager` | IAM faltando | Ver `SECRETS.md` |
| Pub/Sub sem mensagens | Publisher sem permissão | Rode `setup-pubsub.ps1` |
| Health DOWN / db | Senha ou usuário errado | Verifique segredo `veiculos-db-password` |
| 401 em todos endpoints | JWT / login | Use `/auth/login` primeiro |
| Cold start lento | escala a zero | Normal; 1ª req ~10–30 s |

---

## Referências no repositório

| Arquivo | Conteúdo |
|---------|----------|
| [`SECRETS.md`](SECRETS.md) | Secret Manager |
| [`FUNCTIONS.md`](FUNCTIONS.md) | Cloud Function |
| [`LOGGING.md`](LOGGING.md) | Logs JSON / correlationId |
| [`cloud-run.env.example`](cloud-run.env.example) | Variáveis de ambiente |
| [`../../Dockerfile`](../../Dockerfile) | Build multi-stage Java 21 |
| [`../../src/main/resources/application-gcp.yml`](../../src/main/resources/application-gcp.yml) | Profile produção GCP |

---

## Trilha AWS (alternativa)

Deploy AWS via Elastic Beanstalk + ECR: [`../elasticbeanstalk/DEPLOY.md`](../elasticbeanstalk/DEPLOY.md)

Local com LocalStack: `docker compose up --build` na raiz do projeto.
