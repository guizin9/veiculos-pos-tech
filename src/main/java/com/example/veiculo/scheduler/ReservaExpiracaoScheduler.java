package com.example.veiculo.scheduler;

import com.example.veiculo.service.PagamentoService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Dispara periodicamente a expiração de reservas cujo pagamento venceu.
 * A lógica de negócio fica em {@link PagamentoService#expirarVencidos()} para
 * poder ser reaproveitada por gatilhos externos (EventBridge / Lambda / Step Functions).
 */
@Component
@RequiredArgsConstructor
public class ReservaExpiracaoScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReservaExpiracaoScheduler.class);

    private final PagamentoService pagamentoService;

    @Scheduled(fixedDelayString = "${app.reserva.intervalo-verificacao-ms:60000}")
    public void verificarExpiracoes() {
        int quantidade = pagamentoService.expirarVencidos();
        if (quantidade > 0) {
            log.info("Expiração automática: {} reserva(s) expirada(s) e veículo(s) liberado(s).", quantidade);
        }
    }
}
