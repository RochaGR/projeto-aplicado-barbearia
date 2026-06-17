package com.barbearia.agendamento.service;

import com.barbearia.agendamento.model.Agendamento;
import com.barbearia.agendamento.model.Barbeiro;
import com.barbearia.agendamento.model.Cliente;
import com.barbearia.agendamento.model.ConfiguracaoHorario;
import com.barbearia.agendamento.model.Servico;
import com.barbearia.agendamento.repository.AgendamentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários do AgendamentoService")
class AgendamentoServiceTest {

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @Mock
    private BarbeiroService barbeiroService;

    @Mock
    private ServicoService servicoService;

    @Mock
    private ClienteService clienteService;

    @Mock
    private FidelidadeService fidelidadeService;

    @Mock
    private EmailService emailService;

    @Mock
    private ConfiguracaoHorarioService configuracaoHorarioService;

    @Mock
    private FeriadoService feriadoService;

    @InjectMocks
    private AgendamentoService agendamentoService;

    private Agendamento agendamento;
    private Barbeiro barbeiro;
    private Servico servico;
    private Cliente cliente;

    @BeforeEach
    void setUp() {
        barbeiro = new Barbeiro();
        barbeiro.setId(1L);
        barbeiro.setNome("João Barbeiro");
        barbeiro.setEmail("joao@barbearia.com");

        servico = new Servico();
        servico.setId(1L);
        servico.setNome("Corte de Cabelo");
        servico.setPreco(50.0);
        servico.setDuracaoMinutos(30);

        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("Cliente Teste");
        cliente.setEmail("cliente@teste.com");

        agendamento = new Agendamento();
        agendamento.setId(1L);
        agendamento.setBarbeiro(barbeiro);
        agendamento.setServico(servico);
        agendamento.setCliente(cliente);
        agendamento.setDataHora(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0));
        agendamento.setPrecoOriginal(50.0);
        agendamento.setPrecoFinal(50.0);
        agendamento.setStatus("AGENDADO");
    }

    @Test
    @DisplayName("Deve buscar agendamento por ID com sucesso")
    void deveBuscarAgendamentoPorIdComSucesso() {
        when(agendamentoRepository.findById(1L)).thenReturn(Optional.of(agendamento));

        Optional<Agendamento> resultado = agendamentoService.buscarPorId(1L);

        assertTrue(resultado.isPresent());
        assertEquals("AGENDADO", resultado.get().getStatus());
        verify(agendamentoRepository).findById(1L);
    }

    @Test
    @DisplayName("Deve retornar vazio ao buscar agendamento por ID inexistente")
    void deveRetornarVazioAoBuscarAgendamentoPorIdInexistente() {
        when(agendamentoRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Agendamento> resultado = agendamentoService.buscarPorId(999L);

        assertFalse(resultado.isPresent());
        verify(agendamentoRepository).findById(999L);
    }

    @Test
    @DisplayName("Deve agendar com sucesso")
    void deveAgendarComSucesso() {
        when(barbeiroService.buscarPorId(1L)).thenReturn(Optional.of(barbeiro));
        when(servicoService.buscarPorId(1L)).thenReturn(Optional.of(servico));
        when(clienteService.buscarPorId(1L)).thenReturn(Optional.of(cliente));
        when(agendamentoRepository.existsByBarbeiroIdAndDataHoraAndStatusNot(anyLong(), any(LocalDateTime.class), anyString()))
                .thenReturn(false);
        when(agendamentoRepository.save(any(Agendamento.class))).thenReturn(agendamento);
        doNothing().when(emailService).enviarConfirmacaoAgendamento(any(Agendamento.class));

        Agendamento resultado = agendamentoService.agendar(agendamento);

        assertNotNull(resultado);
        verify(barbeiroService).buscarPorId(1L);
        verify(servicoService).buscarPorId(1L);
        verify(clienteService).buscarPorId(1L);
        verify(agendamentoRepository).existsByBarbeiroIdAndDataHoraAndStatusNot(1L, agendamento.getDataHora(), "CANCELADO");
        verify(agendamentoRepository).save(agendamento);
        verify(emailService).enviarConfirmacaoAgendamento(agendamento);
    }

    @Test
    @DisplayName("Deve lançar exceção ao agendar com barbeiro nulo")
    void deveLancarExcecaoAoAgendarComBarbeiroNulo() {
        agendamento.setBarbeiro(null);

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> agendamentoService.agendar(agendamento)
        );

        verify(barbeiroService, never()).buscarPorId(anyLong());
    }

    @Test
    @DisplayName("Deve lançar exceção ao agendar com barbeiro inexistente")
    void deveLancarExcecaoAoAgendarComBarbeiroInexistente() {
        when(barbeiroService.buscarPorId(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> agendamentoService.agendar(agendamento)
        );

        assertEquals("Barbeiro não encontrado", exception.getMessage());
        verify(barbeiroService).buscarPorId(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção ao agendar com serviço nulo")
    void deveLancarExcecaoAoAgendarComServicoNulo() {
        agendamento.setServico(null);
        when(barbeiroService.buscarPorId(1L)).thenReturn(Optional.of(barbeiro));

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> agendamentoService.agendar(agendamento)
        );

        verify(barbeiroService).buscarPorId(1L);
        verify(servicoService, never()).buscarPorId(anyLong());
    }

    @Test
    @DisplayName("Deve lançar exceção ao agendar com serviço inexistente")
    void deveLancarExcecaoAoAgendarComServicoInexistente() {
        when(barbeiroService.buscarPorId(1L)).thenReturn(Optional.of(barbeiro));
        when(servicoService.buscarPorId(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> agendamentoService.agendar(agendamento)
        );

        assertEquals("Serviço não encontrado", exception.getMessage());
        verify(servicoService).buscarPorId(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção ao agendar com cliente nulo")
    void deveLancarExcecaoAoAgendarComClienteNulo() {
        agendamento.setCliente(null);
        when(barbeiroService.buscarPorId(1L)).thenReturn(Optional.of(barbeiro));
        when(servicoService.buscarPorId(1L)).thenReturn(Optional.of(servico));

        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> agendamentoService.agendar(agendamento)
        );

        verify(clienteService, never()).buscarPorId(anyLong());
    }

    @Test
    @DisplayName("Deve lançar exceção ao agendar com cliente inexistente")
    void deveLancarExcecaoAoAgendarComClienteInexistente() {
        when(barbeiroService.buscarPorId(1L)).thenReturn(Optional.of(barbeiro));
        when(servicoService.buscarPorId(1L)).thenReturn(Optional.of(servico));
        when(clienteService.buscarPorId(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> agendamentoService.agendar(agendamento)
        );

        assertEquals("Cliente não encontrado", exception.getMessage());
        verify(clienteService).buscarPorId(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção ao agendar com conflito de horário")
    void deveLancarExcecaoAoAgendarComConflitoDeHorario() {
        when(barbeiroService.buscarPorId(1L)).thenReturn(Optional.of(barbeiro));
        when(servicoService.buscarPorId(1L)).thenReturn(Optional.of(servico));
        when(clienteService.buscarPorId(1L)).thenReturn(Optional.of(cliente));
        when(agendamentoRepository.existsByBarbeiroIdAndDataHoraAndStatusNot(anyLong(), any(LocalDateTime.class), anyString()))
                .thenReturn(true);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> agendamentoService.agendar(agendamento)
        );

        assertEquals("Barbeiro já possui agendamento neste horário", exception.getMessage());
        verify(agendamentoRepository).existsByBarbeiroIdAndDataHoraAndStatusNot(1L, agendamento.getDataHora(), "CANCELADO");
        verify(agendamentoRepository, never()).save(any(Agendamento.class));
    }

    @Test
    @DisplayName("Deve buscar agendamentos por email do barbeiro")
    void deveBuscarAgendamentosPorEmailDoBarbeiro() {
        List<Agendamento> agendamentos = List.of(agendamento);
        when(agendamentoRepository.findByBarbeiroEmail("joao@barbearia.com")).thenReturn(agendamentos);

        List<Agendamento> resultado = agendamentoService.findByBarbeiroEmail("joao@barbearia.com");

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(agendamentoRepository).findByBarbeiroEmail("joao@barbearia.com");
    }

    @Test
    @DisplayName("Deve listar todos os agendamentos")
    void deveListarTodosAgendamentos() {
        List<Agendamento> agendamentos = List.of(agendamento);
        when(agendamentoRepository.findAll()).thenReturn(agendamentos);

        List<Agendamento> resultado = agendamentoService.listarTodos();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(agendamentoRepository).findAll();
    }

    @Test
    @DisplayName("Deve listar todos os agendamentos com paginação")
    void deveListarTodosAgendamentosComPaginacao() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Agendamento> pagina = new PageImpl<>(List.of(agendamento));
        when(agendamentoRepository.findAll(pageable)).thenReturn(pagina);

        Page<Agendamento> resultado = agendamentoService.listarTodos(pageable);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        verify(agendamentoRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Deve listar agendamentos por barbeiro")
    void deveListarAgendamentosPorBarbeiro() {
        List<Agendamento> agendamentos = List.of(agendamento);
        when(agendamentoRepository.findByBarbeiroId(1L)).thenReturn(agendamentos);

        List<Agendamento> resultado = agendamentoService.listarPorBarbeiro(1L);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(agendamentoRepository).findByBarbeiroId(1L);
    }

    @Test
    @DisplayName("Deve listar agendamentos por cliente")
    void deveListarAgendamentosPorCliente() {
        List<Agendamento> agendamentos = List.of(agendamento);
        when(agendamentoRepository.findByClienteId(1L)).thenReturn(agendamentos);

        List<Agendamento> resultado = agendamentoService.listarPorCliente(1L);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(agendamentoRepository).findByClienteId(1L);
    }

    @Test
    @DisplayName("Deve verificar conflito de horário")
    void deveVerificarConflitoDeHorario() {
        when(agendamentoRepository.existsByBarbeiroIdAndDataHoraAndStatusNot(1L, agendamento.getDataHora(), "CANCELADO"))
                .thenReturn(true);

        boolean resultado = agendamentoService.existeConflitoHorario(1L, agendamento.getDataHora());

        assertTrue(resultado);
        verify(agendamentoRepository).existsByBarbeiroIdAndDataHoraAndStatusNot(1L, agendamento.getDataHora(), "CANCELADO");
    }

    @Test
    @DisplayName("Deve salvar agendamento com sucesso")
    void deveSalvarAgendamentoComSucesso() {
        when(agendamentoRepository.save(any(Agendamento.class))).thenReturn(agendamento);

        Agendamento resultado = agendamentoService.salvar(agendamento);

        assertNotNull(resultado);
        verify(agendamentoRepository).save(agendamento);
    }

    @Test
    @DisplayName("Deve salvar agendamento concluído e registrar corte")
    void deveSalvarAgendamentoConcluidoERegistrarCorte() {
        agendamento.setStatus("CONCLUIDO");
        agendamento.setPontoRegistrado(false);
        when(agendamentoRepository.save(any(Agendamento.class))).thenReturn(agendamento);
        when(fidelidadeService.registrarCorte(any(Cliente.class))).thenReturn(true);

        Agendamento resultado = agendamentoService.salvar(agendamento);

        assertNotNull(resultado);
        assertTrue(resultado.getPontoRegistrado());
        verify(fidelidadeService).registrarCorte(cliente);
        verify(agendamentoRepository).save(agendamento);
    }

    @Test
    @DisplayName("Deve salvar agendamento cancelado e enviar email")
    void deveSalvarAgendamentoCanceladoEEnviarEmail() {
        agendamento.setStatus("CANCELADO");
        when(agendamentoRepository.save(any(Agendamento.class))).thenReturn(agendamento);
        doNothing().when(emailService).enviarCancelamentoAgendamento(any(Agendamento.class));

        Agendamento resultado = agendamentoService.salvar(agendamento);

        assertNotNull(resultado);
        verify(emailService).enviarCancelamentoAgendamento(agendamento);
        verify(agendamentoRepository).save(agendamento);
    }

    @Test
    @DisplayName("Deve buscar agendamentos por barbeiro e período")
    void deveBuscarAgendamentosPorBarbeiroEPeriodo() {
        LocalDateTime inicio = LocalDateTime.now();
        LocalDateTime fim = LocalDateTime.now().plusDays(7);
        List<Agendamento> agendamentos = List.of(agendamento);
        when(agendamentoRepository.findByBarbeiroIdAndPeriodo(1L, inicio, fim)).thenReturn(agendamentos);

        List<Agendamento> resultado = agendamentoService.findByBarbeiroIdAndPeriodo(1L, inicio, fim);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(agendamentoRepository).findByBarbeiroIdAndPeriodo(1L, inicio, fim);
    }

    @Test
    @DisplayName("Deve buscar agendamentos por status")
    void deveBuscarAgendamentosPorStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Agendamento> pagina = new PageImpl<>(List.of(agendamento));
        when(agendamentoRepository.findByStatus("AGENDADO", pageable)).thenReturn(pagina);

        Page<Agendamento> resultado = agendamentoService.findByStatus("AGENDADO", pageable);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        verify(agendamentoRepository).findByStatus("AGENDADO", pageable);
    }

    @Test
    @DisplayName("Deve buscar agendamentos por período")
    void deveBuscarAgendamentosPorPeriodo() {
        LocalDateTime inicio = LocalDateTime.now();
        LocalDateTime fim = LocalDateTime.now().plusDays(7);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Agendamento> pagina = new PageImpl<>(List.of(agendamento));
        when(agendamentoRepository.findByDataHoraBetween(inicio, fim, pageable)).thenReturn(pagina);

        Page<Agendamento> resultado = agendamentoService.findByDataHoraBetween(inicio, fim, pageable);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        verify(agendamentoRepository).findByDataHoraBetween(inicio, fim, pageable);
    }

    @Test
    @DisplayName("Deve buscar agendamentos por período e status")
    void deveBuscarAgendamentosPorPeriodoEStatus() {
        LocalDateTime inicio = LocalDateTime.now();
        LocalDateTime fim = LocalDateTime.now().plusDays(7);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Agendamento> pagina = new PageImpl<>(List.of(agendamento));
        when(agendamentoRepository.findByDataHoraBetweenAndStatus(inicio, fim, "AGENDADO", pageable))
                .thenReturn(pagina);

        Page<Agendamento> resultado = agendamentoService.findByDataHoraBetweenAndStatus(inicio, fim, "AGENDADO", pageable);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        verify(agendamentoRepository).findByDataHoraBetweenAndStatus(inicio, fim, "AGENDADO", pageable);
    }

    @Test
    @DisplayName("Deve cancelar agendamentos por data")
    void deveCancelarAgendamentosPorData() {
        LocalDate data = LocalDate.now().plusDays(1);
        List<Agendamento> agendamentos = List.of(agendamento);
        when(agendamentoRepository.findByDataBetweenAndStatusAtivo(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(agendamentos);
        when(agendamentoRepository.save(any(Agendamento.class))).thenReturn(agendamento);
        doNothing().when(emailService).enviarCancelamentoFeriado(any(Agendamento.class), anyString(), any(LocalDate.class));
        doNothing().when(emailService).enviarNotificacaoBarbeiro(any(Agendamento.class), anyString(), any(LocalDate.class));

        List<Agendamento> resultado = agendamentoService.cancelarAgendamentosPorData(data, "Feriado");

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("CANCELADO", agendamento.getStatus());
        verify(agendamentoRepository).save(agendamento);
        verify(emailService).enviarCancelamentoFeriado(agendamento, "Feriado", data);
        verify(emailService).enviarNotificacaoBarbeiro(agendamento, "Feriado", data);
    }
}
