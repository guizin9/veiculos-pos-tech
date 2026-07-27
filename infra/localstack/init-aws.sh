#!/bin/bash
# Inicialização dos recursos AWS simulados no LocalStack (SQS, Secrets Manager, IAM, Lambda)
set -euo pipefail

echo "[init-aws] Criando fila SQS..."
awslocal sqs create-queue --queue-name veiculos-eventos

QUEUE_URL=$(awslocal sqs get-queue-url --queue-name veiculos-eventos --query 'QueueUrl' --output text)
QUEUE_ARN=$(awslocal sqs get-queue-attributes --queue-url "$QUEUE_URL" --attribute-names QueueArn --query 'Attributes.QueueArn' --output text)

echo "[init-aws] Criando segredo no Secrets Manager..."
awslocal secretsmanager create-secret \
  --name veiculos/app \
  --secret-string '{"username":"postgres","password":"postgres","jwtSecret":"troque-este-segredo-para-um-valor-forte-com-32+chars"}' \
  || echo "[init-aws] Segredo veiculos/app já existe"

echo "[init-aws] Criando role IAM para Lambda..."
awslocal iam create-role \
  --role-name lambda-veiculos-role \
  --assume-role-policy-document '{"Version":"2012-10-17","Statement":[{"Effect":"Allow","Principal":{"Service":"lambda.amazonaws.com"},"Action":"sts:AssumeRole"}]}' \
  || echo "[init-aws] Role já existe"

echo "[init-aws] Empacotando e criando Lambda gerar-codigo-pagamento..."
cd /etc/localstack/init/ready.d/lambda
zip -j /tmp/gerar-codigo-pagamento.zip index.js

awslocal lambda create-function \
  --function-name gerar-codigo-pagamento \
  --runtime nodejs20.x \
  --handler index.handler \
  --zip-file fileb:///tmp/gerar-codigo-pagamento.zip \
  --role arn:aws:iam::000000000000:role/lambda-veiculos-role \
  || awslocal lambda update-function-code \
       --function-name gerar-codigo-pagamento \
       --zip-file fileb:///tmp/gerar-codigo-pagamento.zip

echo "[init-aws] Conectando Lambda à fila SQS (event source mapping)..."
awslocal lambda create-event-source-mapping \
  --function-name gerar-codigo-pagamento \
  --event-source-arn "$QUEUE_ARN" \
  --batch-size 1 \
  || echo "[init-aws] Event source mapping já existe"

echo "[init-aws] Recursos AWS (LocalStack) prontos. Fila: $QUEUE_URL"
