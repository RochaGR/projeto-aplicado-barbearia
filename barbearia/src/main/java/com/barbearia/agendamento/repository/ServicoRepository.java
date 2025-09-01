package com.barbearia.agendamento.repository;

import com.barbearia.agendamento.model.Servico;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ServicoRepository extends JpaRepository<Servico, Long> {
    boolean existsByNome(String nome);

    List<Servico> findAllByOrderByNomeAsc();
}