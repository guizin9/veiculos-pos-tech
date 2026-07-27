/**
 * Lambda de demonstração — processa eventos RESERVA_CRIADA da fila SQS.
 * Gera um código de pagamento alternativo fictício e registra no CloudWatch Logs.
 * A lógica principal permanece no monólito Spring Boot; esta Lambda evidencia
 * o conceito serverless exigido na FASE 5 (LocalStack / AWS).
 */
exports.handler = async (event) => {
  for (const record of event.Records) {
    try {
      const body = JSON.parse(record.body);
      if (body.tipo === 'RESERVA_CRIADA') {
        const codigo = 'PAG-LMB-' + Math.random().toString(36).substring(2, 10).toUpperCase();
        console.log(JSON.stringify({
          origem: 'lambda-gerar-codigo-pagamento',
          reservaId: body.reservaId,
          clienteId: body.clienteId,
          veiculoId: body.veiculoId,
          codigoAlternativo: codigo,
          mensagem: 'Código fictício gerado pela Lambda (demonstração FASE 5)'
        }));
      }
    } catch (err) {
      console.error('Erro ao processar registro SQS:', err.message);
    }
  }
  return { statusCode: 200, body: 'OK' };
};
