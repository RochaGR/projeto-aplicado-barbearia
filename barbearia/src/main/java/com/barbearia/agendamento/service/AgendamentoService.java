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

       public AgendamentoService(
                     AgendamentoRepository repository,
                     BarbeiroService barbeiroService,
                     ServicoService servicoService,
                     ClienteService clienteService) {
              this.repository = repository;
              this.barbeiroService = barbeiroService;
              this.servicoService = servicoService;
              this.clienteService = clienteService;
       }

       public Optional<Agendamento> buscarPorId(Long id) {
              return repository.findById(id);
       }

       public Agendamento agendar(Agendamento agendamento) {
              Barbeiro barbeiro = barbeiroService.buscarPorId(agendamento.getBarbeiro().getId())
                            .orElseThrow(() -> new IllegalArgumentException("Barbeiro não encontrado"));

              Servico servico = servicoService.buscarPorId(agendamento.getServico().getId())
                            .orElseThrow(() -> new IllegalArgumentException("Serviço não encontrado"));

              Cliente cliente = clienteService.buscarPorId(agendamento.getCliente().getId())
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

       public Page<Agendamento> listarTodos(Pageable pageable) {
              return repository.findAll(pageable);
       }

       public List<Agendamento> listarPorBarbeiro(Long barbeiroId) {
              return repository.findByBarbeiroId(barbeiroId);
       }

       public boolean existeConflitoHorario(Long barbeiroId, LocalDateTime dataHora) {
              return repository.existsByBarbeiroIdAndDataHoraAndStatusNot(barbeiroId, dataHora, "CANCELADO");
       }

       public Agendamento salvar(Agendamento agendamento) {
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
