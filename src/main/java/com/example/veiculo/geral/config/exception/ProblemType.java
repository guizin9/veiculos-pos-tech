package com.example.veiculo.geral.config.exception;

import lombok.Getter;

@Getter
public enum ProblemType {

	DADOS_INVALIDO("dados-invalido", "Dados inválido"),
	ERRO_DE_SISTEMA("erro-de-sistema", "Erro de Sistema"),
	PARAMETRO_INVALIDO("parametro-invalido", "Parâmetro inválido"),
	MENSAGEM_INCOMPREESIVEL("mensagem-incompreensivel", "Mensagem incompreensível"),
	RECURSO_NAO_ENCONTRADO("recurso-nao-encontrada", "Recurso não encontrada"),
	ENTIDADE_EM_USO("entidade-em-uso", "Entidade em uso"),
	ERRO_NOGOCIO("erro-negocio", "Violação de regra de negócio"),
	RECURSO_JA_EXISTENTE("recurso-ja-existente", "Recurso já cadastradao"),
	RECURSO_JA_ALTERADO("recurso-ja-alterado", "Recurso já alterado por outro usuário"),
	VIOLACAO_DE_CONSTRAINT("violação-de-constraint", "Recurso viola regra de integridade");
	
	private String title;
	private String uri;
	
	private ProblemType(String path, String title) {
		this.title = title;
		this.uri = "https://admi.com.br/" + path;
	}		
}
