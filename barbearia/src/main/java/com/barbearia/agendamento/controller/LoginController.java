
package com.barbearia.agendamento.controller;

import com.barbearia.agendamento.model.Barbeiro;
import com.barbearia.agendamento.model.Administrador;
import com.barbearia.agendamento.service.BarbeiroService;
import com.barbearia.agendamento.service.AdministradorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private BarbeiroService barbeiroService;

    @Autowired
    private AdministradorService administradorService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/barbeiro")
    public String loginBarbeiro(@RequestParam String email, @RequestParam String senha) {
        Optional<Barbeiro> barbeiroOpt = barbeiroService.buscarPorEmail(email);
        if (barbeiroOpt.isPresent() && passwordEncoder.matches(senha, barbeiroOpt.get().getSenha())) {
            return "Login de barbeiro bem-sucedido.";
        }
        return "Email ou senha inválidos para barbeiro.";
    }

    @PostMapping("/admin")
    public String loginAdmin(@RequestParam String email, @RequestParam String senha) {
        Optional<Administrador> adminOpt = administradorService.buscarPorEmail(email);
        if (adminOpt.isPresent() && passwordEncoder.matches(senha, adminOpt.get().getSenha())) {
            return "Login de administrador bem-sucedido.";
        }
        return "Email ou senha inválidos para administrador.";
    }
}
