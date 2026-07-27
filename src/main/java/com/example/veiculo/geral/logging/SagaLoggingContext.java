package com.example.veiculo.geral.logging;

import org.slf4j.MDC;

/**
 * Contexto de observabilidade da SAGA via MDC (propagado nos logs estruturados).
 * Não inclui dados pessoais — apenas IDs operacionais.
 */
public final class SagaLoggingContext {

    public static final String CORRELATION_ID = "correlationId";
    public static final String SAGA_ID = "sagaId";
    public static final String CORRELATION_HEADER = "X-Correlation-Id";

    private SagaLoggingContext() {
    }

    public static void bindReserva(Long reservaId) {
        if (reservaId != null) {
            MDC.put(SAGA_ID, String.valueOf(reservaId));
        }
    }

    public static void bindCorrelationId(String correlationId) {
        if (correlationId != null && !correlationId.isBlank()) {
            MDC.put(CORRELATION_ID, correlationId);
        }
    }

    public static void clearSaga() {
        MDC.remove(SAGA_ID);
    }

    public static void clearCorrelation() {
        MDC.remove(CORRELATION_ID);
    }
}
