# Secret Manager — Veículos API (GCP)

Dois modos de uso (escolha um):

| Modo | Quando usar | Config |
|------|-------------|--------|
| **A — App carrega** | Padrão profile `gcp` | `GCP_SECRET_MANAGER_ENABLED=true` + nomes dos segredos |
| **B — Cloud Run monta** | Recomendado em produção | `--set-secrets` no deploy; desabilite carregamento no app |

Variáveis explícitas (`DB_PASSWORD`, `JWT_SECRET`) **sempre têm prioridade** sobre o Secret Manager.

---

## 1. Criar segredos (PowerShell + gcloud)

Substitua `SEU_PROJETO` e use senhas fortes geradas localmente (não as cole aqui).

```powershell
$PROJECT = "SEU_PROJETO"
gcloud config set project $PROJECT

# Senha do usuário Cloud SQL (veiculos_app)
"SUASENHA_DB_FORTE" | gcloud secrets create veiculos-db-password --data-file=-

# JWT HMAC — mínimo 32 caracteres
"SEU_JWT_SECRETO_COM_32_OU_MAIS_CARACTERES" | gcloud secrets create veiculos-jwt-secret --data-file=-
```

Se o segredo já existir, adicionar nova versão:

```powershell
"NOVA_SENHA" | gcloud secrets versions add veiculos-db-password --data-file=-
```

---

## 2. Permissões IAM

O service account do Cloud Run precisa de `roles/secretmanager.secretAccessor`:

```powershell
$PROJECT_NUMBER = gcloud projects describe $PROJECT --format="value(projectNumber)"
$SA = "$PROJECT_NUMBER-compute@developer.gserviceaccount.com"

gcloud secrets add-iam-policy-binding veiculos-db-password `
  --member="serviceAccount:$SA" `
  --role="roles/secretmanager.secretAccessor"

gcloud secrets add-iam-policy-binding veiculos-jwt-secret `
  --member="serviceAccount:$SA" `
  --role="roles/secretmanager.secretAccessor"
```

---

## 3. Deploy Cloud Run

### Modo A — App carrega (EnvironmentPostProcessor)

```powershell
gcloud run deploy veiculos-api `
  --image REGION-docker.pkg.dev/SEU_PROJETO/veiculos/api:latest `
  --region us-central1 `
  --set-env-vars "SPRING_PROFILES_ACTIVE=gcp,GCP_PROJECT_ID=SEU_PROJETO,GCP_CLOUDSQL_INSTANCE=SEU_PROJETO:us-central1:veiculos-db,DB_USERNAME=veiculos_app,GCP_SECRET_MANAGER_ENABLED=true,GCP_SECRET_DB_PASSWORD=veiculos-db-password,GCP_SECRET_JWT=veiculos-jwt-secret" `
  --add-cloudsql-instances SEU_PROJETO:us-central1:veiculos-db `
  --allow-unauthenticated
```

### Modo B — Cloud Run monta segredos (sem SDK na inicialização)

```powershell
gcloud run deploy veiculos-api `
  --image REGION-docker.pkg.dev/SEU_PROJETO/veiculos/api:latest `
  --region us-central1 `
  --set-env-vars "SPRING_PROFILES_ACTIVE=gcp,GCP_PROJECT_ID=SEU_PROJETO,GCP_CLOUDSQL_INSTANCE=SEU_PROJETO:us-central1:veiculos-db,DB_USERNAME=veiculos_app,GCP_SECRET_MANAGER_ENABLED=false" `
  --set-secrets "DB_PASSWORD=veiculos-db-password:latest,JWT_SECRET=veiculos-jwt-secret:latest" `
  --add-cloudsql-instances SEU_PROJETO:us-central1:veiculos-db `
  --allow-unauthenticated
```

---

## 4. Verificação

```powershell
curl https://URL-DO-SERVICO/actuator/health
```

Logs no Cloud Logging — **nunca** devem exibir `DB_PASSWORD` ou `JWT_SECRET`.

---

## Referência no código

- `GcpSecretManagerEnvironmentPostProcessor` — carrega segredos antes do DataSource
- Profile `gcp` em `application-gcp.yml`
- Template de env: `infra/gcp/cloud-run.env.example`
