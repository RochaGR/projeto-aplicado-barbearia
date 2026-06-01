package com.barbearia.agendamento.service;

import com.barbearia.agendamento.fila.ListaOrdenada;
import com.barbearia.agendamento.model.Agendamento;
import com.barbearia.agendamento.model.Barbeiro;
import com.barbearia.agendamento.model.Cliente;
import com.barbearia.agendamento.model.ConfiguracaoHorario;
import com.barbearia.agendamento.model.Servico;
import com.barbearia.agendamento.repository.AgendamentoRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class AgendamentoService {

       private final AgendamentoRepository repository;
       private final BarbeiroService barbeiroService;
       private final ServicoService servicoService;
       private final ClienteService clienteService;
       private final FidelidadeService fidelidadeService;
       private final EmailService emailService;
       private final ConfiguracaoHorarioService configuracaoHorarioService;
       private final FeriadoService feriadoService;

       public AgendamentoService(
               AgendamentoRepository repository,
               BarbeiroService barbeiroService,
               ServicoService servicoService,
               ClienteService clienteService,
               FidelidadeService fidelidadeService,
               EmailService emailService,
               ConfiguracaoHorarioService configuracaoHorarioService,
               FeriadoService feriadoService) {

              this.repository = repository;
              this.barbeiroService = barbeiroService;
              this.servicoService = servicoService;
              this.clienteService = clienteService;
              this.fidelidadeService = fidelidadeService;
              this.emailService = emailService;
              this.configuracaoHorarioService = configuracaoHorarioService;
              this.feriadoService = feriadoService;
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

              Agendamento salvo = repository.save(agendamento);

              emailService.enviarConfirmacaoAgendamento(salvo);

              return salvo;
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

       public List<Agendamento> listarPorCliente(Long clienteId) {
              return repository.findByClienteId(clienteId);
       }

       public boolean existeConflitoHorario(Long barbeiroId, LocalDateTime dataHora) {
              return repository.existsByBarbeiroIdAndDataHoraAndStatusNot(barbeiroId, dataHora, "CANCELADO");
       }

       public Agendamento salvar(@NonNull Agendamento agendamento) {

              if (agendamento.getId() != null) {
                     Agendamento existente = repository.findById(agendamento.getId()).orElse(null);
                     if (existente != null && existente.getPontoRegistrado()) {
                            agendamento.setPontoRegistrado(true);
                     }
              }

              if ("CONCLUIDO".equalsIgnoreCase(agendamento.getStatus()) && !agendamento.getPontoRegistrado()) {
                     fidelidadeService.registrarCorte(agendamento.getCliente());
                     agendamento.setPontoRegistrado(true);
              }

              Agendamento salvo = repository.save(agendamento);

              if ("CANCELADO".equalsIgnoreCase(salvo.getStatus())) {
                     emailService.enviarCancelamentoAgendamento(salvo);
              }

              return salvo;
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

       public Page<Agendamento> findByDataHoraBetweenAndStatus(LocalDateTime inicio,
                                                               LocalDateTime fim,
                                                               String status,
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

         public List<Map<String, Object>> listarHorariosDisponiveis(Long barbeiroId, Long servicoId, LocalDate data) {
             if (data.isBefore(LocalDate.now())) {
                 return List.of();
             }
             Servico servico = servicoService.buscarPorId(servicoId)
                     .orElseThrow(() -> new IllegalArgumentException("Serviço não encontrado"));
             int duracaoMinutos = servico.getDuracaoMinutos() != null ? servico.getDuracaoMinutos() : 30;

             int diaSemana = data.getDayOfWeek().getValue();
             if (data.getDayOfWeek() == DayOfWeek.SUNDAY) {
                 diaSemana = 1;
             } else {
                 diaSemana = data.getDayOfWeek().getValue() + 1;
             }

             ConfiguracaoHorario config = configuracaoHorarioService.buscarPorDiaSemana(diaSemana).orElse(null);
             if (config == null || !config.getAtivo()) {
                 return List.of();
             }

             if (feriadoService.isFeriado(data)) {
                 return List.of();
             }

             LocalTime horarioInicial = config.getAbertura() != null ? config.getAbertura() : LocalTime.of(8, 0);
             LocalTime horarioLimite = config.getFechamento() != null ? config.getFechamento() : LocalTime.of(19, 0);

             LocalDateTime inicio = data.atStartOfDay();
             LocalDateTime fim = data.atTime(23, 59);
             List<Agendamento> existentes = repository.findByBarbeiroIdAndPeriodo(barbeiroId, inicio, fim);

             DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");

             List<Map<String, Object>> slots = new ArrayList<>();
             LocalTime slotTime = horarioInicial;

             while (slotTime.isBefore(horarioLimite)) {
                 LocalDateTime slotDateTime = LocalDateTime.of(data, slotTime);
                 LocalDateTime slotFim = slotDateTime.plusMinutes(duracaoMinutos);

                 boolean cabeHorario = !slotFim.toLocalTime().isAfter(horarioLimite);

                 boolean ocupado = false;
                 if (cabeHorario) {
                     for (Agendamento ag : existentes) {
                         LocalDateTime agInicio = ag.getDataHora();
                         int agDuracao = ag.getServico().getDuracaoMinutos() != null
                                 ? ag.getServico().getDuracaoMinutos() : 30;
                         LocalDateTime agFim = agInicio.plusMinutes(agDuracao);

                         if (slotDateTime.isBefore(agFim) && slotFim.isAfter(agInicio)) {
                             ocupado = true;
                             break;
                         }
                     }
                 }

                 Map<String, Object> slot = new LinkedHashMap<>();
                 slot.put("horario", slotTime.format(fmt));
                 slot.put("disponivel", cabeHorario && !ocupado);
                 slots.add(slot);

                 slotTime = slotTime.plusMinutes(30);
             }

             return slots;
         }

         public List<Agendamento> cancelarAgendamentosPorData(LocalDate data, String motivo) {
             LocalDateTime inicio = data.atStartOfDay();
             LocalDateTime fim = data.plusDays(1).atStartOfDay();
             List<Agendamento> afetados = repository.findByDataBetweenAndStatusAtivo(inicio, fim);

             for (Agendamento ag : afetados) {
                 ag.setStatus("CANCELADO");
                 repository.save(ag);
                 emailService.enviarCancelamentoFeriado(ag, motivo, data);
                 emailService.enviarNotificacaoBarbeiro(ag, motivo, data);
             }

             return afetados;
         }
}