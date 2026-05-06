package com.barbearia.agendamento.controller;

import com.barbearia.agendamento.model.CartaoFidelidade;
import com.barbearia.agendamento.model.Cliente;
import com.barbearia.agendamento.model.ConfiguracaoFidelidade;
import com.barbearia.agendamento.model.DescontoFidelidade;
import com.barbearia.agendamento.service.ClienteService;
import com.barbearia.agendamento.service.FidelidadeService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
@RequestMapping("/cliente/fidelidade")
public class FidelidadeController {

    private final FidelidadeService fidelidadeService;
    private final ClienteService clienteService;

    public FidelidadeController(FidelidadeService fidelidadeService, ClienteService clienteService) {
        this.fidelidadeService = fidelidadeService;
        this.clienteService = clienteService;
    }

    @GetMapping
    public String verCartao(Model model, Authentication authentication) {
        String email = getUsernameFromAuthentication(authentication);
        Cliente cliente = clienteService.buscarPorEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));

        ConfiguracaoFidelidade config = fidelidadeService.buscarConfiguracaoAtual();

        CartaoFidelidade cartao = fidelidadeService.buscarCartao(cliente.getId()).orElseGet(() -> {
            CartaoFidelidade novoCartao = new CartaoFidelidade();
            novoCartao.setCliente(cliente);
            novoCartao.setPontos(0);
            novoCartao.setTotalCortesRealizados(0);
            return novoCartao;
        });

        Optional<DescontoFidelidade> desconto = fidelidadeService.buscarDescontoDisponivel(cliente.getId());

        model.addAttribute("cartao", cartao);
        model.addAttribute("config", config);
        model.addAttribute("temDesconto", desconto.isPresent());
        desconto.ifPresent(d -> model.addAttribute("percentualDesconto", d.getPercentualDesconto()));

        return "fidelidade";
    }

    private String getUsernameFromAuthentication(Authentication authentication) {
        if (authentication == null) {
            throw new IllegalArgumentException("Usuário não autenticado");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof org.springframework.security.oauth2.core.user.OAuth2User oauth2User) {
            return (String) oauth2User.getAttributes().get("email");
        }
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        return authentication.getName();
    }
}
