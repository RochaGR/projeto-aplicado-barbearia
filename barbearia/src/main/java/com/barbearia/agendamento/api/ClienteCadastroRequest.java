package com.barbearia.agendamento.api;

public record ClienteCadastroRequest(String nome, String telefone, String email, String senha) {
}
