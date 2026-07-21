package com.example.veiculo.geral.config.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;

@JsonInclude(Include.NON_NULL) // Só inclui os campos que não forem nulos
@Getter
@Builder                       // 
public class Problem {

	private Integer status;
	private String  type;
	private String  title;
	private String  detail;

	private String userMessage;
	private OffsetDateTime timestamp;
	private List<Field> fields;
	
	@Getter
	@Builder
	public static class Field {
		private String nome;
		private String userMessage;
	}
	
}
