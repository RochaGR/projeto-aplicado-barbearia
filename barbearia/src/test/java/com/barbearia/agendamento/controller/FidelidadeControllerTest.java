package com.barbearia.agendamento.controller;

import com.barbearia.agendamento.model.CartaoFidelidade;
import com.barbearia.agendamento.model.Cliente;
import com.barbearia.agendamento.model.ConfiguracaoFidelidade;
import com.barbearia.agendamento.model.DescontoFidelidade;
import com.barbearia.agendamento.service.ClienteService;
import com.barbearia.agendamento.service.FidelidadeService;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários do FidelidadeController")
class FidelidadeControllerTest {

    @Mock
    private FidelidadeService fidelidadeService;

    @Mock
    private ClienteService clienteService;

    @Mock
    private Model model;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private FidelidadeController fidelidadeController;

    private Cliente cliente;
    private CartaoFidelidade cartao;
    private ConfiguracaoFidelidade config;
    private DescontoFidelidade desconto;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setEmail("cliente@teste.com");
        cliente.setNome("Cliente Teste");

        cartao = new CartaoFidelidade();
        cartao.setId(1L);
        cartao.setCliente(cliente);
        cartao.setPontos(3);
        cartao.setTotalCortesRealizados(8);

        config = new ConfiguracaoFidelidade();
        config.setId(1L);
        config.setPercentualDesconto(40.0);
        config.setCortesParaDesconto(5);

        desconto = new DescontoFidelidade();
        desconto.setId(1L);
        desconto.setCliente(cliente);
        desconto.setPercentualDesconto(40.0);
        desconto.setUtilizado(false);
    }

    @Test
    @DisplayName("Deve exibir cartão de fidelidade existente")
    void deveExibirCartaoFidelidadeExistente() {
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("cliente@teste.com");
        when(clienteService.buscarPorEmail("cliente@teste.com")).thenReturn(Optional.of(cliente));
        when(fidelidadeService.buscarConfiguracaoAtual()).thenReturn(config);
        when(fidelidadeService.buscarCartao(1L)).thenReturn(Optional.of(cartao));
        when(fidelidadeService.buscarDescontoDisponivel(1L)).thenReturn(Optional.of(desconto));

        String view = fidelidadeController.verCartao(model, authentication);

        assertEquals("fidelidade", view);
        verify(model).addAttribute("cartao", cartao);
        verify(model).addAttribute("config", config);
        verify(model).addAttribute("temDesconto", true);
        verify(model).addAttribute("percentualDesconto", 40.0);
    }

    @Test
    @DisplayName("Deve criar novo cartão quando não existir")
    void deveCriarNovoCartaoQuandoNaoExistir() {
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("cliente@teste.com");
        when(clienteService.buscarPorEmail("cliente@teste.com")).thenReturn(Optional.of(cliente));
        when(fidelidadeService.buscarConfiguracaoAtual()).thenReturn(config);
        when(fidelidadeService.buscarCartao(1L)).thenReturn(Optional.empty());
        when(fidelidadeService.buscarDescontoDisponivel(1L)).thenReturn(Optional.empty());

        String view = fidelidadeController.verCartao(model, authentication);

        assertEquals("fidelidade", view);
        verify(model).addAttribute(eq("cartao"), any(CartaoFidelidade.class));
        verify(model).addAttribute("config", config);
        verify(model).addAttribute("temDesconto", false);
    }

    @Test
    @DisplayName("Deve exibir cartão sem desconto disponível")
    void deveExibirCartaoSemDescontoDisponivel() {
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("cliente@teste.com");
        when(clienteService.buscarPorEmail("cliente@teste.com")).thenReturn(Optional.of(cliente));
        when(fidelidadeService.buscarConfiguracaoAtual()).thenReturn(config);
        when(fidelidadeService.buscarCartao(1L)).thenReturn(Optional.of(cartao));
        when(fidelidadeService.buscarDescontoDisponivel(1L)).thenReturn(Optional.empty());

        String view = fidelidadeController.verCartao(model, authentication);

        assertEquals("fidelidade", view);
        verify(model).addAttribute("cartao", cartao);
        verify(model).addAttribute("config", config);
        verify(model).addAttribute("temDesconto", false);
    }

    @Test
    @DisplayName("Deve lançar erro quando cliente não encontrado")
    void deveLancarErroQuandoClienteNaoEncontrado() {
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("cliente@teste.com");
        when(clienteService.buscarPorEmail("cliente@teste.com")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            fidelidadeController.verCartao(model, authentication);
        });
    }

    @Test
    @DisplayName("Deve exibir configuração de fidelidade atual")
    void deveExibirConfiguracaoFidelidadeAtual() {
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("cliente@teste.com");
        when(clienteService.buscarPorEmail("cliente@teste.com")).thenReturn(Optional.of(cliente));
        when(fidelidadeService.buscarConfiguracaoAtual()).thenReturn(config);
        when(fidelidadeService.buscarCartao(1L)).thenReturn(Optional.of(cartao));
        when(fidelidadeService.buscarDescontoDisponivel(1L)).thenReturn(Optional.empty());

        String view = fidelidadeController.verCartao(model, authentication);

        assertEquals("fidelidade", view);
        verify(model).addAttribute("config", config);
        assertEquals(40.0, config.getPercentualDesconto());
        assertEquals(5, config.getCortesParaDesconto());
    }

    @Test
    @DisplayName("Deve exibir pontos do cartão corretamente")
    void deveExibirPontosDoCartaoCorretamente() {
        cartao.setPontos(4);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("cliente@teste.com");
        when(clienteService.buscarPorEmail("cliente@teste.com")).thenReturn(Optional.of(cliente));
        when(fidelidadeService.buscarConfiguracaoAtual()).thenReturn(config);
        when(fidelidadeService.buscarCartao(1L)).thenReturn(Optional.of(cartao));
        when(fidelidadeService.buscarDescontoDisponivel(1L)).thenReturn(Optional.empty());

        String view = fidelidadeController.verCartao(model, authentication);

        assertEquals("fidelidade", view);
        verify(model).addAttribute("cartao", cartao);
        assertEquals(4, cartao.getPontos());
    }

    @Test
    @DisplayName("Deve exibir total de cortes realizados")
    void deveExibirTotalDeCortesRealizados() {
        cartao.setTotalCortesRealizados(12);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("cliente@teste.com");
        when(clienteService.buscarPorEmail("cliente@teste.com")).thenReturn(Optional.of(cliente));
        when(fidelidadeService.buscarConfiguracaoAtual()).thenReturn(config);
        when(fidelidadeService.buscarCartao(1L)).thenReturn(Optional.of(cartao));
        when(fidelidadeService.buscarDescontoDisponivel(1L)).thenReturn(Optional.empty());

        String view = fidelidadeController.verCartao(model, authentication);

        assertEquals("fidelidade", view);
        verify(model).addAttribute("cartao", cartao);
        assertEquals(12, cartao.getTotalCortesRealizados());
    }
}
