package com.barbearia.agendamento.api;

import com.barbearia.agendamento.validation.SenhaValida;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClienteCadastroRequest(
		@NotBlank(message = "Nome é obrigatório")
		@Size(min = 3, message = "Nome deve ter no mínimo 3 caracteres")
		String nome,
		@NotBlank(message = "Telefone é obrigatório")
		String telefone,
		@NotBlank(message = "Email é obrigatório")
		@Email(message = "Informe um email válido")
		String email,
		@NotBlank(message = "Senha é obrigatória")
		@SenhaValida
		String senha) {
}
