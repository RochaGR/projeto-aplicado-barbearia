
package com.barbearia.agendamento.service;

import com.barbearia.agendamento.model.Administrador;
import com.barbearia.agendamento.model.Barbeiro;
import com.barbearia.agendamento.model.Cliente;
import com.barbearia.agendamento.repository.AdministradorRepository;
import com.barbearia.agendamento.repository.BarbeiroRepository;
import com.barbearia.agendamento.repository.ClienteRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final ClienteRepository clienteRepository;
    private final AdministradorRepository administradorRepository;
    private final BarbeiroRepository barbeiroRepository;

    public UserDetailsServiceImpl(
            ClienteRepository clienteRepository,
            AdministradorRepository administradorRepository,
            BarbeiroRepository barbeiroRepository) {
        this.clienteRepository = clienteRepository;
        this.administradorRepository = administradorRepository;
        this.barbeiroRepository = barbeiroRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Verifica se é Administrador
        Administrador admin = administradorRepository.findByEmail(email).orElse(null);
        if (admin != null) {
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            return new User(admin.getEmail(), admin.getSenha(), authorities);
        }

        // Verifica se é Barbeiro
        Barbeiro barbeiro = barbeiroRepository.findByEmail(email).orElse(null);
        if (barbeiro != null) {
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_BARBEIRO"));
            return new User(barbeiro.getEmail(), barbeiro.getSenha(), authorities);
        }

        // Verifica se é Cliente
        Cliente cliente = clienteRepository.findByEmail(email).orElse(null);
        if (cliente != null) {
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_CLIENTE"));
            // Clientes OAuth2 podem ter senha nula
            String senha = cliente.getSenha() != null ? cliente.getSenha() : "";
            return new User(cliente.getEmail(), senha, authorities);
        }

        // Nenhum usuário encontrado
        throw new UsernameNotFoundException("Usuário não encontrado: " + email);
    }
}
