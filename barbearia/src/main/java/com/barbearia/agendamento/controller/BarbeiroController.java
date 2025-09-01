
package com.barbearia.agendamento.controller;

import com.barbearia.agendamento.model.Barbeiro;
import com.barbearia.agendamento.service.BarbeiroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/barbeiros")
public class BarbeiroController {

    @Autowired
    private BarbeiroService barbeiroService;

    @PostMapping
    public Barbeiro cadastrar(@RequestBody Barbeiro barbeiro) {
        return barbeiroService.salvar(barbeiro);
    }

    @GetMapping("/{id}")
    public Optional<Barbeiro> buscarPorId(@PathVariable Long id) {
        return barbeiroService.buscarPorId(id);
    }

    @GetMapping("/email/{email}")
    public Optional<Barbeiro> buscarPorEmail(@PathVariable String email) {
        return barbeiroService.buscarPorEmail(email);
    }
}
