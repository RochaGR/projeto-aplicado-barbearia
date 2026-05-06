package com.barbearia.agendamento.config;

import com.barbearia.agendamento.model.AuthProvider;
import com.barbearia.agendamento.model.Cliente;
import com.barbearia.agendamento.repository.ClienteRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;


@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final ClienteRepository clienteRepository;

    public CustomOAuth2UserService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String googleId = (String) attributes.get("sub");

        Optional<Cliente> clienteOpt = clienteRepository.findByEmail(email);
        Cliente cliente;

        if (clienteOpt.isPresent()) {
            cliente = clienteOpt.get();
            // Se o cliente existia com login local, vincula também ao Google
            if (cliente.getGoogleId() == null) {
                cliente.setGoogleId(googleId);
                clienteRepository.save(cliente);
            }
        } else {
            // Cria um novo cliente a partir dos dados do Google
            cliente = new Cliente();
            cliente.setNome(name);
            cliente.setEmail(email);
            cliente.setGoogleId(googleId);
            cliente.setAuthProvider(AuthProvider.GOOGLE);
            cliente.setAtivo(true);
            cliente.setSenha(java.util.UUID.randomUUID().toString());
            // Telefone fica null mas será pedido na tela de completar cadastro
            clienteRepository.save(cliente);
        }

        // Retorna um OAuth2User com a role CLIENTE
        return new DefaultOAuth2User(
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_CLIENTE")),
                attributes,
                "email"
        );
    }
}
