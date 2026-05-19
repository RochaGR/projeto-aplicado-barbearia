package com.barbearia.agendamento.service;

import com.barbearia.agendamento.model.Feriado;
import com.barbearia.agendamento.repository.FeriadoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class FeriadoService {

    private final FeriadoRepository repository;

    public FeriadoService(FeriadoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Feriado> listarTodos() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public boolean isFeriado(LocalDate data) {
        return repository.existsByData(data);
    }

    @Transactional
    public Feriado salvar(Feriado feriado) {
        if (repository.existsByData(feriado.getData())) {
            throw new IllegalArgumentException("Já existe um feriado cadastrado para esta data");
        }
        return repository.save(feriado);
    }

    @Transactional
    public void excluir(Long id) {
        repository.deleteById(id);
    }
}
