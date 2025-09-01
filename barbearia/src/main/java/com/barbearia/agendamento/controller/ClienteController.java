package com.barbearia.agendamento.controller;

import com.barbearia.agendamento.model.Cliente;
import com.barbearia.agendamento.service.ClienteService;
import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping("/cadastro")
    public String mostrarFormularioCadastro(Model model) {
        model.addAttribute("cliente", new Cliente());
        return "cadastro-cliente";
    }

    @PostMapping("/cadastrar")
    public String cadastrarCliente(@Valid @ModelAttribute Cliente cliente,
            BindingResult result,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "cadastro-cliente";
        }

        try {
            clienteService.cadastrarCliente(cliente);
            redirectAttributes.addFlashAttribute("sucesso",
                    "Cadastro realizado com sucesso! Faça login para continuar.");
            return "redirect:/clientes/login";
        } catch (IllegalArgumentException e) {
            result.rejectValue("email", "error.cliente", e.getMessage());
            return "cadastro-cliente";
        }
    }

    @GetMapping("/login")
    public String exibirLogin() {
        return "login"; 
    }
}
