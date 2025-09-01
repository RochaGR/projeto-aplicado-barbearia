
package com.barbearia.agendamento.repository;

import com.barbearia.agendamento.model.Barbeiro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BarbeiroRepository extends JpaRepository<Barbeiro, Long> {
    Optional<Barbeiro> findByEmail(String email);
}
