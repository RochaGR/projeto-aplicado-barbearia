package com.barbearia.agendamento.repository;

import com.barbearia.agendamento.model.CartaoFidelidade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartaoFidelidadeRepository extends JpaRepository<CartaoFidelidade, Long> {
    Optional<CartaoFidelidade> findByClienteId(Long clienteId);
}

