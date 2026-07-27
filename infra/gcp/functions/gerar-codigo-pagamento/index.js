/**
 * Cloud Function (Gen2) — processa eventos RESERVA_CRIADA do Pub/Sub.
 * Equivalente GCP da Lambda AWS em infra/lambda/gerar-codigo-pagamento.
 * A lógica principal permanece no monólito Spring Boot; esta função evidencia
 * o conceito serverless na trilha GCP (FASE 5).
 */
const functions = require('@google-cloud/functions-framework');

functions.cloudEvent('gerarCodigoPagamento', cloudEvent => {
  const message = cloudEvent.data?.message;
  if (!message?.data) {
    console.warn(JSON.stringify({ severity: 'WARNING', origem: 'gerar-codigo-pagamento', mensagem: 'Mensagem Pub/Sub sem payload' }));
    return;
  }

  try {
    const body = JSON.parse(Buffer.from(message.data, 'base64').toString('utf8'));
    const tipo = body.tipo || message.attributes?.tipo;

    if (tipo !== 'RESERVA_CRIADA') {
      console.log(JSON.stringify({
        severity: 'INFO',
        origem: 'gerar-codigo-pagamento',
        messageId: message.messageId,
        tipo,
        mensagem: 'Evento ignorado (apenas RESERVA_CRIADA é processado)'
      }));
      return;
    }

    const codigo = 'PAG-GCF-' + Math.random().toString(36).substring(2, 10).toUpperCase();

    // Log estruturado para Cloud Logging — sem PII (clienteId omitido, LGPD)
    console.log(JSON.stringify({
      severity: 'INFO',
      origem: 'gerar-codigo-pagamento',
      messageId: message.messageId,
      tipo,
      reservaId: body.reservaId,
      veiculoId: body.veiculoId,
      sagaId: body.metadata?.sagaId ?? body.reservaId,
      codigoAlternativo: codigo,
      mensagem: 'Código fictício gerado pela Cloud Function (demonstração FASE 5 GCP)'
    }));
  } catch (err) {
    console.error(JSON.stringify({
      severity: 'ERROR',
      origem: 'gerar-codigo-pagamento',
      messageId: message.messageId,
      erro: err.message
    }));
    throw err;
  }
});
