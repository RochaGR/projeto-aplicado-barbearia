package com.barbearia.agendamento.service;

import com.barbearia.agendamento.fila.ListaOrdenada;
import com.barbearia.agendamento.model.Agendamento;
import com.barbearia.agendamento.model.Barbeiro;
import com.barbearia.agendamento.model.Cliente;
import com.barbearia.agendamento.model.Servico;
import com.barbearia.agendamento.repository.AgendamentoRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AgendamentoService {

       private final AgendamentoRepository repository;
       private final BarbeiroService barbeiroService;
       private final ServicoService servicoService;
       private final ClienteService clienteService;
       private final FidelidadeService fidelidadeService;

       public AgendamentoService(
                     AgendamentoRepository repository,
                     BarbeiroService barbeiroService,
                     ServicoService servicoService,
                     ClienteService clienteService,
                     FidelidadeService fidelidadeService) {
              this.repository = repository;
              this.barbeiroService = barbeiroService;
              this.servicoService = servicoService;
              this.clienteService = clienteService;
              this.fidelidadeService = fidelidadeService;
       }

       public Optional<Agendamento> buscarPorId(@NonNull Long id) {
              return repository.findById(id);
       }

       public Agendamento agendar(Agendamento agendamento) {
              Long barbeiroId = agendamento.getBarbeiro().getId();
              if (barbeiroId == null) {
                     throw new IllegalArgumentException("ID do barbeiro não pode ser nulo");
              }
              Barbeiro barbeiro = barbeiroService.buscarPorId(barbeiroId)
                            .orElseThrow(() -> new IllegalArgumentException("Barbeiro não encontrado"));

              Long servicoId = agendamento.getServico().getId();
              if (servicoId == null) {
                     throw new IllegalArgumentException("ID do serviço não pode ser nulo");
              }
              Servico servico = servicoService.buscarPorId(servicoId)
                            .orElseThrow(() -> new IllegalArgumentException("Serviço não encontrado"));

              Long clienteId = agendamento.getCliente().getId();
              if (clienteId == null) {
                     throw new IllegalArgumentException("ID do cliente não pode ser nulo");
              }
              Cliente cliente = clienteService.buscarPorId(clienteId)
                            .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));

              if (existeConflitoHorario(barbeiro.getId(), agendamento.getDataHora())) {
                     throw new IllegalStateException("Barbeiro já possui agendamento neste horário");
              }

              agendamento.setBarbeiro(barbeiro);
              agendamento.setServico(servico);
              agendamento.setCliente(cliente);

              return repository.save(agendamento);
       }

       public List<Agendamento> findByBarbeiroEmail(String email) {
              return repository.findByBarbeiroEmail(email);
       }

       public List<Agendamento> listarTodos() {
              return repository.findAll();
       }

       public Page<Agendamento> listarTodos(@NonNull Pageable pageable) {
              return repository.findAll(pageable);
       }

       public List<Agendamento> listarPorBarbeiro(Long barbeiroId) {
              return repository.findByBarbeiroId(barbeiroId);
       }

       public boolean existeConflitoHorario(Long barbeiroId, LocalDateTime dataHora) {
              return repository.existsByBarbeiroIdAndDataHoraAndStatusNot(barbeiroId, dataHora, "CANCELADO");
       }

<<<<<<< HEAD
       public Agendamento salvar(Agendamento agendamento) {
              if (agendamento.getId() != null) {
                     Agendamento existente = repository.findById(agendamento.getId()).orElse(null);
                     if (existente != null && existente.isPontoRegistrado()) {
                            agendamento.setPontoRegistrado(true);
                     }
              }

              if ("CONCLUIDO".equalsIgnoreCase(agendamento.getStatus()) && !agendamento.isPontoRegistrado()) {
                     fidelidadeService.registrarCorte(agendamento.getCliente());
                     agendamento.setPontoRegistrado(true);
              }
=======
       public Agendamento salvar(@NonNull Agendamento agendamento) {
>>>>>>> 3986e7d
              return repository.save(agendamento);
       }

       public List<Agendamento> findByBarbeiroIdAndPeriodo(Long barbeiroId,
                     LocalDateTime inicio,
                     LocalDateTime fim) {
              return repository.findByBarbeiroIdAndPeriodo(barbeiroId, inicio, fim);
       }

       public Page<Agendamento> findByStatus(String status, Pageable pageable) {
              return repository.findByStatus(status, pageable);
       }

       public Page<Agendamento> findByDataHoraBetween(LocalDateTime inicio, LocalDateTime fim, Pageable pageable) {
              return repository.findByDataHoraBetween(inicio, fim, pageable);
       }

       public Page<Agendamento> findByDataHoraBetweenAndStatus(LocalDateTime inicio, LocalDateTime fim, String status,
                     Pageable pageable) {
              return repository.findByDataHoraBetweenAndStatus(inicio, fim, status, pageable);
       }

       public Agendamento[] listarTodosOrdenados() {
              List<Agendamento> listaOriginal = repository.findAll();

              ListaOrdenada fila = new ListaOrdenada();

              for (int i = 0; i < listaOriginal.size(); i++) {
                     fila.enqueueOrdenado(listaOriginal.get(i));
              }

              Agendamento[] ordenados = new Agendamento[listaOriginal.size()];
              int i = 0;

              while (!fila.isEmpty()) {
                     ordenados[i] = fila.dequeue();
                     i++;
              }

              return ordenados;
       }

}
