package com.barbearia.agendamento.service;

import com.barbearia.agendamento.model.ConfiguracaoHorario;
import com.barbearia.agendamento.repository.ConfiguracaoHorarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class ConfiguracaoHorarioService {

    private final ConfiguracaoHorarioRepository repository;

    public ConfiguracaoHorarioService(ConfiguracaoHorarioRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ConfiguracaoHorario> listarTodos() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<ConfiguracaoHorario> buscarPorDiaSemana(Integer diaSemana) {
        return repository.findByDiaSemana(diaSemana);
    }

    @Transactional
    public ConfiguracaoHorario salvar(ConfiguracaoHorario config) {
        return repository.save(config);
    }

    @Transactional
    public void inicializarDefaults() {
        if (repository.count() > 0) return;

        String[] nomes = {"Domingo", "Segunda-feira", "Terça-feira", "Quarta-feira", "Quinta-feira", "Sexta-feira", "Sábado"};
        for (int i = 1; i <= 7; i++) {
            ConfiguracaoHorario c = new ConfiguracaoHorario();
            c.setDiaSemana(i);
            c.setDiaNome(nomes[i - 1]);
            if (i == 1) {
                c.setAtivo(false);
                c.setAbertura(null);
                c.setFechamento(null);
            } else if (i == 7) {
                c.setAtivo(true);
                c.setAbertura(LocalTime.of(8, 0));
                c.setFechamento(LocalTime.of(12, 0));
            } else {
                c.setAtivo(true);
                c.setAbertura(LocalTime.of(8, 0));
                c.setFechamento(LocalTime.of(19, 0));
            }
            repository.save(c);
        }
    }
}
