package com.barbearia.agendamento.api;

import com.barbearia.agendamento.model.Agendamento;
import com.barbearia.agendamento.model.Barbeiro;
import com.barbearia.agendamento.model.Servico;
import com.barbearia.agendamento.service.AgendamentoService;
import com.barbearia.agendamento.service.BarbeiroService;
import com.barbearia.agendamento.service.ClienteService;
import com.barbearia.agendamento.service.ConfiguracaoFidelidadeService;
import com.barbearia.agendamento.service.ServicoService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.Locale;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminRestController {

    private final AgendamentoService agendamentoService;
    private final ClienteService clienteService;
    private final BarbeiroService barbeiroService;
    private final ServicoService servicoService;
    private final ConfiguracaoFidelidadeService configuracaoFidelidadeService;

    public AdminRestController(AgendamentoService agendamentoService,
            ClienteService clienteService,
            BarbeiroService barbeiroService,
            ServicoService servicoService,
            ConfiguracaoFidelidadeService configuracaoFidelidadeService) {
        this.agendamentoService = agendamentoService;
        this.clienteService = clienteService;
        this.barbeiroService = barbeiroService;
        this.servicoService = servicoService;
        this.configuracaoFidelidadeService = configuracaoFidelidadeService;
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        List<Agendamento> todos = agendamentoService.listarTodos();

        long agendados = todos.stream().filter(a -> "AGENDADO".equals(a.getStatus())).count();
        long confirmados = todos.stream().filter(a -> "CONFIRMADO".equals(a.getStatus())).count();
        long concluidos = todos.stream().filter(a -> "CONCLUIDO".equals(a.getStatus())).count();
        long cancelados = todos.stream().filter(a -> "CANCELADO".equals(a.getStatus())).count();

        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
        LocalDateTime fimDia = LocalDate.now().atTime(23, 59, 59);
        long agendamentosHoje = todos.stream()
                .filter(a -> !a.getDataHora().isBefore(inicioDia) && !a.getDataHora().isAfter(fimDia))
                .count();

        LocalDateTime inicioSemana = LocalDate.now().atStartOfDay()
                .minusDays(LocalDate.now().getDayOfWeek().getValue() - 1L);
        LocalDateTime fimSemana = inicioSemana.plusDays(6).withHour(23).withMinute(59).withSecond(59);
        long agendamentosSemana = todos.stream()
                .filter(a -> !a.getDataHora().isBefore(inicioSemana) && !a.getDataHora().isAfter(fimSemana))
                .count();

        YearMonth mesAtual = YearMonth.now();
        LocalDateTime inicioMes = mesAtual.atDay(1).atStartOfDay();
        LocalDateTime fimMes = mesAtual.atEndOfMonth().atTime(23, 59, 59);
        long agendamentosMes = todos.stream()
                .filter(a -> !a.getDataHora().isBefore(inicioMes) && !a.getDataHora().isAfter(fimMes))
                .count();

        double receitaMes = todos.stream()
                .filter(a -> "CONCLUIDO".equals(a.getStatus())
                        && !a.getDataHora().isBefore(inicioMes)
                        && !a.getDataHora().isAfter(fimMes))
                .mapToDouble(a -> a.getServico().getPreco())
                .sum();

        Map<String, Long> porBarbeiro = new HashMap<>();
        todos.stream()
                .filter(a -> !a.getDataHora().isBefore(inicioMes) && !a.getDataHora().isAfter(fimMes))
                .forEach(a -> porBarbeiro.merge(a.getBarbeiro().getNome(), 1L, (oldVal, newVal) -> oldVal + newVal));
        String barbeiroDestaque = porBarbeiro.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Nenhum");

        Map<String, Long> porServico = new HashMap<>();
        todos.stream()
                .filter(a -> !a.getDataHora().isBefore(inicioMes) && !a.getDataHora().isAfter(fimMes))
                .forEach(a -> porServico.merge(a.getServico().getNome(), 1L, (oldVal, newVal) -> oldVal + newVal));
        String servicoDestaque = porServico.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Nenhum");

        double taxaCancelamento = agendamentosMes > 0 ? (cancelados * 100.0) / agendamentosMes : 0;

        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime prox24 = agora.plusHours(24);
        List<Map<String, Object>> proximos = todos.stream()
                .filter(a -> a.getDataHora().isAfter(agora)
                        && a.getDataHora().isBefore(prox24)
                        && !"CANCELADO".equals(a.getStatus()))
                .sorted(Comparator.comparing(Agendamento::getDataHora))
                .limit(5)
                .map(ApiMapper::agendamento)
                .collect(Collectors.toList());

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("totalAgendamentos", todos.size());
        m.put("totalClientes", clienteService.listarTodosClientes().size());
        m.put("totalBarbeiros", barbeiroService.listarTodos().size());
        m.put("agendados", agendados);
        m.put("confirmados", confirmados);
        m.put("concluidos", concluidos);
        m.put("cancelados", cancelados);
        m.put("agendamentosHoje", agendamentosHoje);
        m.put("agendamentosSemana", agendamentosSemana);
        m.put("agendamentosMes", agendamentosMes);
        m.put("receitaMes", receitaMes);
        m.put("barbeiroDestaque", barbeiroDestaque);
        m.put("servicoDestaque", servicoDestaque);
        m.put("taxaCancelamento", String.format(Locale.ROOT, "%.1f", taxaCancelamento));
        m.put("proximosAgendamentos", proximos);
        return m;
    }

    @GetMapping("/agendamentos")
    public Map<String, Object> todosAgendamentos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam(required = false) String status) {
        Agendamento[] arr = agendamentoService.listarTodosOrdenados();
        List<Agendamento> lista = new ArrayList<>(Arrays.asList(arr));
        if (data != null) {
            lista = lista.stream()
                    .filter(a -> a.getDataHora().toLocalDate().equals(data))
                    .collect(Collectors.toList());
        }
        if (status != null && !status.isEmpty()) {
            lista = lista.stream()
                    .filter(a -> a.getStatus().equalsIgnoreCase(status))
                    .collect(Collectors.toList());
        }
        return Map.of("agendamentos",
                lista.stream().map(ApiMapper::agendamento).collect(Collectors.toList()));
    }

    @PostMapping("/agendamentos/{id}/confirmar")
    public ResponseEntity<?> confirmar(@PathVariable @NonNull Long id) {
        try {
            Agendamento ag = agendamentoService.buscarPorId(id)
                    .orElseThrow(() -> new IllegalArgumentException("Agendamento não encontrado"));
            ag.setStatus("CONFIRMADO");
            agendamentoService.salvar(ag);
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/agendamentos/{id}/cancelar")
    public ResponseEntity<?> cancelar(@PathVariable @NonNull Long id) {
        try {
            Agendamento ag = agendamentoService.buscarPorId(id)
                    .orElseThrow(() -> new IllegalArgumentException("Agendamento não encontrado"));
            ag.setStatus("CANCELADO");
            agendamentoService.salvar(ag);
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/barbeiros")
    public Map<String, Object> barbeiros() {
        return Map.of("barbeiros",
                barbeiroService.listarTodos().stream().map(ApiMapper::barbeiro).collect(Collectors.toList()));
    }

    @PostMapping("/barbeiros")
    public ResponseEntity<?> cadastrarBarbeiro(@RequestBody Barbeiro barbeiro) {
        try {
            if (barbeiro.getNome() == null || barbeiro.getNome().trim().length() < 3) {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "Nome deve ter no mínimo 3 caracteres",
                        "errors", Map.of("nome", "Nome deve ter no mínimo 3 caracteres")));
            }
            if (barbeiro.getEmail() == null || !barbeiro.getEmail().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "Informe um email válido",
                        "errors", Map.of("email", "Informe um email válido")));
            }
            if (barbeiro.getSenha() == null
                    || barbeiro.getSenha().length() < 8
                    || !barbeiro.getSenha().matches(".*[A-Z].*")
                    || !barbeiro.getSenha().matches(".*\\d.*")
                    || !barbeiro.getSenha().matches(".*[^a-zA-Z0-9].*")) {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "Senha não atende aos requisitos de segurança",
                        "errors", Map.of("senha", "Senha não atende aos requisitos de segurança")));
            }
            if (barbeiroService.buscarPorEmail(barbeiro.getEmail()).isPresent()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of(
                                "message", "Este email já está cadastrado",
                                "errors", Map.of("email", "Este email já está cadastrado")));
            }
            barbeiro.setNome(barbeiro.getNome().trim());
            barbeiro.setEmail(barbeiro.getEmail().trim());
            barbeiro.setTelefone(barbeiro.getTelefone() == null ? null : barbeiro.getTelefone().trim());
            barbeiroService.salvar(barbeiro);
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/barbeiros/{id}/toggle")
    public ResponseEntity<?> toggle(@PathVariable @NonNull Long id) {
        try {
            barbeiroService.alternarAtivo(id);
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/barbeiros/{id}")
    public ResponseEntity<?> excluirBarbeiro(@PathVariable @NonNull Long id) {
        try {
            List<Agendamento> pendentes = agendamentoService.listarPorBarbeiro(id).stream()
                    .filter(a -> "AGENDADO".equals(a.getStatus()))
                    .collect(Collectors.toList());
            if (!pendentes.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Barbeiro possui agendamentos pendentes"));
            }
            barbeiroService.excluir(id);
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/servicos")
    public Map<String, Object> servicos() {
        return Map.of("servicos",
                servicoService.listarTodos().stream().map(ApiMapper::servico).collect(Collectors.toList()));
    }

    @PostMapping("/servicos")
    public ResponseEntity<?> cadastrarServico(@RequestBody Servico servico) {
        try {
            if (servico.getNome() == null || servico.getNome().trim().length() < 3) {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "Nome deve ter no mínimo 3 caracteres",
                        "errors", Map.of("nome", "Nome deve ter no mínimo 3 caracteres")));
            }
            if (servico.getDescricao() == null || servico.getDescricao().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "Descrição é obrigatória",
                        "errors", Map.of("descricao", "Descrição é obrigatória")));
            }
            if (servico.getPreco() == null || servico.getPreco() < 0) {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "Preço inválido",
                        "errors", Map.of("preco", "Preço inválido")));
            }
            if (servico.getDuracaoMinutos() == null || servico.getDuracaoMinutos() < 1) {
                return ResponseEntity.badRequest().body(Map.of(
                        "message", "Duração inválida",
                        "errors", Map.of("duracaoMinutos", "Duração inválida")));
            }
            servico.setNome(servico.getNome().trim());
            servico.setDescricao(servico.getDescricao().trim());
            servicoService.cadastrar(servico);
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/servicos/{id}")
    public ResponseEntity<?> excluirServico(@PathVariable @NonNull Long id) {
        try {
            servicoService.excluir(id);
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/fidelidade")
    public Map<String, Object> fidelidadeGet() {
        var config = configuracaoFidelidadeService.buscar();
        return Map.of(
                "percentualDesconto", config.getPercentualDesconto(),
                "cortesParaDesconto", config.getCortesParaDesconto());
    }

    public record FidelidadeBody(int percentualDesconto, int cortesParaDesconto) {
    }

    @PostMapping("/fidelidade")
    public ResponseEntity<?> fidelidadePost(@RequestBody FidelidadeBody body) {
        try {
            configuracaoFidelidadeService.salvar((double) body.percentualDesconto(), body.cortesParaDesconto(), "admin-api");
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
