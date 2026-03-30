package com.barbearia.agendamento.service;

import com.barbearia.agendamento.model.ConfiguracaoFidelidade;
import com.barbearia.agendamento.repository.ConfiguracaoFidelidadeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ConfiguracaoFidelidadeService {

    private final ConfiguracaoFidelidadeRepository repository;

    public ConfiguracaoFidelidadeService(ConfiguracaoFidelidadeRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public ConfiguracaoFidelidade buscar() {
        return repository.findById(1L)
                .orElseGet(() -> repository.save(new ConfiguracaoFidelidade()));
    }

    @Transactional
    public ConfiguracaoFidelidade salvar(Double percentualDesconto,
                                         Integer cortesParaDesconto,
                                         String emailAdmin) {
        if (percentualDesconto == null || percentualDesconto < 1 || percentualDesconto > 100) {
            throw new IllegalArgumentException("Percentual deve estar entre 1 e 100");
        }
        if (cortesParaDesconto == null || cortesParaDesconto < 1) {
            throw new IllegalArgumentException("Cortes mínimos deve ser pelo menos 1");
        }

        ConfiguracaoFidelidade config = buscar();
        config.setPercentualDesconto(percentualDesconto);
        config.setCortesParaDesconto(cortesParaDesconto);
        config.setUltimaAtualizacao(LocalDateTime.now());
        config.setAtualizadoPor(emailAdmin);

        return repository.save(config);
    }
}

