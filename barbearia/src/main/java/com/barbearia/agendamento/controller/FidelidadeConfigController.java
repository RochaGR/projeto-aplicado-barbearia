package com.barbearia.agendamento.controller;

import com.barbearia.agendamento.service.ConfiguracaoFidelidadeService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/fidelidade")
public class FidelidadeConfigController {

    private final ConfiguracaoFidelidadeService configService;

    public FidelidadeConfigController(ConfiguracaoFidelidadeService configService) {
        this.configService = configService;
    }

    @GetMapping("/config")
    public String exibirConfig(Model model) {
        model.addAttribute("config", configService.buscar());
        return "admin/fidelidade-config";
    }

    @PostMapping("/config/salvar")
    public String salvarConfig(@RequestParam Double percentualDesconto,
                               @RequestParam Integer cortesParaDesconto,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        try {
            String emailAdmin = userDetails != null ? userDetails.getUsername() : "sistema";
            configService.salvar(percentualDesconto, cortesParaDesconto, emailAdmin);
            redirectAttributes.addFlashAttribute("sucesso", "Configuração atualizada com sucesso!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/admin/fidelidade/config";
    }
}

