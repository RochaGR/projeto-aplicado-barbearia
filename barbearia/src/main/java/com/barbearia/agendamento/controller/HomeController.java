package com.barbearia.agendamento.controller;

import com.barbearia.agendamento.service.AdministradorService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final AdministradorService administradorService;

    public HomeController(AdministradorService administradorService) {
        this.administradorService = administradorService;
    }

    @GetMapping("/")
    public String home() {
        boolean adminExiste = administradorService.buscarPorId(1L).isPresent();
        if (!adminExiste) {
            return "redirect:/config/inicial";
        }
        return "index";
    }
}
