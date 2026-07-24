package com.example.veiculo.saga;

/**
 * Etapas da SAGA de compra (orquestração interna no monólito).
 */
public enum SagaEtapa {
    RESERVA,
    PAGAMENTO_GERADO,
    PAGAMENTO_CONFIRMADO,
    VENDA,
    DOCUMENTACAO,
    RETIRADA,
    CANCELADA,
    EXPIRADA
}
