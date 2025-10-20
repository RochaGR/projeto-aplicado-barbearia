package com.barbearia.agendamento.controller;

import com.barbearia.agendamento.model.Agendamento;
import com.barbearia.agendamento.service.AgendamentoService;
import com.barbearia.agendamento.service.BarbeiroService;
import com.barbearia.agendamento.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class DashboardController {

    @Autowired
    private AgendamentoService agendamentoService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private BarbeiroService barbeiroService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Estatísticas gerais
        List<Agendamento> todosAgendamentos = agendamentoService.listarTodos();

        // Total de agendamentos
        model.addAttribute("totalAgendamentos", todosAgendamentos.size());

        // Total de clientes
        model.addAttribute("totalClientes", clienteService.listarTodosClientes().size());

        // Total de barbeiros
        model.addAttribute("totalBarbeiros", barbeiroService.listarTodos().size());

        // Agendamentos por status
        long agendados = todosAgendamentos.stream()
                .filter(a -> a.getStatus().equals("AGENDADO"))
                .count();
        long confirmados = todosAgendamentos.stream()
                .filter(a -> a.getStatus().equals("CONFIRMADO"))
                .count();
        long concluidos = todosAgendamentos.stream()
                .filter(a -> a.getStatus().equals("CONCLUIDO"))
                .count();
        long cancelados = todosAgendamentos.stream()
                .filter(a -> a.getStatus().equals("CANCELADO"))
                .count();

        model.addAttribute("agendados", agendados);
        model.addAttribute("confirmados", confirmados);
        model.addAttribute("concluidos", concluidos);
        model.addAttribute("cancelados", cancelados);

        // Agendamentos de hoje
        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
        LocalDateTime fimDia = LocalDate.now().atTime(23, 59, 59);

        List<Agendamento> agendamentosHoje = todosAgendamentos.stream()
                .filter(a -> a.getDataHora().isAfter(inicioDia) &&
                        a.getDataHora().isBefore(fimDia))
                .toList();

        model.addAttribute("agendamentosHoje", agendamentosHoje.size());

        // Agendamentos desta semana
        LocalDateTime inicioSemana = LocalDate.now().atStartOfDay().minusDays(LocalDate.now().getDayOfWeek().getValue() - 1);
        LocalDateTime fimSemana = inicioSemana.plusDays(6).withHour(23).withMinute(59).withSecond(59);

        long agendamentosSemana = todosAgendamentos.stream()
                .filter(a -> a.getDataHora().isAfter(inicioSemana) &&
                        a.getDataHora().isBefore(fimSemana))
                .count();

        model.addAttribute("agendamentosSemana", agendamentosSemana);

        // Agendamentos deste mês
        YearMonth mesAtual = YearMonth.now();
        LocalDateTime inicioMes = mesAtual.atDay(1).atStartOfDay();
        LocalDateTime fimMes = mesAtual.atEndOfMonth().atTime(23, 59, 59);

        long agendamentosMes = todosAgendamentos.stream()
                .filter(a -> a.getDataHora().isAfter(inicioMes) &&
                        a.getDataHora().isBefore(fimMes))
                .count();

        model.addAttribute("agendamentosMes", agendamentosMes);

        // Receita do mês (apenas agendamentos concluídos)
        double receitaMes = todosAgendamentos.stream()
                .filter(a -> a.getStatus().equals("CONCLUIDO") &&
                        a.getDataHora().isAfter(inicioMes) &&
                        a.getDataHora().isBefore(fimMes))
                .mapToDouble(a -> a.getServico().getPreco())
                .sum();

        model.addAttribute("receitaMes", receitaMes);

        // Barbeiro com mais agendamentos do mês
        Map<String, Long> agendamentosPorBarbeiro = new HashMap<>();
        todosAgendamentos.stream()
                .filter(a -> a.getDataHora().isAfter(inicioMes) &&
                        a.getDataHora().isBefore(fimMes))
                .forEach(a -> {
                    String nome = a.getBarbeiro().getNome();
                    agendamentosPorBarbeiro.put(nome,
                            agendamentosPorBarbeiro.getOrDefault(nome, 0L) + 1);
                });

        String barbeiroDestaque = agendamentosPorBarbeiro.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Nenhum");

        model.addAttribute("barbeiroDestaque", barbeiroDestaque);

        // Serviço mais solicitado do mês
        Map<String, Long> servicosPorNome = new HashMap<>();
        todosAgendamentos.stream()
                .filter(a -> a.getDataHora().isAfter(inicioMes) &&
                        a.getDataHora().isBefore(fimMes))
                .forEach(a -> {
                    String nome = a.getServico().getNome();
                    servicosPorNome.put(nome,
                            servicosPorNome.getOrDefault(nome, 0L) + 1);
                });

        String servicoDestaque = servicosPorNome.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Nenhum");

        model.addAttribute("servicoDestaque", servicoDestaque);

        // Taxa de cancelamento do mês
        double taxaCancelamento = agendamentosMes > 0 ?
                (cancelados * 100.0) / agendamentosMes : 0;
        model.addAttribute("taxaCancelamento", String.format("%.1f", taxaCancelamento));

        // Próximos agendamentos (próximas 24 horas)
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime proximas24h = agora.plusHours(24);

        List<Agendamento> proximosAgendamentos = todosAgendamentos.stream()
                .filter(a -> a.getDataHora().isAfter(agora) &&
                        a.getDataHora().isBefore(proximas24h) &&
                        !a.getStatus().equals("CANCELADO"))
                .sorted((a1, a2) -> a1.getDataHora().compareTo(a2.getDataHora()))
                .limit(5)
                .toList();

        model.addAttribute("proximosAgendamentos", proximosAgendamentos);

        return "admin/dashboard";
    }
}