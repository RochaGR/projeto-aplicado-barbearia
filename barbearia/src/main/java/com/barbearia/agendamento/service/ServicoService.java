package com.barbearia.agendamento.service;

import com.barbearia.agendamento.model.Servico;
import com.barbearia.agendamento.repository.ServicoRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ServicoService {

    private final ServicoRepository repository;

    public ServicoService(ServicoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Optional<Servico> buscarPorId(@NonNull Long id) {
        return repository.findById(id);
    }

    @Transactional
    public void cadastrar(Servico servico) {
        if (repository.existsByNome(servico.getNome())) {
            throw new IllegalArgumentException("Já existe um serviço com este nome");
        }
        repository.save(servico);
    }

    @Transactional(readOnly = true)
    public List<Servico> listarTodos() {
        return repository.findAllByOrderByNomeAsc();
    }

    @Transactional
    public void excluir(@NonNull Long id) {
        try {
            repository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException(
                    "Não é possível excluir: serviço vinculado a agendamentos");
        }
    }
}