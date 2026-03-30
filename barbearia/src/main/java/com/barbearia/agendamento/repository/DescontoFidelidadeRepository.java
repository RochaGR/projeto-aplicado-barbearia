package com.barbearia.agendamento.repository;

import com.barbearia.agendamento.model.DescontoFidelidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DescontoFidelidadeRepository extends JpaRepository<DescontoFidelidade, Long> {
    Optional<DescontoFidelidade> findByClienteIdAndUtilizadoFalse(Long clienteId);

    List<DescontoFidelidade> findByClienteId(Long clienteId);

    @Query("SELECT COALESCE(SUM(d.valorEconomizado), 0) FROM DescontoFidelidade d " +
            "WHERE d.cliente.id = :clienteId AND d.utilizado = true AND d.valorEconomizado IS NOT NULL")
    Double somarEconomiaByClienteId(@Param("clienteId") Long clienteId);
}
