package com.barbearia.agendamento.repository;

import com.barbearia.agendamento.model.Feriado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface FeriadoRepository extends JpaRepository<Feriado, Long> {
    Optional<Feriado> findByData(LocalDate data);
    boolean existsByData(LocalDate data);
}
