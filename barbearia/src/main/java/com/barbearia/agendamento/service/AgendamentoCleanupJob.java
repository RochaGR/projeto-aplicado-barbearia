package com.barbearia.agendamento.service;

import com.barbearia.agendamento.model.Agendamento;
import com.barbearia.agendamento.repository.AgendamentoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AgendamentoCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(AgendamentoCleanupJob.class);

    private final AgendamentoRepository repository;

    public AgendamentoCleanupJob(AgendamentoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    @Scheduled(cron = "0 0 1 * * *")
    public void cancelarAgendamentosPassados() {
        LocalDateTime agora = LocalDateTime.now();
        List<Agendamento> passados = repository.findPassadosNaoCancelados(agora);

        if (passados.isEmpty()) {
            log.info("Nenhum agendamento passado para cancelar.");
            return;
        }

        log.info("Cancelando {} agendamento(s) passado(s)...", passados.size());
        for (Agendamento ag : passados) {
            ag.setStatus("CANCELADO");
            repository.save(ag);
            log.info("Agendamento {} cancelado automaticamente.", ag.getId());
        }
    }
}
