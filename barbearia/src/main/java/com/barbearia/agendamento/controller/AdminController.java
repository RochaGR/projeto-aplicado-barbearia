package com.barbearia.agendamento.controller;

import com.barbearia.agendamento.model.Administrador;
import com.barbearia.agendamento.model.Agendamento;
import com.barbearia.agendamento.model.Barbeiro;
import com.barbearia.agendamento.service.AdministradorService;
import com.barbearia.agendamento.service.AgendamentoService;
import com.barbearia.agendamento.service.BarbeiroService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdministradorService administradorService;

    @Autowired
    private BarbeiroService barbeiroService;

    @Autowired
    private AgendamentoService agendamentoService;

    // Lista Ordenada

    @GetMapping("/todos-agendamentos")
    public String verTodosAgendamentos(Model model) {
        Agendamento[] agendamentos = agendamentoService.listarTodosOrdenados();
        model.addAttribute("agendamentos", agendamentos);
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
            barbeiroService.salvar(b);
            redirectAttributes.addFlashAttribute("sucesso", "Status do barbeiro atualizado.");
        } else {
            redirectAttributes.addFlashAttribute("erro", "Barbeiro não encontrado.");
        }
        return "redirect:/admin/barbeiros";
    }

    @GetMapping("/barbeiros/excluir/{id}")
    public String excluirBarbeiro(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Optional<Barbeiro> optional = barbeiroService.buscarPorId(id);
        if (optional.isPresent()) {
            barbeiroService.excluir(id);
            redirectAttributes.addFlashAttribute("sucesso", "Barbeiro excluído com sucesso.");
        } else {
            redirectAttributes.addFlashAttribute("erro", "Barbeiro não encontrado.");
        }
        return "redirect:/admin/barbeiros";
    }
}
