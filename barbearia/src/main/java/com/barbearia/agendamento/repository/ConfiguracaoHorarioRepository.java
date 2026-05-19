package com.barbearia.agendamento.repository;

import com.barbearia.agendamento.model.ConfiguracaoHorario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConfiguracaoHorarioRepository extends JpaRepository<ConfiguracaoHorario, Long> {
    Optional<ConfiguracaoHorario> findByDiaSemana(Integer diaSemana);
}
