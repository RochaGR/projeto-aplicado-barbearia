package com.barbearia.agendamento.controller;

import com.barbearia.agendamento.model.*;
import com.barbearia.agendamento.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários do AgendamentoController")
class AgendamentoControllerTest {

    @Mock
    private AgendamentoService agendamentoService;

    @Mock
    private ServicoService servicoService;

    @Mock
    private BarbeiroService barbeiroService;

    @Mock
    private ClienteService clienteService;

    @Mock
    private FidelidadeService fidelidadeService;

    @Mock
    private Model model;

    @Mock
    private Authentication authentication;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private RedirectAttributes redirectAttributes;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private AgendamentoController agendamentoController;

    private Agendamento agendamento;
    private Cliente cliente;
    private Barbeiro barbeiro;
    private Servico servico;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setEmail("cliente@teste.com");
        cliente.setNome("Cliente Teste");

        barbeiro = new Barbeiro();
        barbeiro.setId(1L);
        barbeiro.setEmail("barbeiro@teste.com");
        barbeiro.setNome("Barbeiro Teste");

        servico = new Servico();
        servico.setId(1L);
        servico.setNome("Corte de Cabelo");
        servico.setPreco(50.0);
        servico.setDuracaoMinutos(30);

        agendamento = new Agendamento();
        agendamento.setId(1L);
        agendamento.setCliente(cliente);
        agendamento.setBarbeiro(barbeiro);
        agendamento.setServico(servico);
        agendamento.setDataHora(LocalDateTime.now().plusDays(1));
        agendamento.setStatus("AGENDADO");
    }

    @Test
    @DisplayName("Deve mostrar formulário de agendamento")
    void deveMostrarFormularioAgendamento() {
        when(servicoService.listarTodos()).thenReturn(List.of(servico));
        when(barbeiroService.listarTodos()).thenReturn(List.of(barbeiro));
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("cliente@teste.com");
        when(clienteService.buscarPorEmail("cliente@teste.com")).thenReturn(Optional.of(cliente));

        String view = agendamentoController.mostrarFormularioAgendamento(model, authentication);

        assertEquals("agendamento-cliente-form", view);
        verify(model).addAttribute(eq("agendamento"), any(Agendamento.class));
    }

    @Test
    @DisplayName("Deve salvar agendamento com sucesso")
    void deveSalvarAgendamentoComSucesso() {
        when(bindingResult.hasErrors()).thenReturn(false);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("cliente@teste.com");
        when(clienteService.buscarPorEmail("cliente@teste.com")).thenReturn(Optional.of(cliente));
        when(agendamentoService.agendar(any(Agendamento.class))).thenReturn(agendamento);
        when(fidelidadeService.buscarDescontoDisponivel(anyLong())).thenReturn(Optional.empty());
        when(fidelidadeService.aplicarDesconto(anyLong(), anyDouble())).thenReturn(50.0);

        String view = agendamentoController.salvarAgendamento(agendamento, bindingResult, model, redirectAttributes, authentication);

        assertEquals("redirect:/agendamentos/confirmacao?agendamentoId=1", view);
        verify(redirectAttributes).addFlashAttribute("sucesso", "Agendamento realizado com sucesso!");
    }

    @Test
    @DisplayName("Deve retornar formulário com erro de validação")
    void deveRetornarFormularioComErroValidacao() {
        when(bindingResult.hasErrors()).thenReturn(true);
        when(servicoService.listarTodos()).thenReturn(List.of(servico));
        when(barbeiroService.listarTodos()).thenReturn(List.of(barbeiro));

        String view = agendamentoController.salvarAgendamento(agendamento, bindingResult, model, redirectAttributes, authentication);

        assertEquals("agendamento-cliente-form", view);
    }

    @Test
    @DisplayName("Deve mostrar confirmação de agendamento")
    void deveMostrarConfirmacaoAgendamento() {
        when(agendamentoService.buscarPorId(1L)).thenReturn(Optional.of(agendamento));

        String view = agendamentoController.mostrarConfirmacao(1L, model);

        assertEquals("confirmacao-agendamento", view);
        verify(model).addAttribute("agendamento", agendamento);
    }

    @Test
    @DisplayName("Deve listar agendamentos do cliente")
    void deveListarAgendamentosDoCliente() {
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("cliente@teste.com");
        when(clienteService.buscarPorEmail("cliente@teste.com")).thenReturn(Optional.of(cliente));
        when(agendamentoService.listarPorCliente(1L)).thenReturn(List.of(agendamento));
        when(fidelidadeService.buscarCartao(1L)).thenReturn(Optional.empty());
        when(fidelidadeService.buscarDescontoDisponivel(1L)).thenReturn(Optional.empty());
        
        ConfiguracaoFidelidade config = new ConfiguracaoFidelidade();
        config.setCortesParaDesconto(5);
        config.setPercentualDesconto(40.0);
        when(fidelidadeService.buscarConfiguracaoAtual()).thenReturn(config);

        String view = agendamentoController.listarAgendamentos(null, model, authentication);

        assertEquals("lista-agendamentos", view);
        verify(model).addAttribute("agendamentos", List.of(agendamento));
    }

    @Test
    @DisplayName("Deve listar agendamentos do barbeiro")
    void deveListarAgendamentosDoBarbeiro() {
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("barbeiro@teste.com");
        when(agendamentoService.findByBarbeiroEmail("barbeiro@teste.com")).thenReturn(List.of(agendamento));

        String view = agendamentoController.listarAgendamentosBarbeiro(null, model, authentication);

        assertEquals("lista-agendamentos-barbeiro", view);
        verify(model).addAttribute("agendamentos", List.of(agendamento));
    }

    @Test
    @DisplayName("Deve concluir agendamento com sucesso")
    void deveConcluirAgendamentoComSucesso() {
        when(agendamentoService.buscarPorId(1L)).thenReturn(Optional.of(agendamento));
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("barbeiro@teste.com");

        String view = agendamentoController.concluirAgendamento(1L, authentication, redirectAttributes);

        assertEquals("redirect:/agendamentos/barbeiro", view);
        verify(redirectAttributes).addFlashAttribute("sucesso", "Agendamento marcado como concluído!");
        verify(agendamentoService).salvar(any(Agendamento.class));
    }

    @Test
    @DisplayName("Deve cancelar agendamento do barbeiro com sucesso")
    void deveCancelarAgendamentoBarbeiroComSucesso() {
        when(agendamentoService.buscarPorId(1L)).thenReturn(Optional.of(agendamento));
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("barbeiro@teste.com");

        String view = agendamentoController.cancelarAgendamentoBarbeiro(1L, authentication, redirectAttributes);

        assertEquals("redirect:/agendamentos/barbeiro", view);
        verify(redirectAttributes).addFlashAttribute("sucesso", "Agendamento cancelado com sucesso!");
    }

    @Test
    @DisplayName("Deve cancelar agendamento do cliente com sucesso")
    void deveCancelarAgendamentoClienteComSucesso() {
        agendamento.setDataHora(LocalDateTime.now().plusDays(2));
        when(agendamentoService.buscarPorId(1L)).thenReturn(Optional.of(agendamento));
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("cliente@teste.com");

        String view = agendamentoController.cancelarAgendamentoCliente(1L, authentication, redirectAttributes);

        assertEquals("redirect:/agendamentos", view);
        verify(redirectAttributes).addFlashAttribute("sucesso", "Agendamento cancelado com sucesso!");
    }

    @Test
    @DisplayName("Deve mostrar formulário de edição")
    void deveMostrarFormularioEdicao() {
        when(agendamentoService.buscarPorId(1L)).thenReturn(Optional.of(agendamento));
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("cliente@teste.com");
        when(servicoService.listarTodos()).thenReturn(List.of(servico));
        when(barbeiroService.listarTodos()).thenReturn(List.of(barbeiro));

        String view = agendamentoController.mostrarFormularioEdicao(1L, model, authentication);

        assertEquals("agendamento-cliente-form", view);
        verify(model).addAttribute("agendamento", agendamento);
    }

    @Test
    @DisplayName("Deve lançar erro ao tentar editar agendamento de outro cliente")
    void deveLancarErroAoEditarAgendamentoDeOutroCliente() {
        when(agendamentoService.buscarPorId(1L)).thenReturn(Optional.of(agendamento));
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("outro@teste.com");

        String view = agendamentoController.mostrarFormularioEdicao(1L, model, authentication);

        assertEquals("redirect:/agendamentos", view);
        verify(model).addAttribute("erro", "Você não tem permissão para editar este agendamento");
    }
}
