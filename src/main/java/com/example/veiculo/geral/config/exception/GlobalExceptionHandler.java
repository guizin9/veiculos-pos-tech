package com.example.veiculo.geral.config.exception;

import com.example.veiculo.geral.config.exception.dto.ErroCampoDto;
import com.example.veiculo.geral.config.exception.dto.ErroRespostaDto;
import com.example.veiculo.geral.config.exception.personal.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.lang.reflect.Field;
import java.nio.file.AccessDeniedException;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String MSG_ERRO_GENERICA_USUARIO_FINAL =
            "Ocorreu um erro interno inesperado no sistema. Tente novamente e se o problema persistir, entre em contato com o administrador do sistema.";
    @Autowired private MessageSource messageSource;

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ErroRespostaDto handleException(Exception e) {
        return ErroRespostaDto.conflito(e.getMessage());
    }

    @ExceptionHandler(RegistroDuplicadoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErroRespostaDto handleRegistroDuplicadoException(RegistroDuplicadoException e) {
        return ErroRespostaDto.conflito(e.getMessage());
    }

    @ExceptionHandler(RegistroSemIntegridadeException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErroRespostaDto handleRegistroDuplicadoException(RegistroSemIntegridadeException e) {
        ///return ErroRespostaDto.conflito(e.getMessage());
        return new ErroRespostaDto(HttpStatus.CONFLICT.value(), e.getMessage(), List.of());
    }

    @ExceptionHandler(OperacaoNaoPemitidaExecption.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroRespostaDto handleOperacaoNaoPemitidaExecption(OperacaoNaoPemitidaExecption e) {
        return ErroRespostaDto.respostaPadrao(e.getMessage());
    }

    @ExceptionHandler(CampoInvalidoException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroRespostaDto handlerCampoInvalidoException(CampoInvalidoException e) {
        return new ErroRespostaDto(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Erro de validação", List.of(new ErroCampoDto(e.getCampo(), e.getMessage())));
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErroRespostaDto handlerAccessDeniedException(AccessDeniedException e) {
        return new ErroRespostaDto(HttpStatus.FORBIDDEN.value(), "Acesso negado (1)", List.of());
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErroRespostaDto handlerAuthorizationDeniedException(AuthorizationDeniedException e) {
        return new ErroRespostaDto(HttpStatus.FORBIDDEN.value(), "Acesso negado (2)", List.of());
    }

    @ExceptionHandler(RegistroNaoEncontradoException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroRespostaDto handleRegistroNaoEncontradoException(RegistroNaoEncontradoException e) {
        return ErroRespostaDto.conflito(e.getMessage());
    }

    @ExceptionHandler(RegistroNaoEnviadoException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroRespostaDto handleRegistroNaoEnviadoException(RegistroNaoEnviadoException e) {
        return ErroRespostaDto.conflito(e.getMessage());
    }
    //return ErroRespostaDto.respostaPadrao(e.getMessage());

    @ExceptionHandler(ErroGeralException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErroRespostaDto handleRegistroDuplicadoException(ErroGeralException e) {
        return ErroRespostaDto.conflito(e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErroRespostaDto handRuntimeException(RuntimeException e) { // Erro não tratado
        return new ErroRespostaDto(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Ocorreu um erro inesperado, entre em contato com a administração do sistema", List.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrityViolation(DataIntegrityViolationException ex, WebRequest request) {

        String mensagemUsuario = "Ocorreu um erro ao processar a operação. Verifique os dados informados.";

        // Mensagem mais técnica (log)
        String mensagemDesenvolvedor = ex.getRootCause() != null ? ex.getRootCause().getMessage() : ex.getMessage();

        mensagemUsuario += ". " + mensagemDesenvolvedor.split("\n")[0];

        Problem problem = createProblemBuilder(HttpStatus.BAD_REQUEST,
                ProblemType.VIOLACAO_DE_CONSTRAINT,
                mensagemUsuario)
                .userMessage(mensagemUsuario)
              //  .developerMessage(mensagemDesenvolvedor)
    //.fields(problemField)
                .build();

        ex.printStackTrace(); // loga no console

        return handleExceptionInternal(ex, problem, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String detail = "Um ou mais campos estão inválidos. Faça o preenchimento correto e tente novamente.";
        System.out.println(detail);
        BindingResult bindingResult = ex.getBindingResult();
        Object target = bindingResult.getTarget(); // Deve ser EnderecoDtoEntrada
        List<Problem.Field> problemField = bindingResult.getFieldErrors().stream()
                //.sorted(Comparator.comparing(FieldError::getField))
                .sorted(Comparator.comparing(fieldError ->
                                messageSource.getMessage(
                                        fieldError,
                                        LocaleContextHolder.getLocale()
                                ),
                        String.CASE_INSENSITIVE_ORDER
                ))
                .map(fieldError -> {
                    String message = messageSource.getMessage(fieldError, LocaleContextHolder.getLocale());
                    String finalMessage = message;

                    try {
                        // Tenta acessar diretamente o campo "ordem" do objeto alvo
                        Field ordemField = target.getClass().getDeclaredField("ordem");
                        ordemField.setAccessible(true);
                        Object ordemValue = ordemField.get(target);

                        if (ordemValue != null) {
                            finalMessage = message.replace("{ordem}", ordemValue + "º");
                        }
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        // Ignora e usa mensagem padrão
                    }

                    return Problem.Field.builder()
                            .nome(fieldError.getField()) // continua sendo "logradouro"
                            .userMessage(finalMessage)
                            .build();
                })
                .collect(Collectors.toList());

//                    return Problem.Field.builder()
//                            .nome(fieldError.getField())
//                            .userMessage(finalMessage)
//                            .build();


        Problem problem = createProblemBuilder(HttpStatus.valueOf(status.value()),
                ProblemType.DADOS_INVALIDO,
                detail)
                .userMessage(detail)
                .fields(problemField)
                .build();
        return handleExceptionInternal(ex, problem, new HttpHeaders(), HttpStatus.valueOf(status.value()), request);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
                                                             HttpStatusCode statusCode, WebRequest request) {
        if (body == null) {
            body = Problem.builder()
                    .timestamp(OffsetDateTime.now())
                    .title(ex.getMessage())
                    .status(statusCode.value())
                    .userMessage(MSG_ERRO_GENERICA_USUARIO_FINAL)
                    .build();
        }
        else if (body instanceof String) {
            body = Problem.builder()
                    .timestamp(OffsetDateTime.now())
                    .title((String) body)
                    .status(statusCode.value())
                    .userMessage(MSG_ERRO_GENERICA_USUARIO_FINAL)
                    .build();
        }

        return super.handleExceptionInternal(ex, body, headers, statusCode, request);
    }

    private Problem.ProblemBuilder createProblemBuilder(HttpStatus status, ProblemType problemType, String detail) {
        return Problem.builder()
                .timestamp(OffsetDateTime.now())
                .title(problemType.getTitle())
                .status(status.value())
                .detail(detail)
                .type(problemType.getUri());
    }
}
