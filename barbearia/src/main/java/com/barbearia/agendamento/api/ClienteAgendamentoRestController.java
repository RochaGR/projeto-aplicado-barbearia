package com.barbearia.agendamento.api;

import com.barbearia.agendamento.model.Agendamento;
import com.barbearia.agendamento.model.Barbeiro;
import com.barbearia.agendamento.model.Cliente;
import com.barbearia.agendamento.model.ConfiguracaoFidelidade;
import com.barbearia.agendamento.model.Servico;
import com.barbearia.agendamento.service.AgendamentoService;
import com.barbearia.agendamento.service.BarbeiroService;
import com.barbearia.agendamento.service.ClienteService;
import com.barbearia.agendamento.service.FidelidadeService;
import com.barbearia.agendamento.service.ServicoService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cliente")
public class ClienteAgendamentoRestController {

    private final AgendamentoService agendamentoService;
    private final ClienteService clienteService;
    private final BarbeiroService barbeiroService;
    private final ServicoService servicoService;
    private final FidelidadeService fidelidadeService;

    public ClienteAgendamentoRestController(AgendamentoService agendamentoService,
            ClienteService clienteService,
            BarbeiroService barbeiroService,
            ServicoService servicoService,
            FidelidadeService fidelidadeService) {
        this.agendamentoService = agendamentoService;
        this.clienteService = clienteService;
        this.barbeiroService = barbeiroService;
        this.servicoService = servicoService;
        this.fidelidadeService = fidelidadeService;
    }

    @GetMapping("/agendamentos/form-options")
    public Map<String, Object> formOptions(@AuthenticationPrincipal UserDetails user) {
        ConfiguracaoFidelidade config = fidelidadeService.buscarConfiguracaoAtual();
        Cliente cliente = clienteService.buscarPorEmail(user.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));
        var descontoDisponivel = fidelidadeService.buscarDescontoDisponivel(cliente.getId()).orElse(null);
        var cartao = fidelidadeService.buscarCartao(cliente.getId()).orElse(null);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("servicos", servicoService.listarTodos().stream().filter(Servico::isAtivo).map(ApiMapper::servico)
                .collect(Collectors.toList()));
        m.put("barbeiros", barbeiroService.listarTodos().stream().filter(Barbeiro::isAtivo).map(ApiMapper::barbeiro)
                .collect(Collectors.toList()));
        m.put("cortesParaDesconto", config.getCortesParaDesconto());
        m.put("percentualDescontoPadrao", config.getPercentualDesconto());
        m.put("temDesconto", descontoDisponivel != null);
        m.put("percentualDesconto", descontoDisponivel != null ? descontoDisponivel.getPercentualDesconto() : null);
        m.put("pontosAtuais", cartao != null ? cartao.getPontos() : 0);
        return m;
    }

    @GetMapping("/agendamentos/{id}")
    public ResponseEntity<?> agendamentoPorId(@PathVariable Long id, @AuthenticationPrincipal UserDetails user) {
        Cliente cliente = clienteService.buscarPorEmail(user.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));
        Agendamento a = agendamentoService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Agendamento não encontrado"));
        if (!a.getCliente().getId().equals(cliente.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Sem permissão"));
        }
        return ResponseEntity.ok(ApiMapper.agendamento(a));
    }

    @PostMapping("/agendamentos")
    public ResponseEntity<?> criar(@RequestBody AgendamentoFormRequest req, @AuthenticationPrincipal UserDetails user) {
        try {
            Cliente cliente = clienteService.buscarPorEmail(user.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));
            LocalDateTime dt = LocalDateTime.parse(req.dataHora());
            if (dt.isBefore(LocalDateTime.now())) {
                return ResponseEntity.badRequest().body(Map.of("message", "A data/hora deve ser futura"));
            }
            if (agendamentoService.existeConflitoHorario(req.barbeiroId(), dt)) {
                return ResponseEntity.badRequest().body(Map.of("message", "Horário já ocupado para este barbeiro"));
            }
            Agendamento ag = new Agendamento();
            ag.setCliente(cliente);
            Barbeiro b = barbeiroService.buscarPorId(req.barbeiroId())
                    .orElseThrow(() -> new IllegalArgumentException("Barbeiro não encontrado"));
            ag.setBarbeiro(b);
            Servico s = servicoService.buscarPorId(req.servicoId())
                    .orElseThrow(() -> new IllegalArgumentException("Serviço não encontrado"));
            ag.setServico(s);
            double precoOriginal = s.getPreco() != null ? s.getPreco() : 0.0;
            double precoFinal = precoOriginal;
            Double percentualAplicado = null;
            double valorDescontado = 0.0;
            var descontoDisponivel = fidelidadeService.buscarDescontoDisponivel(cliente.getId());
            if (descontoDisponivel.isPresent()) {
                percentualAplicado = descontoDisponivel.get().getPercentualDesconto();
                precoFinal = fidelidadeService.aplicarDesconto(cliente.getId(), precoOriginal);
                valorDescontado = Math.max(0.0, precoOriginal - precoFinal);
            }
            ag.setPrecoOriginal(precoOriginal);
            ag.setPrecoFinal(precoFinal);
            ag.setPercentualDescontoAplicado(percentualAplicado);
            ag.setValorDescontado(valorDescontado);
            ag.setDataHora(dt);
            ag.setStatus("AGENDADO");
            Agendamento salvo = agendamentoService.agendar(ag);
            return ResponseEntity.ok(Map.of("id", salvo.getId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/agendamentos/{id}")
    public ResponseEntity<?> editar(@PathVariable Long id, @RequestBody AgendamentoFormRequest req,
            @AuthenticationPrincipal UserDetails user) {
        try {
            Cliente cliente = clienteService.buscarPorEmail(user.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));
            Agendamento ag = agendamentoService.buscarPorId(id)
                    .orElseThrow(() -> new IllegalArgumentException("Agendamento não encontrado"));
            if (!ag.getCliente().getId().equals(cliente.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Sem permissão"));
            }
            if (!"AGENDADO".equals(ag.getStatus())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Apenas agendamentos AGENDADO podem ser editados."));
            }
            LocalDateTime dt = LocalDateTime.parse(req.dataHora());
            if (dt.isBefore(LocalDateTime.now())) {
                return ResponseEntity.badRequest().body(Map.of("message", "A data/hora deve ser futura"));
            }
            boolean mudouBarbeiroOuHorario = !ag.getBarbeiro().getId().equals(req.barbeiroId())
                    || !ag.getDataHora().equals(dt);
            if (mudouBarbeiroOuHorario && agendamentoService.existeConflitoHorario(req.barbeiroId(), dt)) {
                return ResponseEntity.badRequest().body(Map.of("message", "Horário já ocupado para este barbeiro"));
            }
            Barbeiro barbeiro = barbeiroService.buscarPorId(req.barbeiroId())
                    .orElseThrow(() -> new IllegalArgumentException("Barbeiro não encontrado"));
            Servico servico = servicoService.buscarPorId(req.servicoId())
                    .orElseThrow(() -> new IllegalArgumentException("Serviço não encontrado"));
            ag.setBarbeiro(barbeiro);
            ag.setServico(servico);
            ag.setDataHora(dt);
            agendamentoService.salvar(ag);
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/agendamentos/{id}/confirmacao")
    public Map<String, Object> confirmacao(@PathVariable Long id, @AuthenticationPrincipal UserDetails user) {
        Cliente cliente = clienteService.buscarPorEmail(user.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));
        Agendamento a = agendamentoService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Agendamento não encontrado"));
        if (!a.getCliente().getId().equals(cliente.getId())) {
            throw new IllegalArgumentException("Sem permissão");
        }
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("agendamento", ApiMapper.agendamento(a));
        double precoOriginal = a.getPrecoOriginal() != null ? a.getPrecoOriginal() : a.getServico().getPreco();
        double precoFinal = a.getPrecoFinal() != null ? a.getPrecoFinal() : precoOriginal;
        double valorDescontado = a.getValorDescontado() != null ? a.getValorDescontado() : Math.max(0.0, precoOriginal - precoFinal);
        m.put("descontoAplicado", valorDescontado > 0.0);
        m.put("precoOriginal", precoOriginal);
        m.put("precoFinal", precoFinal);
        m.put("valorDescontado", valorDescontado);
        m.put("percentualDesconto", a.getPercentualDescontoAplicado());
        return m;
    }

    @GetMapping("/agendamentos")
    public Map<String, Object> listar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @AuthenticationPrincipal UserDetails user) {
        Cliente cliente = clienteService.buscarPorEmail(user.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));
        List<Agendamento> lista = agendamentoService.listarPorBarbeiro(cliente.getId());
        ConfiguracaoFidelidade config = fidelidadeService.buscarConfiguracaoAtual();
        boolean temDesconto = fidelidadeService.buscarDescontoDisponivel(cliente.getId()).isPresent();
        if (data != null) {
            lista = lista.stream()
                    .filter(a -> a.getDataHora().toLocalDate().equals(data))
                    .collect(Collectors.toList());
        }
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("agendamentos", lista.stream().map(ApiMapper::agendamento).collect(Collectors.toList()));
        m.put("temDesconto", temDesconto);
        m.put("cortesParaDesconto", config.getCortesParaDesconto());
        m.put("percentualDescontoAtual", temDesconto
                ? fidelidadeService.buscarDescontoDisponivel(cliente.getId()).map(d -> d.getPercentualDesconto()).orElse(null)
                : null);
        return m;
    }

    @PostMapping("/agendamentos/{id}/cancelar")
    public ResponseEntity<?> cancelar(@PathVariable Long id, @AuthenticationPrincipal UserDetails user) {
        try {
            Cliente cliente = clienteService.buscarPorEmail(user.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));
            Agendamento ag = agendamentoService.buscarPorId(id)
                    .orElseThrow(() -> new IllegalArgumentException("Agendamento não encontrado"));
            if (!ag.getCliente().getId().equals(cliente.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Sem permissão"));
            }
            if (ag.getDataHora().minusHours(2).isBefore(LocalDateTime.now())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Cancelamento com no mínimo 2 horas de antecedência"));
            }
            ag.setStatus("CANCELADO");
            agendamentoService.salvar(ag);
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/fidelidade")
    public Map<String, Object> fidelidade(@AuthenticationPrincipal UserDetails user) {
        try {
            Cliente cliente = clienteService.buscarPorEmail(user.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));
            ConfiguracaoFidelidade config = fidelidadeService.buscarConfiguracaoAtual();
            var cartao = fidelidadeService.buscarCartao(cliente.getId()).orElse(null);
            var descontoDisponivel = fidelidadeService.buscarDescontoDisponivel(cliente.getId()).orElse(null);
            int cortesRealizados = cartao != null ? cartao.getTotalCortesRealizados() : 0;
            int pontosAtuais = cartao != null ? cartao.getPontos() : 0;
            boolean temDesconto = descontoDisponivel != null;
            int faltam = temDesconto ? 0 : Math.max(0, config.getCortesParaDesconto() - pontosAtuais);
            Double percentualDisponivel = temDesconto ? descontoDisponivel.getPercentualDesconto() : null;
            Double economiaTotal = fidelidadeService.economiaTotal(cliente.getId());

            Map<String, Object> m = new LinkedHashMap<>();
            Map<String, Object> cartaoPayload = new LinkedHashMap<>();
            cartaoPayload.put("cortesRealizados", cortesRealizados);
            cartaoPayload.put("cortesParaDesconto", config.getCortesParaDesconto());
            cartaoPayload.put("pontosAtuais", pontosAtuais);
            cartaoPayload.put("faltamParaDesconto", faltam);
            cartaoPayload.put("percentualDesconto", config.getPercentualDesconto());
            cartaoPayload.put("percentualDisponivel", percentualDisponivel);
            cartaoPayload.put("temDesconto", temDesconto);
            cartaoPayload.put("economiaTotal", economiaTotal);
            m.put("config", Map.of("percentualDesconto", config.getPercentualDesconto(), "cortesParaDesconto", config.getCortesParaDesconto()));
            m.put("cartao", cartaoPayload);
            m.put("temDesconto", temDesconto);
            m.put("percentualDesconto", percentualDisponivel);
            m.put("totalCortes", cortesRealizados);
            m.put("totalEconomizado", economiaTotal);
            return m;
        } catch (Exception e) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("config", Map.of("percentualDesconto", 10, "cortesParaDesconto", 5));
            m.put("cartao", null);
            m.put("temDesconto", false);
            m.put("percentualDesconto", null);
            m.put("totalCortes", 0);
            m.put("totalEconomizado", 0);
            return m;
        }
    }

    @GetMapping("/historico")
    public Map<String, Object> historico(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) String status,
            @AuthenticationPrincipal UserDetails user) {
        Cliente cliente = clienteService.buscarPorEmail(user.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));
        List<Agendamento> historico = agendamentoService.listarPorBarbeiro(cliente.getId());
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
        historico = historico.stream()
                .sorted((a1, a2) -> a2.getDataHora().compareTo(a1.getDataHora()))
                .collect(Collectors.toList());

        long totalAgendamentos = historico.size();
        long concluidos = historico.stream().filter(a -> "CONCLUIDO".equals(a.getStatus())).count();
        long cancelados = historico.stream().filter(a -> "CANCELADO".equals(a.getStatus())).count();
        double totalEconomizado = fidelidadeService.economiaTotal(cliente.getId());

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("historico", historico.stream().map(ApiMapper::agendamento).collect(Collectors.toList()));
        m.put("totalAgendamentos", totalAgendamentos);
        m.put("concluidos", concluidos);
        m.put("cancelados", cancelados);
        m.put("totalEconomizado", totalEconomizado);
        return m;
    }
}
