# Deploy na AWS — App Runner (recomendado: mais simples e econômico)

## Por que App Runner (e não ECS Fargate)?

| Critério | App Runner | ECS Fargate |
|---|---|---|
| Complexidade | Baixa (aponta para imagem ECR) | Alta (cluster, task definition, ALB, VPC) |
| Custo parado | Escala automática; paga por uso | Task sempre provisionada ou config extra |
| Ideal para | Monólito Spring Boot em container | Microserviços / controle fino de rede |

Para esta FASE 5, **App Runner + RDS PostgreSQL (free tier ou créditos) + SQS/Lambda/Secrets reais** é o caminho **mais simples e mais barato** que ainda demonstra os conceitos de nuvem.

Alternativas por provedor:
- **AWS App Runner** — recomendado (este guia)
- **Google Cloud Run** — similar, escala a zero, fácil; porém SQS/Lambda são AWS
- **Azure Container Apps** — equivalente Azure; exige adaptar mensageria

## Pré-requisitos

- Conta AWS (solicite créditos FIAP via Discord se necessário)
- AWS CLI configurado (`aws configure`)
- Docker instalado

## 1. Build e push da imagem (ECR)

```bash
# Variáveis
AWS_REGION=us-east-1
AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
REPO=veiculos-api

# Criar repositório ECR
aws ecr create-repository --repository-name $REPO --region $AWS_REGION

# Login no ECR
aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com

# Build e push
docker build -t $REPO .
docker tag $REPO:latest $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$REPO:latest
docker push $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$REPO:latest
```

## 2. Banco gerenciado (RDS PostgreSQL — free tier)

Crie uma instância **db.t3.micro** PostgreSQL (750 h/mês free tier por 12 meses) ou use **RDS Serverless v2** com créditos.

Anote: endpoint, usuário, senha, database `veiculos`.

## 3. Secrets Manager

```bash
aws secretsmanager create-secret \
  --name veiculos/app \
  --secret-string '{"username":"postgres","password":"SUA_SENHA","jwtSecret":"SEU_JWT_SECRET_FORTE_32+CHARS"}'
```

No App Runner, referencie o segredo como variáveis de ambiente (integração nativa).

## 4. SQS (fila real)

```bash
aws sqs create-queue --queue-name veiculos-eventos
```

Anote a `QueueUrl`.

## 5. Criar serviço App Runner

Via console AWS → **App Runner** → Create service:

- **Source:** Container registry → ECR → imagem `veiculos-api:latest`
- **Port:** 8083
- **CPU/Memory:** 0.25 vCPU / 0.5 GB (mínimo, ~US$ 5–15/mês com tráfego baixo)
- **Variáveis de ambiente:**
  - `DB_URL=jdbc:postgresql://SEU-RDS:5432/veiculos`
  - `DB_USERNAME` / `DB_PASSWORD` (referência ao Secrets Manager)
  - `JWT_SECRET` (referência ao Secrets Manager)
  - `AWS_ENABLED=true`
  - `AWS_SQS_ENABLED=true`
  - `AWS_SQS_QUEUE_URL=https://sqs.us-east-1.amazonaws.com/ACCOUNT/veiculos-eventos`
  - `AWS_REGION=us-east-1`
  - (sem `AWS_ENDPOINT` — usa AWS real)
- **IAM role:** anexe a policy em [`../iam/policy-minima-apprunner.json`](../iam/policy-minima-apprunner.json)

## 6. Lambda (opcional em produção)

Faça deploy da função em [`../lambda/gerar-codigo-pagamento`](../lambda/gerar-codigo-pagamento):

```bash
cd infra/lambda/gerar-codigo-pagamento
zip function.zip index.js
aws lambda create-function \
  --function-name gerar-codigo-pagamento \
  --runtime nodejs20.x \
  --handler index.handler \
  --zip-file fileb://function.zip \
  --role arn:aws:iam::ACCOUNT:role/lambda-veiculos-role
```

Conecte à fila SQS via event source mapping (igual ao LocalStack).

## 7. CloudWatch

Logs da API: App Runner envia automaticamente para CloudWatch Logs.
Logs da Lambda: `/aws/lambda/gerar-codigo-pagamento`.

## Estimativa de custo (cenário demonstração)

| Recurso | Custo estimado |
|---|---|
| App Runner (0.25 vCPU, pouco tráfego) | ~US$ 5–15/mês |
| RDS db.t3.micro (free tier 12 meses) | US$ 0 |
| SQS (< 1M requests) | ~US$ 0 |
| Lambda (< 1M invocações) | ~US$ 0 |
| Secrets Manager (1 segredo) | ~US$ 0.40/mês |
| **Total demonstração** | **~US$ 6–16/mês** |

> Desligue o App Runner e pare o RDS quando não estiver demonstrando para reduzir custo.

## O que rodou onde (evidência FASE 5)

| Componente | Local (LocalStack/Docker) | Nuvem real (App Runner) |
|---|---|---|
| API Spring Boot | docker-compose | App Runner |
| PostgreSQL | container postgres | RDS |
| SQS | LocalStack | Amazon SQS |
| Lambda | LocalStack | AWS Lambda |
| Secrets Manager | LocalStack | AWS Secrets Manager |
| CloudWatch | LocalStack logs | CloudWatch |
| IAM | LocalStack | IAM role App Runner |
