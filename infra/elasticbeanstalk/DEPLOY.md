# Deploy na AWS — Elastic Beanstalk (Single instance)

> **App Runner descontinuado para novos clientes** (a partir de 30/04/2026).  
> A AWS recomenda **ECS Express Mode**, mas para este projeto acadêmico (orçamento US$ 8–15/mês) adotamos **Elastic Beanstalk Docker — Single instance**.

## Por que Elastic Beanstalk (e não ECS Express Mode)?

| Critério | ECS Express Mode | Elastic Beanstalk Single instance |
|---|---|---|
| HTTPS automático | Sim | HTTP nativo; HTTPS via CloudFront (opcional) |
| Complexidade | Baixa | Média |
| Custo contínuo | Alto (~US$ 45–70/mês com ALB+Fargate) | Baixo (~US$ 8–15/mês com EC2 pequena) |
| ECR + IAM + SQS | Sim | Sim |
| Ideal para FASE 5 acadêmica | Demo curta | **Demo + custo baixo** |

## Pré-requisitos

- Conta AWS (`637423599009`, região `us-east-1`)
- Imagem no ECR: `637423599009.dkr.ecr.us-east-1.amazonaws.com/veiculos-pos-tech:latest`
- AWS CLI configurada (`aws configure`)

## Arquitetura

```
Internet
   ↓
CloudFront (HTTPS opcional)
   ↓
Elastic Beanstalk Single Instance (EC2)
   ↓
Container Spring Boot :8083 (ECR)
   ↓
RDS PostgreSQL + SQS + Lambda (opcional)
```

---

## 1. RDS PostgreSQL

Console → **RDS** → **Create database**

| Campo | Valor |
|---|---|
| Engine | PostgreSQL 16 |
| Template | Free tier (se elegível) |
| Instance | `db.t3.micro` ou `db.t4g.micro` |
| DB name | `veiculos` |
| Master username | `postgres` |
| Master password | *(anote)* |
| Public access | **No** (recomendado) |
| VPC | default ou a mesma do Beanstalk |

Anote o **endpoint** (ex.: `veiculos.xxxxx.us-east-1.rds.amazonaws.com`).

> Confira elegibilidade: **Billing → Free Tier**.

---

## 2. Amazon SQS

```powershell
aws sqs create-queue --queue-name veiculos-eventos --region us-east-1
```

Anote a **QueueUrl** retornada.

---

## 3. IAM — instance profile do Beanstalk

1. **IAM → Roles → Create role**
2. **AWS service → Elastic Beanstalk → Elastic Beanstalk environment**
3. Anexe a policy: [`../iam/policy-minima-beanstalk-ec2.json`](../iam/policy-minima-beanstalk-ec2.json)
4. Nome sugerido: `aws-elasticbeanstalk-ec2-veiculos-role`

> O Beanstalk também precisa da role padrão de serviço (`aws-elasticbeanstalk-service-role`) — o assistente de criação oferece criar automaticamente.

---

## 4. Preparar pacote de deploy

O arquivo [`Dockerrun.aws.json`](Dockerrun.aws.json) já aponta para a imagem ECR.

Empacote para upload (PowerShell, na pasta `infra/elasticbeanstalk`):

```powershell
cd infra/elasticbeanstalk
Compress-Archive -Path Dockerrun.aws.json -DestinationPath veiculos-beanstalk.zip -Force
```

---

## 5. Criar ambiente Elastic Beanstalk

Console → **Elastic Beanstalk** → **Create application**

| Campo | Valor |
|---|---|
| Application name | `veiculos-api` |
| Platform | **Docker** → Docker running on 64bit Amazon Linux 2023 |
| Application code | Upload `veiculos-beanstalk.zip` |

### Environment tier

- **Single instance** (não Load balanced) — mais barato para demo acadêmica

### Instance settings

| Campo | Valor sugerido |
|---|---|
| Instance type | `t3.small` ou `t3.micro` (free tier EC2 se elegível) |
| EC2 instance profile | `aws-elasticbeanstalk-ec2-veiculos-role` |

### Variáveis de ambiente (Configuration → Software)

| Nome | Valor |
|---|---|
| `DB_URL` | `jdbc:postgresql://SEU-RDS:5432/veiculos` |
| `DB_USERNAME` | `postgres` |
| `DB_PASSWORD` | *(senha RDS)* |
| `JWT_SECRET` | *(≥32 caracteres)* |
| `AWS_ENABLED` | `true` |
| `AWS_SQS_ENABLED` | `true` |
| `AWS_SQS_QUEUE_URL` | *(URL da fila)* |
| `AWS_REGION` | `us-east-1` |
| `SERVER_PORT` | `8083` |

**Não** definir `SPRING_PROFILES_ACTIVE=docker` nem `AWS_ENDPOINT`.

### Rede (Beanstalk ↔ RDS)

- Coloque Beanstalk e RDS na **mesma VPC**
- Security Group do RDS: inbound **5432** a partir do Security Group da instância Beanstalk

---

## 6. Validar deploy

URL pública (formato típico):

```
http://veiculos-api.us-east-1.elasticbeanstalk.com
```

Testes:

```powershell
curl http://SEU-DOMINIO-EB/actuator/health
curl http://SEU-DOMINIO-EB/swagger-ui.html
```

Login:

```powershell
curl -X POST http://SEU-DOMINIO-EB/auth/login `
  -H "Content-Type: application/json" `
  -d '{"username":"admin","senha":"admin123"}'
```

Logs: **Elastic Beanstalk → Logs → Request logs / Full logs** ou CloudWatch.

---

## 7. HTTPS com CloudFront (opcional, evidência FASE 5)

1. **CloudFront → Create distribution**
2. Origin: domínio HTTP do Beanstalk
3. Viewer protocol policy: **Redirect HTTP to HTTPS**
4. Use a URL `https://xxxxx.cloudfront.net` nos relatórios

---

## 8. Lambda + SQS (opcional)

Igual ao guia anterior: [`../lambda/gerar-codigo-pagamento`](../lambda/gerar-codigo-pagamento) + event source mapping na fila `veiculos-eventos`.

---

## Estimativa de custo

| Recurso | Estimativa |
|---|---|
| Elastic Beanstalk (taxa) | US$ 0 |
| EC2 t3.micro/small | US$ 7–10/mês |
| RDS free tier | US$ 0 (12 meses) ou US$ 12–20/mês |
| SQS / Lambda (baixo volume) | ~US$ 0 |
| CloudFront (demo) | centavos |
| **Total (com RDS free tier)** | **~US$ 8–15/mês** |

> **Pare o ambiente EB** e **pare o RDS** quando não estiver demonstrando.

---

## O que rodou onde (evidência FASE 5)

| Componente | Local | Nuvem real |
|---|---|---|
| API Spring Boot | docker-compose | **Elastic Beanstalk** (EC2 + container ECR) |
| PostgreSQL | container | **RDS** |
| SQS | LocalStack | **Amazon SQS** |
| Lambda | LocalStack | **AWS Lambda** |
| HTTPS | — | **CloudFront** (opcional) |
| Logs | stdout | **CloudWatch / EB logs** |

---

## Alternativa: ECS Express Mode

Use apenas se precisar de HTTPS automático **sem CloudFront** e aceitar custo maior (~US$ 45–70/mês) ou ambiente ligado só durante a apresentação.

Substituto oficial do App Runner: https://docs.aws.amazon.com/AmazonECS/latest/developerguide/express-mode.html
