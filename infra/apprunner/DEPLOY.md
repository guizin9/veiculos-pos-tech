# Deploy na AWS — App Runner (LEGADO)

> **⚠️ Descontinuado para novos clientes** a partir de **30/04/2026**.  
> A AWS não aceita mais criação de novos serviços App Runner.  
> **Use o guia atual:** [`../elasticbeanstalk/DEPLOY.md`](../elasticbeanstalk/DEPLOY.md)  
> Alternativa oficial AWS: **Amazon ECS Express Mode**.

---

O conteúdo abaixo permanece apenas como referência histórica do projeto.

## Por que App Runner (legado)

| Critério | App Runner | ECS Fargate |
|---|---|---|
| Complexidade | Baixa | Alta |
| Ideal para | Monólito Spring Boot | Controle fino de rede |

## Imagem ECR (já publicada)

```
637423599009.dkr.ecr.us-east-1.amazonaws.com/veiculos-pos-tech:latest
```

Para novo deploy, siga [`../elasticbeanstalk/DEPLOY.md`](../elasticbeanstalk/DEPLOY.md).
