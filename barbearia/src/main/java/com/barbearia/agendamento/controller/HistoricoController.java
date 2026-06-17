package com.barbearia.agendamento.controller;

import com.barbearia.agendamento.model.Agendamento;
import com.barbearia.agendamento.model.Cliente;
import com.barbearia.agendamento.repository.DescontoFidelidadeRepository;
import com.barbearia.agendamento.service.AgendamentoService;
import com.barbearia.agendamento.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/cliente")
public class HistoricoController {

    @Autowired
    private AgendamentoService agendamentoService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private DescontoFidelidadeRepository descontoFidelidadeRepository;

    @GetMapping("/historico")
    public String historico(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) String status,
            Model model,
            Authentication authentication) {

        String email = getUsernameFromAuthentication(authentication);
        Cliente cliente = clienteService.buscarPorEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));

        // Buscar todos os agendamentos do cliente
        List<Agendamento> historico = agendamentoService.listarPorCliente(cliente.getId());

        // Aplicar filtros
        if (dataInicio != null) {
            historico = historico.stream()
                    .filter(a -> !a.getDataHora().toLocalDate().isBefore(dataInicio))
                    .collect(Collectors.toList());
        }

        if (dataFim != null) {
            historico = historico.stream()
                    .filter(a -> !a.getDataHora().toLocalDate().isAfter(dataFim))
                    .collect(Collectors.toList());
        }

        if (status != null && !status.isEmpty()) {
            historico = historico.stream()
                    .filter(a -> a.getStatus().equalsIgnoreCase(status))
                    .collect(Collectors.toList());
        }

        // Ordenar por data decrescente (mais recente primeiro)
        historico = historico.stream()
                .sorted((a1, a2) -> a2.getDataHora().compareTo(a1.getDataHora()))
                .collect(Collectors.toList());

        // Estatisticas do cliente
        long totalAgendamentos = historico.size();
        long concluidos = historico.stream()
                .filter(a -> a.getStatus().equals("CONCLUIDO"))
                .count();
        long cancelados = historico.stream()
                .filter(a -> a.getStatus().equals("CANCELADO"))
                .count();

        Double totalEconomizado = descontoFidelidadeRepository.somarEconomiaByClienteId(cliente.getId());

        model.addAttribute("historico", historico);
        model.addAttribute("totalAgendamentos", totalAgendamentos);
        model.addAttribute("concluidos", concluidos);
        model.addAttribute("cancelados", cancelados);
        model.addAttribute("totalEconomizado", totalEconomizado != null ? totalEconomizado : 0.0);
        model.addAttribute("dataInicio", dataInicio);
        model.addAttribute("dataFim", dataFim);
        model.addAttribute("statusFiltro", status);

        return "historico";
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