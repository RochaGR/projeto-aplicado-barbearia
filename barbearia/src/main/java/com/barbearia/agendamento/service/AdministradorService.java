package com.barbearia.agendamento.service;

import com.barbearia.agendamento.model.Administrador;
import com.barbearia.agendamento.repository.AdministradorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdministradorService {

    @Autowired
    private AdministradorRepository administradorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Administrador salvar(Administrador administrador) {
        administrador.setSenha(passwordEncoder.encode(administrador.getSenha()));
        return administradorRepository.save(administrador);
    }

    public Optional<Administrador> buscarPorEmail(String email) {
        return administradorRepository.findByEmail(email);
    }

    public Optional<Administrador> buscarPorId(Long id) {
        return administradorRepository.findById(id);
    }

    public List<Administrador> listarTodos() {
        return administradorRepository.findAll();
    }

    public boolean existeAlgumAdministrador() {
        return administradorRepository.count() > 0;
    }
}
