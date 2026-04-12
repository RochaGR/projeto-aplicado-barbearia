package com.barbearia.agendamento.api;

public record AgendamentoFormRequest(Long barbeiroId, Long servicoId, String dataHora) {
}
