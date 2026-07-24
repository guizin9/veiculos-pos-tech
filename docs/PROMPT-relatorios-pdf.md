# Prompt para outra IA gerar os relatorios do PDF (FASE 5)

Este prompt e **autossuficiente**: contem todo o contexto do projeto, porque a IA que vai receber **nao conhece o sistema**. Basta copiar tudo abaixo da linha e colar na IA.

---

Voce e um(a) arquiteto(a) de solucoes em nuvem e especialista em seguranca de dados. Preciso que voce escreva o **conteudo de um PDF** (documento tecnico, em portugues do Brasil, pronto para entrega academica) com **tres relatorios** sobre o projeto descrito abaixo. Voce NAO tem acesso ao codigo; use apenas o contexto fornecido aqui.

## Contexto do projeto (leia com atencao)

**O que e:** uma **API REST** para uma **empresa de revenda de veiculos automotores** que quer operar uma plataforma na internet. O time de frontend/UX consome a API. A API permite cadastrar veiculos para venda, cadastrar compradores e conduzir todo o processo de compra, da selecao do veiculo ate a retirada.

**Stack tecnica atual:**
- Linguagem: Java 21.
- Framework: Spring Boot 4.1 (Web MVC, Data JPA, Spring Security, Bean Validation).
- Banco de dados: PostgreSQL (atualmente uma instancia unica; Hibernate com `ddl-auto: update`).
- Build: Maven. Aplicacao roda como um monolito (JAR Spring Boot), hoje na porta 8083.
- Arquitetura em camadas: Controller -> Validador de Dados -> Service (transacional) -> Validador de Negocio -> Repository -> Entidades JPA. Tratamento de erros centralizado.

**Dados armazenados (entidades e campos):**
- `Marca`: id, nome (unico). Dado de catalogo/referencia.
- `Modelo`: id, nome (unico), pertence a uma Marca. Catalogo.
- `Versao`: id, nome (unico), pertence a um Modelo. Catalogo.
- `Cor`: id, nome (unico). Catalogo.
- `Veiculo`: id, status (A=a venda, R=reservado, V=vendido, D=desativado), anoFabricacao, anoModelo, chassi, valor, versao, cor. Dado operacional (nao pessoal).
- `Cliente` (o comprador): id, nome, **cpf**, endereco completo (logradouro, numero, complemento, bairro, cidade, estado, cep), **celular**, **foneFixo**, **email**. Contem dados pessoais.
- `ReservaVendaVeiculo` (a transacao de compra): id, status (R=reservado, V=vendido, C=cancelado), valor, cliente, veiculo, dtReserva, dtVenda, dtCancelamento, retirado (S/N), dtRetirada. Torna-se dado pessoal por associacao (liga um comprador a uma compra).
- Sera necessario tambem um **codigo de pagamento** (dado financeiro) e os dados para **emitir a documentacao do veiculo** na retirada.

**Processo de compra (fluxo de negocio):** o cliente seleciona o veiculo, **reserva** o automovel, recebe um **codigo de pagamento**, **paga** e **retira** o veiculo (baixa no estoque). Problemas possiveis: outro cliente reserva o veiculo antes; o pagamento nao e efetuado; o cliente desiste em qualquer etapa. Em qualquer falha, a reserva pode ser **cancelada** e o veiculo volta a ficar disponivel (status A). A venda so pode ocorrer para **compradores previamente cadastrados**.

**Estado atual de seguranca (pontos ja conhecidos, a serem tratados nos relatorios):**
- A configuracao do Spring Security esta permissiva (`anyRequest().permitAll()`), ou seja, a API esta aberta, sem autenticacao/autorizacao efetiva.
- Dados pessoais (CPF, endereco, contatos) sao armazenados em texto plano, sem criptografia ou mascaramento.
- Credenciais de banco estao no arquivo de configuracao (`application.yml`).
- Ainda nao ha geracao de codigo de pagamento nem expiracao/timeout de reserva.
- O identificador unico do cliente hoje e o `nome` (o CPF nao e unico nem validado).

**Requisito legal:** a solucao trata dados pessoais e deve estar aderente a **LGPD** (Lei Geral de Protecao de Dados do Brasil).

## O que voce deve produzir (3 relatorios)

Escreva de forma tecnica, objetiva e bem estruturada, com titulos, subtitulos e **tabelas** onde fizer sentido. Sempre que citar um servico ou decisao, **justifique**. Use um provedor de nuvem principal (recomendo **AWS**) e, se possivel, cite equivalentes em Azure e GCP.

### Relatorio 1 - Desenho da arquitetura em nuvem
- Proponha uma arquitetura para rodar a solucao na nuvem, **priorizando servicos serverless e/ou gerenciados**.
- Descreva cada camada: hospedagem do frontend, borda/entrada, autenticacao, aplicacao (a API Spring Boot), orquestracao do processo de compra, mensageria, banco de dados, segredos e chaves.
- **Justifique a escolha de cada servico** (por que ele, e nao alternativas). Ex.: por que containers gerenciados vs. funcoes vs. maquinas virtuais para uma aplicacao JVM.
- Inclua uma secao dedicada aos **servicos de seguranca da nuvem** utilizados (ex.: WAF, gestao de identidade, gestao de chaves/KMS, gestao de segredos, auditoria/logs, deteccao de ameacas, isolamento de rede/VPC) e **justifique o uso de cada um**.
- Entregue tambem um **diagrama da arquitetura em Mermaid** (flowchart), com os componentes e as ligacoes entre eles.

### Relatorio 2 - Relatorio de seguranca de dados
Cubra obrigatoriamente:
- **Quais dados sao armazenados** pela solucao (liste por entidade, classificando cada grupo).
- **Quais sao os dados sensiveis** (sob a otica da LGPD) e por que (CPF, endereco, contatos, codigo/dados de pagamento).
- **Politicas de acesso a dados** implementadas/recomendadas (autenticacao, autorizacao por papel/RBAC, principio do menor privilegio, mascaramento na saida, regra de "venda so para cadastrados", segregacao de rede).
- **Politicas de seguranca da operacao** (criptografia em transito e em repouso, gestao de segredos, minimizacao e finalidade, retencao e descarte/anonimizacao, auditoria/rastreabilidade, backup/recuperacao, resposta a incidentes).
- **Riscos e acoes de mitigacao**: monte uma tabela com risco, impacto e mitigacao (inclua ao menos: API aberta, vazamento de CPF/endereco, injecao SQL/XSS, credenciais no codigo, reserva concorrente, pagamento nao efetuado/desistencia, perda de dados, acesso indevido interno, negacao de servico, retencao excessiva).

### Relatorio 3 - Relatorio de orquestracao SAGA
- Explique brevemente **por que o padrao SAGA** e necessario neste processo de compra distribuido (varios passos: reserva -> codigo de pagamento -> pagamento -> venda -> retirada, com falhas possiveis).
- Compare os dois estilos: **coreografia** vs **orquestracao**.
- **Recomende um tipo** (orquestracao ou coreografia) e **justifique** a escolha para este caso especifico.
- Descreva os **passos da SAGA** e suas **transacoes de compensacao** (ex.: liberar veiculo, cancelar reserva, expirar codigo de pagamento) numa tabela.
- Entregue um **diagrama Mermaid** (stateDiagram) do fluxo da SAGA com os caminhos de sucesso e de compensacao.
- Comente garantias importantes: idempotencia, consistencia eventual e "nunca deixar estoque preso".

## Formato de saida
- Portugues do Brasil, tom tecnico-academico.
- Estruture com secoes numeradas (1, 2, 3) e subsecoes.
- Use tabelas para servicos/justificativas, dados/classificacao e riscos/mitigacao.
- Inclua os diagramas Mermaid pedidos (arquitetura e SAGA) em blocos de codigo.
- Ao final, um pequeno checklist confirmando que os tres relatorios e os requisitos (serverless/gerenciados, justificativas, servicos de seguranca) foram atendidos.

---

## Observacao (para voce, dono do projeto)

Este prompt cobre exatamente os itens do enunciado da FASE 5. Se preferir outro provedor de nuvem (Azure/GCP) como principal, troque "AWS" pelo desejado na secao "O que voce deve produzir". O conteudo gerado pode ser comparado/mesclado com o que ja existe em [FASE5-Entregaveis.md](FASE5-Entregaveis.md).
