package com.barbearia.agendamento.repository;

import com.barbearia.agendamento.model.Agendamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import java.time.LocalDateTime;
import java.util.List;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

        @Query("SELECT a FROM Agendamento a WHERE a.barbeiro.email = :email")
        List<Agendamento> findByBarbeiroEmail(@Param("email") String email);

       ;

        @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END " +
                        "FROM Agendamento a WHERE a.barbeiro.id = :barbeiroId " +
                        "AND a.dataHora = :dataHora " +
                        "AND a.status <> :status")
        boolean existsByBarbeiroIdAndDataHoraAndStatusNot(
                        @Param("barbeiroId") Long barbeiroId,
                        @Param("dataHora") LocalDateTime dataHora,
                        @Param("status") String status);

        @Query("SELECT a FROM Agendamento a WHERE a.barbeiro.id = :barbeiroId " +
                        "AND a.dataHora BETWEEN :inicio AND :fim " +
                        "AND a.status <> 'CANCELADO'")
        List<Agendamento> findByBarbeiroIdAndPeriodo(
                        @Param("barbeiroId") Long barbeiroId,
                        @Param("inicio") LocalDateTime inicio,
                        @Param("fim") LocalDateTime fim);

        @Query("SELECT a FROM Agendamento a WHERE a.cliente.id = :clienteId")
        List<Agendamento> findByClienteId(@Param("clienteId") Long clienteId);

        List<Agendamento> findByBarbeiroId(Long barbeiroId);

        @NonNull
        Page<Agendamento> findAll(@NonNull Pageable pageable);

        Page<Agendamento> findByDataHoraBetween(LocalDateTime inicio, LocalDateTime fim, Pageable pageable);

        Page<Agendamento> findByStatus(String status, Pageable pageable);

        Page<Agendamento> findByDataHoraBetweenAndStatus(
                        LocalDateTime inicio,
                        LocalDateTime fim,
                        String status,
                        Pageable pageable);

        @Query("SELECT a FROM Agendamento a WHERE a.dataHora < :dataHora AND a.status IN ('AGENDADO')")
        List<Agendamento> findPassadosNaoCancelados(@Param("dataHora") LocalDateTime dataHora);

        @Query("SELECT a FROM Agendamento a WHERE a.dataHora >= :inicio AND a.dataHora < :fim AND a.status IN ('AGENDADO')")
        List<Agendamento> findByDataBetweenAndStatusAtivo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);
}