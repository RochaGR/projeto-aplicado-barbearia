package com.barbearia.agendamento.repository;

import com.barbearia.agendamento.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT c FROM Cliente c WHERE " +
            "LOWER(c.nome) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
            "LOWER(c.email) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
            "c.telefone LIKE CONCAT('%', :termo, '%')")
    List<Cliente> pesquisarClientes(@Param("termo") String termo);
}
