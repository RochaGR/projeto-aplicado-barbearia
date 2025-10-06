package com.barbearia.agendamento.controller;

import com.barbearia.agendamento.model.Administrador;
import com.barbearia.agendamento.model.Agendamento;
import com.barbearia.agendamento.model.Barbeiro;
import com.barbearia.agendamento.service.AdministradorService;
import com.barbearia.agendamento.service.AgendamentoService;
import com.barbearia.agendamento.service.BarbeiroService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdministradorService administradorService;

    @Autowired
    private BarbeiroService barbeiroService;

    @Autowired
    private AgendamentoService agendamentoService;

    // LISTA TODOS OS AGENDAMENTOS ORDENADOS COM FILTROS
    @GetMapping("/todos-agendamentos")
    public String verTodosAgendamentos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam(required = false) String status,
            Model model) {

        Agendamento[] todosAgendamentos = agendamentoService.listarTodosOrdenados();
        List<Agendamento> agendamentosFiltrados = Arrays.asList(todosAgendamentos);

        // Filtrar por data se fornecida
        if (data != null) {
            agendamentosFiltrados = agendamentosFiltrados.stream()
                    .filter(a -> a.getDataHora().toLocalDate().equals(data))
                    .collect(Collectors.toList());
        }

        // Filtrar por status se fornecido
        if (status != null && !status.isEmpty()) {
            agendamentosFiltrados = agendamentosFiltrados.stream()
                    .filter(a -> a.getStatus().equalsIgnoreCase(status))
                    .collect(Collectors.toList());
        }

        model.addAttribute("agendamentos", agendamentosFiltrados);
        model.addAttribute("dataFiltro", data);
        model.addAttribute("statusFiltro", status);
        return "todos-agendamentos";
    }

    @PostMapping
    @ResponseBody
    public Administrador cadastrarAdmin(@RequestBody Administrador administrador) {
        return administradorService.salvar(administrador);
    }

    @GetMapping("/{id}")
    @ResponseBody
    public Optional<Administrador> buscarAdminPorId(@PathVariable Long id) {
        return administradorService.buscarPorId(id);
    }

    @GetMapping("/email/{email}")
    @ResponseBody
    public Optional<Administrador> buscarAdminPorEmail(@PathVariable String email) {
        return administradorService.buscarPorEmail(email);
    }

    @GetMapping("/barbeiros")
    public String listarBarbeiros(Model model) {
        List<Barbeiro> barbeiros = barbeiroService.listarTodos();
        model.addAttribute("barbeiros", barbeiros);
        model.addAttribute("barbeiro", new Barbeiro());
        return "barbeiros";
    }

    @PostMapping("/barbeiros/cadastrar")
    public String cadastrarBarbeiro(@ModelAttribute @Valid Barbeiro barbeiro,
                                    RedirectAttributes redirectAttributes) {
        try {
            // Verificar se o email já está em uso
            if (barbeiroService.buscarPorEmail(barbeiro.getEmail()).isPresent()) {
                redirectAttributes.addFlashAttribute("erro", "Este email já está cadastrado!");
                return "redirect:/admin/barbeiros";
            }

            barbeiroService.salvar(barbeiro);
            redirectAttributes.addFlashAttribute("sucesso", "Barbeiro cadastrado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao cadastrar barbeiro: " + e.getMessage());
        }
        return "redirect:/admin/barbeiros";
    }

    @GetMapping("/barbeiros/toggle-status/{id}")
    public String alternarStatusBarbeiro(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Optional<Barbeiro> optional = barbeiroService.buscarPorId(id);
        if (optional.isPresent()) {
            Barbeiro b = optional.get();
            b.setAtivo(!b.isAtivo()); // Alterna o status
            barbeiroService.salvar(b);

            String statusMsg = b.isAtivo() ? "ativado" : "desativado";
            redirectAttributes.addFlashAttribute("sucesso",
                    "Barbeiro " + statusMsg + " com sucesso.");
        } else {
            redirectAttributes.addFlashAttribute("erro", "Barbeiro não encontrado.");
        }
        return "redirect:/admin/barbeiros";
    }

    @GetMapping("/barbeiros/excluir/{id}")
    public String excluirBarbeiro(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Barbeiro> optional = barbeiroService.buscarPorId(id);
            if (optional.isPresent()) {
                // Verificar se o barbeiro tem agendamentos futuros
                List<Agendamento> agendamentosFuturos = agendamentoService.listarPorBarbeiro(id)
                        .stream()
                        .filter(a -> a.getStatus().equals("AGENDADO"))
                        .collect(Collectors.toList());

                if (!agendamentosFuturos.isEmpty()) {
                    redirectAttributes.addFlashAttribute("erro",
                            "Não é possível excluir. Este barbeiro possui agendamentos pendentes.");
                    return "redirect:/admin/barbeiros";
                }

                barbeiroService.excluir(id);
                redirectAttributes.addFlashAttribute("sucesso", "Barbeiro excluído com sucesso.");
            } else {
                redirectAttributes.addFlashAttribute("erro", "Barbeiro não encontrado.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro",
                    "Erro ao excluir barbeiro: " + e.getMessage());
        }
        return "redirect:/admin/barbeiros";
    }

    // CANCELAR AGENDAMENTO (ADMIN)
    @PostMapping("/agendamentos/cancelar/{id}")
    public String cancelarAgendamento(@PathVariable Long id,
                                      RedirectAttributes redirectAttributes) {
        try {
            Agendamento agendamento = agendamentoService.buscarPorId(id)
                    .orElseThrow(() -> new IllegalArgumentException("Agendamento não encontrado"));

            agendamento.setStatus("CANCELADO");
            agendamentoService.salvar(agendamento);

            redirectAttributes.addFlashAttribute("sucesso", "Agendamento cancelado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao cancelar agendamento: " + e.getMessage());
        }
        return "redirect:/admin/todos-agendamentos";
    }

    // CONFIRMAR AGENDAMENTO (ADMIN)
    @PostMapping("/agendamentos/confirmar/{id}")
    public String confirmarAgendamento(@PathVariable Long id,
                                       RedirectAttributes redirectAttributes) {
        try {
            Agendamento agendamento = agendamentoService.buscarPorId(id)
                    .orElseThrow(() -> new IllegalArgumentException("Agendamento não encontrado"));

            agendamento.setStatus("CONFIRMADO");
            agendamentoService.salvar(agendamento);

            redirectAttributes.addFlashAttribute("sucesso", "Agendamento confirmado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao confirmar agendamento: " + e.getMessage());
        }
        return "redirect:/admin/todos-agendamentos";
    }
}