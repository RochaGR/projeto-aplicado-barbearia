package com.barbearia.agendamento.controller;

import com.barbearia.agendamento.model.Administrador;
import com.barbearia.agendamento.service.AdministradorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/config")
public class ConfiguracaoInicialController {

    private final AdministradorService administradorService;

    public ConfiguracaoInicialController(AdministradorService administradorService) {
        this.administradorService = administradorService;
    }

    @GetMapping("/inicial")
    public String paginaConfiguracao(Model model) {
        boolean jaExisteAdmin = administradorService.buscarPorId(1L).isPresent(); 
        if (jaExisteAdmin) {
            return "redirect:/";
        }

        model.addAttribute("administrador", new Administrador());
        return "configuracao-inicial";
    }

    @PostMapping("/primeiro-admin")
    public String criarPrimeiroAdmin(@ModelAttribute Administrador administrador,
            RedirectAttributes redirectAttributes) {
        try {
            administradorService.salvar(administrador);
            redirectAttributes.addFlashAttribute("success",
                    "Administrador criado com sucesso! Faça login para continuar.");
            return "redirect:/clientes/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Erro ao criar administrador: " + e.getMessage());
            return "redirect:/config/inicial";
        }
    }
}
