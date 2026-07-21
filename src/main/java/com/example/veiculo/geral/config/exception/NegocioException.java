package com.example.veiculo.geral.config.exception;

//@ResponseStatus(value = HttpStatus.BAD_REQUEST) // , reason = "Entidade não encontrada"
public class NegocioException extends RuntimeException { //ResponseStatusException {

	private static final long serialVersionUID = 1L;

	public NegocioException(String mensagem) {
		super(mensagem);	
	}

	public NegocioException(String mensagem, Throwable causa) {
		super(mensagem, causa);
	}		
	
	
//	public NegocioException(String entidade, Long id) {
//		super(String.format("Não existe cadastro de %s com código %d", entidade, id));
//	}		

//	public NegocioException(String entidade, Long id, Throwable causa) {
//		super(String.format("Não existe cadastro de %s com código %d", entidade, id), causa);
//	}		

}
