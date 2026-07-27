# FASE 5 — Plataforma de Revenda de Veículos

**Projeto:** API de Revenda de Veículos (Spring Boot 4.1 / Java 21 / PostgreSQL)
**Repositório:** https://github.com/guizin9/veiculos-pos-tech
**Autor:** _<seu nome>_

Este documento reúne os entregáveis solicitados na FASE 5:

1. Desenho da arquitetura em nuvem (com priorização de serviços serverless/gerenciados e serviços de segurança).
2. Relatório de segurança de dados.
3. Relatório de orquestração SAGA.

> Observação: os diagramas estão em [Mermaid](https://mermaid.js.org/). Ao exportar para PDF (ex.: extensão *Markdown PDF* do VS Code, ou colando no [mermaid.live](https://mermaid.live)), os diagramas são renderizados automaticamente.

---

## Contexto e modelagem atual da solução

A API já implementa o domínio de negócio da revenda. As entidades relevantes são:

| Entidade | Principais campos | Função |
|---|---|---|
| `Marca`, `Modelo`, `Versao`, `Cor` | descrição / relacionamentos | Dados de referência (catálogo) |
| `Veiculo` | `status` (A/R/V/D), `anoFabricacao`, `anoModelo`, `chassi`, `valor`, `versao`, `cor` | Estoque de veículos à venda |
| `Cliente` | `nome`, `cpf`, endereço completo, `celular`, `foneFixo`, `email` | Comprador cadastrado |
| `ReservaVendaVeiculo` | `status` (R/V/C), `valor`, `cliente`, `veiculo`, `dtReserva`, `dtVenda`, `dtCancelamento`, `retirado` (S/N), `dtRetirada` | Transação que conduz o processo de compra |

O **processo de compra** é conduzido pela entidade `ReservaVendaVeiculo` e transita por estados que espelham exatamente o fluxo descrito no case:

```
seleção → reserva (R) → código de pagamento → pagamento → venda (V) → retirada (retirado = S)
                     └────────────── cancelamento (C) em qualquer etapa ───────────────┘
```

Os endpoints já cobrem: inclusão de reserva (`POST /reserva-venda-veiculos`), confirmação de venda (`DELETE /confirma-venda/{id}`), retirada (`DELETE /retira-veiculo/{id}`), cancelamento (`DELETE /{id}`) e as listagens ordenadas por preço (`/reservados`, `/vendidos`, `/cancelados`, `/pendentes-de-retirada`, `/retirados`).

A partir desse ponto, a FASE 5 define **como esse sistema deve rodar em nuvem, como os dados sensíveis são protegidos e como o processo de compra deve ser orquestrado de forma resiliente (SAGA)**.

---

## Implementação realizada (evidências no código)

> Este bloco descreve **o que foi implementado de fato** no repositório, complementando a proposta de arquitetura nas seções seguintes.

| Requisito FASE 5 | Status | Evidência |
|---|---|---|
| Código de pagamento fictício | ✅ | `Pagamento`, `PagamentoService`, `POST /pagamentos/pagar/{codigo}` |
| Expiração de reserva | ✅ | `ReservaExpiracaoScheduler`, `app.reserva.minutos-expiracao` |
| Documentação de retirada | ✅ | `DocumentacaoRetiradaService`, código `DOC-XXXXXXXX` |
| Autenticação JWT + RBAC | ✅ | `AuthController`, `SecurityConfiguration`, `@PreAuthorize` |
| LGPD (CPF, mascaramento, logs) | ✅ | `CpfUtil`, `ClienteDtoSaida`, SQL em WARN |
| SAGA orquestrada | ✅ | `CompraSagaOrchestrator`, `SagaCompra`, compensações |
| Pagamento antes da venda | ✅ | `validarPodeConfirmarVenda()` |
| Mensageria SQS + Lambda (local) | ✅ | `SqsEventPublisher`, LocalStack, Lambda Node.js |
| Mensageria Pub/Sub + Cloud Function (GCP) | ✅ | `GooglePubSubPublisher`, tópico `veiculos-eventos`, `gerar-codigo-pagamento` |
| Docker Compose | ✅ | `docker-compose.yml`, `Dockerfile` |
| Actuator + Swagger | ✅ | `/actuator/health`, `/swagger-ui.html` |
| Testes automatizados | ✅ | 8 testes (`CompraFluxoIntegrationTest`, SAGA unitário) |
| Deploy GCP Cloud Run (nuvem real) | ✅ | `infra/gcp/DEPLOY.md` — **produção ativa** |
| Deploy App Runner AWS (alternativa) | 📋 Documentado | `infra/apprunner/DEPLOY.md` |

**Decisão arquitetural:** manter **um único monólito Spring Boot**. A SAGA é orquestrada internamente; SQS/Lambda (local) e Pub/Sub/Cloud Function (GCP) servem como integração assíncrona e evidência serverless, sem dividir em microserviços.

### Deploy em produção (GCP — veiculos-pos-tech)

| Recurso | Valor |
|---------|-------|
| **URL da API** | https://veiculos-api-184616306282.us-central1.run.app |
| **Projeto GCP** | `veiculos-pos-tech` |
| **Cloud Run** | `veiculos-api` (revisão ativa com profile `gcp`) |
| **Cloud SQL** | PostgreSQL 16 — `veiculos-pos-tech:us-central1:veiculos-pos-tech-project` |
| **Pub/Sub** | Tópico `veiculos-eventos` |
| **Cloud Function** | `gerar-codigo-pagamento` (Gen2, trigger Pub/Sub) |
| **Secret Manager** | `veiculos-db-password`, `veiculos-jwt-secret` |

**Teste rápido (demo):**

```bash
# Login (campo de senha é "senha", não "password")
curl -X POST https://veiculos-api-184616306282.us-central1.run.app/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","senha":"admin123"}'

# Health
curl https://veiculos-api-184616306282.us-central1.run.app/actuator/health

# Após criar reserva autenticado — logs da Cloud Function
gcloud functions logs read gerar-codigo-pagamento --gen2 --region=us-central1 --project=veiculos-pos-tech --limit=5
```

```mermaid
flowchart TB
    User["Cliente / Swagger UI"]
    CR["Cloud Run — veiculos-api\nSpring Boot profile gcp"]
    SQL["Cloud SQL PostgreSQL 16"]
    SM["Secret Manager\nJWT + senha DB"]
    PS["Pub/Sub veiculos-eventos"]
    CF["Cloud Function\ngerar-codigo-pagamento"]
    LOG["Cloud Logging JSON"]

    User --> CR
    CR --> SQL
    CR --> SM
    CR --> PS
    PS --> CF
    CR --> LOG
    CF --> LOG
```

---

## 1. Desenho da Arquitetura em Nuvem

### 1.1. Princípios adotados

- **Serverless e gerenciado primeiro:** minimizar operação de infraestrutura (patching, escalonamento, alta disponibilidade), pagando por uso e escalando conforme a demanda do e‑commerce.
- **Segurança por padrão (*security by design* e *defense in depth*):** criptografia em trânsito e em repouso, segregação de rede, gestão centralizada de segredos e de identidade.
- **Observabilidade e auditoria** desde o início, exigência reforçada pelo tratamento de dados pessoais (LGPD).

> A referência de provedor em produção é a **GCP** (Cloud Run). Há equivalência direta com AWS e Azure (tabela na seção 1.5).
### 1.2. Diagrama da arquitetura (AWS — proposta de produção)

> **Implementação adotada em produção:** **GCP Cloud Run** + Cloud SQL + Pub/Sub + Cloud Function (ver seção *Deploy em produção* acima). Localmente: Docker Compose + LocalStack simula SQS/Lambda. Alternativa AWS documentada em `infra/apprunner/DEPLOY.md`.
```mermaid
flowchart TB
    User["Cliente / Frontend (SPA)"]
    UX["Frontend hospedado em S3 + CloudFront (CDN)"]

    subgraph Edge["Borda / Segurança de entrada"]
        WAF["AWS WAF + Shield"]
        APIGW["Amazon API Gateway (HTTPS/TLS)"]
        Cognito["Amazon Cognito (Autenticação/JWT) — ou JWT próprio no App Runner"]
    end

    subgraph Compute["Aplicação (containers gerenciados)"]
        AppRunner["API Spring Boot em AWS App Runner (Auto Scaling)"]
    end

    subgraph Orchestration["Orquestração da compra (SAGA)"]
        SagaMono["CompraSagaOrchestrator (dentro do monólito)"]
        SQS["Amazon SQS (filas + DLQ)"]
        LPay["Lambda: gera código de pagamento (evidência serverless)"]
    end

    subgraph Data["Dados (gerenciado)"]
        RDS["Amazon RDS PostgreSQL"]
        Secrets["AWS Secrets Manager"]
        KMS["AWS KMS (chaves de criptografia)"]
    end

    subgraph Observ["Segurança & Observabilidade"]
        CW["CloudWatch (logs/métricas/alarmes)"]
        CT["CloudTrail (auditoria de API)"]
    end

    User --> UX --> CloudFront --> WAF --> APIGW
    APIGW --> Cognito
    APIGW --> AppRunner
    AppRunner --> SagaMono
    SagaMono --> SQS
    SQS --> LPay
    AppRunner --> RDS
    AppRunner --> Secrets
    Secrets --> KMS
    RDS --> KMS
    AppRunner --> CW
    APIGW --> CT
```

### 1.2.1. Diagrama local (Docker Compose + LocalStack)

```mermaid
flowchart LR
    Dev["Desenvolvedor"] --> App["Spring Boot :8083"]
    App --> PG["PostgreSQL :5433"]
    App --> LS["LocalStack :4566"]
    LS --> SQS2["SQS veiculos-eventos"]
    SQS2 --> Lambda2["Lambda gerar-codigo-pagamento"]
```

### 1.3. Serviços escolhidos e justificativas

| Camada | Serviço (AWS) | Por que (justificativa) |
|---|---|---|
| **CDN + Frontend** | **S3 + CloudFront** | Hospedagem estática do SPA do time de UX/frontend com baixa latência global, cache e HTTPS gerenciado. Serverless, custo por uso. |
| **Proteção de borda** | **AWS WAF + Shield** | Bloqueia OWASP Top 10 (SQLi, XSS), rate limiting e mitigação de DDoS antes de chegar à aplicação. Gerenciado, com regras atualizadas pela AWS. |
| **API Gateway** | **Amazon API Gateway** | Ponto único de entrada, encerra TLS, aplica *throttling*, valida token JWT e integra nativamente com Cognito. Reduz superfície de exposição da aplicação. |
| **Autenticação/Autorização** | **Amazon Cognito** | Gerência de identidade gerenciada (login, MFA, emissão de JWT/OAuth2). Essencial para a regra "venda somente para compradores cadastrados". Evita implementar e operar auth próprio. |
| **Aplicação** | **AWS App Runner** *(implementado)* / ECS Fargate *(proposta alternativa)* | App Runner é o serviço mais simples para deploy de container Spring Boot: HTTPS gerenciado, auto scaling e custo baixo (~US$ 6–16/mês). Fargate oferece mais controle de rede/VPC para produção enterprise. |
| **Orquestração da compra** | **`CompraSagaOrchestrator` no monólito** + SQS | Orquestração interna garante consistência sem microserviços; SQS desacopla eventos assíncronos. Step Functions permanece como evolução para fluxos multi-serviço. |
| **Funções pontuais** | **AWS Lambda** | Tarefas curtas e event‑driven: gerar código de pagamento e emitir documentação de retirada. Serverless, escala a zero, custo por execução. |
| **Mensageria** | **Amazon SQS (+ DLQ)** | Desacopla passos assíncronos (ex.: confirmação de pagamento) e garante processamento *at‑least‑once*; a *Dead Letter Queue* isola mensagens com falha para reprocesso. |
| **Banco de dados** | **Amazon Aurora PostgreSQL Serverless v2** | Compatível com o PostgreSQL já usado no projeto. Gerenciado (backup, patch, réplicas), escala automaticamente a capacidade e oferece criptografia em repouso via KMS. |
| **Segredos** | **AWS Secrets Manager** | Armazena credenciais de banco e chaves de integração fora do código/`application.yml`, com rotação automática. |

### 1.4. Serviços de segurança da nuvem (e justificativa do uso)

| Serviço | Uso na solução | Justificativa |
|---|---|---|
| **AWS KMS** | Chaves para criptografar Aurora, S3, SQS, Secrets e campos sensíveis (envelope encryption). | Centraliza a gestão do ciclo de vida das chaves, com rotação e trilha de auditoria de uso — requisito para dados pessoais (LGPD). |
| **AWS IAM** | Permissões mínimas (*least privilege*) por serviço/role. | Garante que cada componente só acesse o que precisa; segrega funções (ex.: Lambda de documentação não altera estoque). |
| **Amazon Cognito** | Autenticação de compradores e emissão de JWT. | Atende "venda somente para compradores cadastrados" e habilita MFA para operações sensíveis. |
| **AWS WAF + Shield** | Filtro de aplicação e anti‑DDoS. | Protege a API pública contra ataques automatizados e injeção. |
| **AWS CloudTrail** | Registro imutável de todas as chamadas de API da conta. | Auditoria e resposta a incidentes; evidência de quem acessou o quê. |
| **Amazon CloudWatch** | Logs, métricas e alarmes. | Detecção de anomalias (picos de erro/latência) e base para auditoria operacional. |
| **Amazon GuardDuty** | Detecção contínua de ameaças. | Identifica acessos anômalos/credenciais comprometidas sem esforço operacional. |
| **VPC + Security Groups + Subnets privadas** | Aurora e Fargate em subnets privadas; acesso ao banco só a partir da aplicação. | Isolamento de rede; o banco de dados nunca é exposto à internet. |

### 1.5. Equivalência entre provedores

| Função | AWS | Azure | GCP |
|---|---|---|---|
| API/Gateway | API Gateway | API Management | API Gateway / Apigee |
| Identidade | Cognito | Entra ID B2C | Identity Platform |
| Containers gerenciados | ECS Fargate | Container Apps | Cloud Run |
| Funções | Lambda | Azure Functions | Cloud Functions |
| Orquestração SAGA | Step Functions | Logic Apps / Durable Functions | Workflows |
| Banco relacional | Aurora Serverless | Azure DB for PostgreSQL Flexible | Cloud SQL / AlloyDB |
| Segredos | Secrets Manager | Key Vault | Secret Manager |
| Chaves | KMS | Key Vault | Cloud KMS |
| WAF | AWS WAF | Azure WAF | Cloud Armor |

---

## 2. Relatório de Segurança de Dados

### 2.1. Dados armazenados pela solução

| Entidade | Campos | Classificação |
|---|---|---|
| `Cliente` | `nome` | Pessoal |
| | `cpf` | **Pessoal sensível / crítico** (identificador único) |
| | `logradouro`, `numero`, `complemento`, `bairro`, `cidade`, `estado`, `cep` | **Pessoal** (endereço) |
| | `celular`, `foneFixo`, `email` | **Pessoal** (contato) |
| `Veiculo` | `chassi`, `valor`, `anoFabricacao`, `anoModelo`, `versao`, `cor`, `status` | Operacional / não pessoal |
| `ReservaVendaVeiculo` | `valor`, `status`, `dtReserva`, `dtVenda`, `dtCancelamento`, `dtRetirada`, `retirado`, vínculo `cliente`/`veiculo` | Operacional; torna‑se **pessoal por associação** (liga um comprador a uma compra) |
| `Marca`, `Modelo`, `Versao`, `Cor` | descrições | Referência / público |
| (novo) **Código de pagamento** | código, valor, validade, status | **Financeiro sensível** |

### 2.2. Quais são os dados sensíveis

Sob a ótica da **LGPD**, todos os campos da entidade `Cliente` são **dados pessoais**. Os de maior criticidade:

- **CPF** — identificador direto; alvo preferencial de fraude. Necessário para emitir o código de pagamento e a documentação do veículo.
- **Endereço completo** — necessário para a documentação de retirada; permite localização física do titular.
- **Contatos (email, celular, fone)** — vetor de *phishing* e engenharia social.
- **Código/dados de pagamento** — dado financeiro; exige proteção reforçada e menor tempo de retenção.

> Observação sobre o modelo atual: a coluna `cpf` está com tamanho 15 e sem restrição de unicidade; o campo único hoje é `nome`. Recomenda‑se tornar o **CPF único e validado**, e **não** usar `nome` como chave única.

### 2.3. Políticas de acesso a dados implementadas / recomendadas

**Implementado no código:**

1. **Autenticação JWT (Spring Security OAuth2 Resource Server):** `POST /auth/login` emite token HMAC; rotas protegidas exigem `Authorization: Bearer`. Exceções públicas: catálogo `/veiculos/a-venda`, health, Swagger.
2. **Autorização por papel (RBAC):** `@PreAuthorize` nos endpoints sensíveis (pagamentos, confirmação de venda). Papéis: `CLIENTE`, `VENDEDOR`, `OPERADOR`, `ADMIN`.
3. **Regra "venda só para cadastrado":** `ReservaVendaVeiculoNegocioValidator` exige cliente existente e **ativo**.
4. **Mascaramento na saída:** `ClienteDtoSaida` mascara CPF (`***.***.***-XX`).
5. **Logs sem PII:** `show-sql: false`, Hibernate SQL/binder em WARN.
6. **Segredos externalizados:** `DB_*`, `JWT_SECRET`, `AWS_*` via variáveis de ambiente.

**Recomendado para produção (evolução):**

1. **Cognito** em vez de JWT próprio (MFA, gestão de usuários).
2. **Vínculo `Usuario` ↔ `Cliente`** para que `CLIENTE` acesse apenas suas reservas.
3. **IAM least privilege** por serviço (App Runner role, Lambda role).
4. **Criptografia de campo** (KMS) para CPF e códigos de pagamento.
5. **Segregação de rede:** RDS em subnet privada.

### 2.4. Políticas de segurança da operação (tratamento dos dados)

- **Criptografia em trânsito:** TLS 1.2+ obrigatório (CloudFront → API Gateway → Fargate).
- **Criptografia em repouso:** Aurora, S3, SQS e Secrets criptografados com **KMS**. Para CPF e código de pagamento, aplicar criptografia adicional em nível de campo (envelope encryption).
- **Gestão de segredos:** credenciais fora do `application.yml`, em **Secrets Manager**, com rotação automática.
- **Minimização e finalidade (LGPD):** coletar apenas o necessário para pagamento e documentação; definir base legal (execução de contrato).
- **Retenção e descarte:** política de expurgo — código de pagamento expira; dados de clientes que desistiram/cancelaram são anonimizados após prazo legal.
- **Auditoria/rastreabilidade:** `CloudTrail` + logs de aplicação registram *quem* acessou dado pessoal, *quando* e *por quê*. O campo `dtOpera` já existe nas entidades e ajuda na trilha.
- **Backup e recuperação:** *point‑in‑time recovery* do Aurora; testes periódicos de restauração.
- **Resposta a incidentes:** GuardDuty + alarmes CloudWatch acionam o plano de resposta; notificação à ANPD e aos titulares quando aplicável.

### 2.5. Riscos e ações de mitigação

| # | Risco | Impacto | Mitigação |
|---|---|---|---|
| 1 | ~~**API aberta**~~ | ~~Alto~~ | ✅ JWT + RBAC implementados. Pendente: vínculo usuário↔cliente. |
| 2 | Vazamento de **CPF/endereço** | Alto (LGPD, fraude) | ✅ Mascaramento na saída; evolução: criptografia KMS em repouso. |
| 3 | **SQL Injection / XSS** | Alto | JPA parametrizado + Bean Validation; WAF na borda (produção). |
| 4 | ~~**Credenciais no código/config**~~ | ~~Alto~~ | ✅ Variáveis de ambiente; evolução: Secrets Manager com rotação. |
| 5 | **Reserva concorrente** | Médio | Checagem de status R/V + transação; evolução: lock otimista. |
| 6 | ~~**Pagamento não efetuado / desistência**~~ | ~~Médio~~ | ✅ Scheduler de expiração + compensação SAGA. |
| 7 | **Perda de dados** | Alto | Backups automatizados, multi‑AZ, testes de restauração. |
| 8 | **Acesso indevido interno** | Médio | RBAC, IAM least privilege, CloudTrail e revisão de acessos. |
| 9 | **Ataque de negação de serviço** | Médio | WAF + Shield + throttling no API Gateway. |
| 10 | **Retenção excessiva de dados** | Médio (LGPD) | Política de retenção/expurgo e anonimização de compras canceladas. |

---

## 3. Relatório de Orquestração SAGA

### 3.1. Por que SAGA neste problema

O processo de compra envolve **múltiplos passos e serviços** (reserva de estoque, geração de código de pagamento, confirmação de pagamento, baixa de estoque, emissão de documentação) que precisam ser **consistentes** mesmo com falhas parciais (outro cliente reservou antes, pagamento não efetuado, desistência). Em uma arquitetura distribuída/serverless não há uma transação ACID única entre serviços; por isso usa‑se o padrão **SAGA**, uma sequência de transações locais em que cada passo tem uma **transação de compensação** para desfazer efeitos anteriores.

### 3.2. Tipo de SAGA adotado: **Orquestração interna**

**Escolha implementada: SAGA por Orquestração, com `CompraSagaOrchestrator` dentro do monólito Spring Boot.**

A proposta original usava AWS Step Functions como orquestrador externo. Para manter um único deploy (requisito do case) e simplificar a demonstração, a orquestração foi implementada **no próprio monólito**, com:

- Entidade `SagaCompra` persistindo etapa e status.
- Validações de transição e idempotência por ordinal de etapa.
- Compensações em cancelamento (`CANCELADA`) e expiração (`EXPIRADA`).
- Eventos publicados em **Amazon SQS** (LocalStack local) para desacoplamento e evidência de mensageria.

**Justificativa (mantida da proposta original):**

1. Fluxo com estados bem definidos e sequenciais.
2. Compensações explícitas e críticas.
3. Timeouts e desistência via scheduler.
4. Observabilidade via entidade `SagaCompra` + logs + eventos SQS.
5. Evolução futura para Step Functions sem reescrever a lógica de negócio.

### 3.3. Fluxo da SAGA (com compensações)

```mermaid
stateDiagram-v2
    [*] --> Selecionado
    Selecionado --> Reservado: Reservar veículo (status Veiculo=R, Reserva=R)
    Reservado --> ReservaFalhou: Veículo já reservado por outro
    Reservado --> CodigoGerado: Gerar código de pagamento
    CodigoGerado --> Pago: Pagamento confirmado
    CodigoGerado --> Expirado: Timeout / desistência
    Pago --> Vendido: Confirmar venda (status Veiculo=V, Reserva=V)
    Vendido --> Retirado: Retirada + documentação (retirado=S)
    Retirado --> [*]

    ReservaFalhou --> [*]
    Expirado --> Compensar
    Pago --> Compensar: Falha ao dar baixa
    Compensar --> Cancelado: Liberar veículo (Veiculo=A) e Reserva=C
    Cancelado --> [*]
```

### 3.4. Passos e transações de compensação

| Passo (transação local) | Efeito | Compensação |
|---|---|---|
| **Reservar veículo** | `Veiculo.status = R`, cria `ReservaVendaVeiculo` com status `R` | Cancelar reserva: `Reserva.status = C`, `Veiculo.status = A` |
| **Gerar código de pagamento** | cria código com validade | Expirar/invalidar código |
| **Confirmar pagamento** | marca pagamento OK | Estornar/registrar falha e disparar cancelamento |
| **Confirmar venda** | `Reserva.status = V`, `Veiculo.status = V` | Reverter para `A`/`C` se etapa seguinte falhar |
| **Retirada + documentação** | `retirado = S`, `dtRetirada` | (passo final; sem compensação — emite documento) |

Esse mapeamento corresponde aos métodos em `ReservaVendaVeiculoService` e `PagamentoService`, coordenados por `CompraSagaOrchestrator`:

| Classe / método | Etapa SAGA |
|---|---|
| `incluirReserva()` | `RESERVA` → `PAGAMENTO_GERADO` |
| `PagamentoService.confirmarPagamento()` | `PAGAMENTO_CONFIRMADO` |
| `confirmaVenda()` | `VENDA` (exige pagamento PAGO) |
| `retiraVeiculo()` | `DOCUMENTACAO` → `RETIRADA` |
| `cancela()` | `CANCELADA` (compensação) |
| `PagamentoService.expirarVencidos()` | `EXPIRADA` (compensação) |

### 3.5. Garantias

- **Idempotência:** cada passo deve ser idempotente (reprocesso via SQS/DLQ não duplica efeitos).
- **Consistência eventual:** o sistema converge para "vendido/retirado" ou "cancelado com veículo liberado".
- **Sem estoque preso:** todo caminho de falha executa a compensação que devolve o veículo ao estoque (`status = A`).

---

## 4. Checklist de entrega

- [x] Link do GitHub do código no topo deste documento.
- [ ] Exportar este arquivo para **PDF** (com os diagramas renderizados).
- [x] Desenho da arquitetura (serverless/gerenciados + segurança + justificativas).
- [x] Relatório de segurança de dados.
- [x] Relatório de orquestração SAGA.
- [x] Implementação funcional (pagamento, SAGA, JWT, Docker, testes).
- [ ] Deploy App Runner com URL pública (evidência em nuvem — ver `infra/apprunner/DEPLOY.md`).
