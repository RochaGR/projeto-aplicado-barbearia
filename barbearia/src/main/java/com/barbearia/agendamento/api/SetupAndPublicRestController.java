package com.barbearia.agendamento.api;

import com.barbearia.agendamento.model.Administrador;
import com.barbearia.agendamento.service.AdministradorService;
import com.barbearia.agendamento.service.ClienteService;
import com.barbearia.agendamento.service.ServicoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.barbearia.agendamento.model.Cliente;
import com.barbearia.agendamento.model.Servico;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class SetupAndPublicRestController {

    private final AdministradorService administradorService;
    private final ClienteService clienteService;
    private final ServicoService servicoService;

    public SetupAndPublicRestController(AdministradorService administradorService,
            ClienteService clienteService,
            ServicoService servicoService) {
        this.administradorService = administradorService;
        this.clienteService = clienteService;
        this.servicoService = servicoService;
    }

    @GetMapping("/api/setup/required")
    public Map<String, Boolean> setupRequired() {
        return Map.of("required", !administradorService.existeAlgumAdministrador());
    }

    @PostMapping("/api/setup/primeiro-admin")
    public ResponseEntity<?> primeiroAdmin(@RequestBody Administrador administrador) {
        if (administradorService.existeAlgumAdministrador()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Configuração já foi concluída."));
        }
        try {
            administradorService.salvar(administrador);
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/api/servicos/publicos")
    public List<Map<String, Object>> servicosPublicos() {
        return servicoService.listarTodos().stream()
                .filter(Servico::isAtivo)
                .map(ApiMapper::servico)
                .collect(Collectors.toList());
    }

    @PostMapping("/api/clientes/cadastro")
    public ResponseEntity<?> cadastro(@RequestBody ClienteCadastroRequest req) {
        try {
            Cliente c = new Cliente();
            c.setNome(req.nome());
            c.setTelefone(req.telefone());
            c.setEmail(req.email());
            c.setSenha(req.senha());
            clienteService.cadastrarCliente(c);
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}
