package com.barbearia.agendamento.api;

import com.barbearia.agendamento.model.Agendamento;
import com.barbearia.agendamento.service.AgendamentoService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/barbeiro/agendamentos")
public class BarbeiroAgendamentoRestController {

    private final AgendamentoService agendamentoService;

    public BarbeiroAgendamentoRestController(AgendamentoService agendamentoService) {
        this.agendamentoService = agendamentoService;
    }

    @GetMapping
    public Map<String, Object> listar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @AuthenticationPrincipal UserDetails user) {
        List<Agendamento> lista = agendamentoService.findByBarbeiroEmail(user.getUsername());
        if (data != null) {
            lista = lista.stream()
                    .filter(a -> a.getDataHora().toLocalDate().equals(data))
                    .collect(Collectors.toList());
        }
        return Map.of("agendamentos",
                lista.stream().map(ApiMapper::agendamento).collect(Collectors.toList()));
    }

    @PostMapping("/{id}/concluir")
    public ResponseEntity<?> concluir(@PathVariable Long id, @AuthenticationPrincipal UserDetails user) {
        try {
            Agendamento ag = agendamentoService.buscarPorId(id)
                    .orElseThrow(() -> new IllegalArgumentException("Agendamento não encontrado"));
            if (!ag.getBarbeiro().getEmail().equals(user.getUsername())) {
                return ResponseEntity.status(403).body(Map.of("message", "Sem permissão"));
            }
            ag.setStatus("CONCLUIDO");
            agendamentoService.salvar(ag);
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelar(@PathVariable Long id, @AuthenticationPrincipal UserDetails user) {
        try {
            Agendamento ag = agendamentoService.buscarPorId(id)
                    .orElseThrow(() -> new IllegalArgumentException("Agendamento não encontrado"));
            if (!ag.getBarbeiro().getEmail().equals(user.getUsername())) {
                return ResponseEntity.status(403).body(Map.of("message", "Sem permissão"));
            }
            ag.setStatus("CANCELADO");
            agendamentoService.salvar(ag);
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
