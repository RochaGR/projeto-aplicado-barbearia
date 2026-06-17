package com.barbearia.agendamento.controller;

import com.barbearia.agendamento.model.Agendamento;
import com.barbearia.agendamento.model.Cliente;
import com.barbearia.agendamento.repository.DescontoFidelidadeRepository;
import com.barbearia.agendamento.service.AgendamentoService;
import com.barbearia.agendamento.service.ClienteService;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários do HistoricoController")
class HistoricoControllerTest {

    @Mock
    private AgendamentoService agendamentoService;

    @Mock
    private ClienteService clienteService;

    @Mock
    private DescontoFidelidadeRepository descontoFidelidadeRepository;

    @Mock
    private Model model;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private HistoricoController historicoController;

    private Cliente cliente;
    private Agendamento agendamento1;
    private Agendamento agendamento2;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setEmail("cliente@teste.com");
        cliente.setNome("Cliente Teste");

        agendamento1 = new Agendamento();
        agendamento1.setId(1L);
        agendamento1.setCliente(cliente);
        agendamento1.setDataHora(LocalDateTime.now().minusDays(1));
        agendamento1.setStatus("CONCLUIDO");

        agendamento2 = new Agendamento();
        agendamento2.setId(2L);
        agendamento2.setCliente(cliente);
        agendamento2.setDataHora(LocalDateTime.now().minusDays(5));
        agendamento2.setStatus("CANCELADO");
    }

    @Test
    @DisplayName("Deve exibir histórico completo do cliente")
    void deveExibirHistoricoCompletoDoCliente() {
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("cliente@teste.com");
        when(clienteService.buscarPorEmail("cliente@teste.com")).thenReturn(java.util.Optional.of(cliente));
        when(agendamentoService.listarPorCliente(1L)).thenReturn(List.of(agendamento1, agendamento2));
        when(descontoFidelidadeRepository.somarEconomiaByClienteId(1L)).thenReturn(50.0);

        String view = historicoController.historico(null, null, null, model, authentication);

        assertEquals("historico", view);
        verify(model).addAttribute(eq("historico"), any());
        verify(model).addAttribute(eq("totalAgendamentos"), eq(2L));
        verify(model).addAttribute(eq("concluidos"), eq(1L));
        verify(model).addAttribute(eq("cancelados"), eq(1L));
        verify(model).addAttribute(eq("totalEconomizado"), eq(50.0));
    }

    @Test
    @DisplayName("Deve filtrar histórico por data início")
    void deveFiltrarHistoricoPorDataInicio() {
        LocalDate dataInicio = LocalDate.now().minusDays(3);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("cliente@teste.com");
        when(clienteService.buscarPorEmail("cliente@teste.com")).thenReturn(java.util.Optional.of(cliente));
        when(agendamentoService.listarPorCliente(1L)).thenReturn(List.of(agendamento1));
        when(descontoFidelidadeRepository.somarEconomiaByClienteId(1L)).thenReturn(0.0);

        String view = historicoController.historico(dataInicio, null, null, model, authentication);

        assertEquals("historico", view);
        verify(model).addAttribute("dataInicio", dataInicio);
    }

    @Test
    @DisplayName("Deve filtrar histórico por data fim")
    void deveFiltrarHistoricoPorDataFim() {
        LocalDate dataFim = LocalDate.now().minusDays(2);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("cliente@teste.com");
        when(clienteService.buscarPorEmail("cliente@teste.com")).thenReturn(java.util.Optional.of(cliente));
        when(agendamentoService.listarPorCliente(1L)).thenReturn(List.of(agendamento2));
        when(descontoFidelidadeRepository.somarEconomiaByClienteId(1L)).thenReturn(0.0);

        String view = historicoController.historico(null, dataFim, null, model, authentication);

        assertEquals("historico", view);
        verify(model).addAttribute("dataFim", dataFim);
    }

    @Test
    @DisplayName("Deve filtrar histórico por status")
    void deveFiltrarHistoricoPorStatus() {
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("cliente@teste.com");
        when(clienteService.buscarPorEmail("cliente@teste.com")).thenReturn(java.util.Optional.of(cliente));
        when(agendamentoService.listarPorCliente(1L)).thenReturn(List.of(agendamento1));
        when(descontoFidelidadeRepository.somarEconomiaByClienteId(1L)).thenReturn(0.0);

        String view = historicoController.historico(null, null, "CONCLUIDO", model, authentication);

        assertEquals("historico", view);
        verify(model).addAttribute("statusFiltro", "CONCLUIDO");
        verify(model).addAttribute("concluidos", 1L);
    }

    @Test
    @DisplayName("Deve calcular estatísticas corretamente")
    void deveCalcularEstatisticasCorretamente() {
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("cliente@teste.com");
        when(clienteService.buscarPorEmail("cliente@teste.com")).thenReturn(java.util.Optional.of(cliente));
        when(agendamentoService.listarPorCliente(1L)).thenReturn(List.of(agendamento1, agendamento2));
        when(descontoFidelidadeRepository.somarEconomiaByClienteId(1L)).thenReturn(100.0);

        String view = historicoController.historico(null, null, null, model, authentication);

        assertEquals("historico", view);
        verify(model).addAttribute("totalAgendamentos", 2L);
        verify(model).addAttribute("concluidos", 1L);
        verify(model).addAttribute("cancelados", 1L);
        verify(model).addAttribute("totalEconomizado", 100.0);
    }

    @Test
    @DisplayName("Deve retornar zero economia quando não houver descontos")
    void deveRetornarZeroEconomiaQuandoNaoHouverDescontos() {
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("cliente@teste.com");
        when(clienteService.buscarPorEmail("cliente@teste.com")).thenReturn(java.util.Optional.of(cliente));
        when(agendamentoService.listarPorCliente(1L)).thenReturn(List.of(agendamento1));
        when(descontoFidelidadeRepository.somarEconomiaByClienteId(1L)).thenReturn(null);

        String view = historicoController.historico(null, null, null, model, authentication);

        assertEquals("historico", view);
        verify(model).addAttribute("totalEconomizado", 0.0);
    }

    @Test
    @DisplayName("Deve ordenar histórico por data decrescente")
    void deveOrdenarHistoricoPorDataDecrescente() {
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("cliente@teste.com");
        when(clienteService.buscarPorEmail("cliente@teste.com")).thenReturn(java.util.Optional.of(cliente));
        when(agendamentoService.listarPorCliente(1L)).thenReturn(List.of(agendamento1, agendamento2));
        when(descontoFidelidadeRepository.somarEconomiaByClienteId(1L)).thenReturn(0.0);

        String view = historicoController.historico(null, null, null, model, authentication);

        assertEquals("historico", view);
        verify(model).addAttribute(eq("historico"), any());
    }
}
