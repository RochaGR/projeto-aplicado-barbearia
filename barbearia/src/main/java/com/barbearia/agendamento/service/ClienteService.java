package com.barbearia.agendamento.service;

import com.barbearia.agendamento.model.Cliente;
import com.barbearia.agendamento.repository.ClienteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    private final ClienteRepository repository;
    private final PasswordEncoder passwordEncoder;

    public ClienteService(ClienteRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Cliente cadastrarCliente(Cliente cliente) {
        validarEmailExistente(cliente.getEmail());
        cliente.setSenha(passwordEncoder.encode(cliente.getSenha()));
        return repository.save(cliente);
    }

    @Transactional(readOnly = true)
    public Optional<Cliente> buscarPorId(Long id) {
        return repository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Cliente> buscarPorEmail(String email) {
        return repository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public List<Cliente> listarTodosClientes() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Cliente> listarClientes(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Transactional
    public Cliente atualizarCliente(Long id, Cliente clienteAtualizado) {
        Cliente clienteExistente = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));

        if (!clienteExistente.getEmail().equals(clienteAtualizado.getEmail()) &&
                repository.existsByEmail(clienteAtualizado.getEmail())) {
            throw new IllegalArgumentException("Email já está em uso");
        }

        clienteExistente.setNome(clienteAtualizado.getNome());
        clienteExistente.setTelefone(clienteAtualizado.getTelefone());
        clienteExistente.setEmail(clienteAtualizado.getEmail());

        if (clienteAtualizado.getSenha() != null && !clienteAtualizado.getSenha().isEmpty()) {
            clienteExistente.setSenha(passwordEncoder.encode(clienteAtualizado.getSenha()));
        }

        return repository.save(clienteExistente);
    }

    @Transactional
    public void excluirCliente(Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Cliente não encontrado");
        }
        repository.deleteById(id);
    }

    private void validarEmailExistente(String email) {
        if (repository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email já cadastrado");
        }
    }

    @Transactional(readOnly = true)
    public List<Cliente> pesquisarClientes(String termo) {
        if (termo == null || termo.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return repository.pesquisarClientes(termo.toLowerCase());
    }
}
