
package com.barbearia.agendamento.service;

import com.barbearia.agendamento.model.Barbeiro;
import com.barbearia.agendamento.repository.BarbeiroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import org.springframework.lang.NonNull;

@Service
public class BarbeiroService {

    @Autowired
    private BarbeiroRepository barbeiroRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Barbeiro salvar(Barbeiro barbeiro) {
        barbeiro.setSenha(passwordEncoder.encode(barbeiro.getSenha()));
        return barbeiroRepository.save(barbeiro);
    }

    public Optional<Barbeiro> buscarPorEmail(String email) {
        return barbeiroRepository.findByEmail(email);
    }

    public Optional<Barbeiro> buscarPorId(@NonNull Long id) {
        return barbeiroRepository.findById(id);
    }

    public List<Barbeiro> listarTodos() {
        return barbeiroRepository.findAll();
    }

    public void excluir(@NonNull Long id) {
        barbeiroRepository.deleteById(id);
    }

    public Barbeiro alternarAtivo(@NonNull Long id) {
        Barbeiro b = barbeiroRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Barbeiro não encontrado"));
        b.setAtivo(!b.isAtivo());
        return barbeiroRepository.save(b);
    }

}
