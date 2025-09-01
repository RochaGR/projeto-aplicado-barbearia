package com.barbearia.agendamento.controller;

import com.barbearia.agendamento.model.Servico;
import com.barbearia.agendamento.service.ServicoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/servicos")
public class ServicoController {

    private final ServicoService service;

    public ServicoController(ServicoService service) {
        this.service = service;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("servicos", service.listarTodos());
        model.addAttribute("servico", new Servico()); 
        return "admin/servicos/servicos";
    }

    @PostMapping
    public String cadastrar(@ModelAttribute Servico servico, RedirectAttributes attributes) {
        try {
            service.cadastrar(servico);
            attributes.addFlashAttribute("sucesso", "Serviço cadastrado com sucesso!");
        } catch (IllegalArgumentException e) {
            attributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/admin/servicos";
    }

    @GetMapping("/excluir/{id}")
    public String excluir(@PathVariable Long id, RedirectAttributes attributes) {
        try {
            service.excluir(id);
            attributes.addFlashAttribute("sucesso", "Serviço excluído com sucesso!");
        } catch (Exception e) {
            attributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/admin/servicos";
    }
}