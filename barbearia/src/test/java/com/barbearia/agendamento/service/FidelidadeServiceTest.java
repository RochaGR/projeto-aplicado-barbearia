package com.barbearia.agendamento.service;

import com.barbearia.agendamento.model.CartaoFidelidade;
import com.barbearia.agendamento.model.Cliente;
import com.barbearia.agendamento.model.ConfiguracaoFidelidade;
import com.barbearia.agendamento.model.DescontoFidelidade;
import com.barbearia.agendamento.repository.CartaoFidelidadeRepository;
import com.barbearia.agendamento.repository.ConfiguracaoFidelidadeRepository;
import com.barbearia.agendamento.repository.DescontoFidelidadeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FidelidadeServiceTest {

    @Mock
    private CartaoFidelidadeRepository cartaoRepository;

    @Mock
    private DescontoFidelidadeRepository descontoRepository;

    @Mock
    private ConfiguracaoFidelidadeRepository configRepository;

    @InjectMocks
    private FidelidadeService fidelidadeService;

    private Cliente cliente;
    private CartaoFidelidade cartao;
    private ConfiguracaoFidelidade config;
    private DescontoFidelidade desconto;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setEmail("cliente@test.com");

        cartao = new CartaoFidelidade();
        cartao.setId(1L);
        cartao.setCliente(cliente);
        cartao.setPontos(0);
        cartao.setTotalCortesRealizados(0);

        config = new ConfiguracaoFidelidade();
        config.setId(1L);
        config.setPercentualDesconto(40.0);
        config.setCortesParaDesconto(5);

        desconto = new DescontoFidelidade();
        desconto.setId(1L);
        desconto.setCliente(cliente);
        desconto.setPercentualDesconto(40.0);
        desconto.setUtilizado(false);
        desconto.setDataGeracao(LocalDateTime.now());
    }

    @Test
    @DisplayName("Deve registrar corte em novo cartão sem gerar desconto")
    void testRegistrarCorte_NovoCartao_SemDesconto() {
        when(configRepository.findById(1L)).thenReturn(Optional.empty());
        when(configRepository.save(any(ConfiguracaoFidelidade.class))).thenReturn(config);
        when(cartaoRepository.findByClienteId(1L)).thenReturn(Optional.empty());
        when(cartaoRepository.save(any(CartaoFidelidade.class))).thenReturn(cartao);

        boolean resultado = fidelidadeService.registrarCorte(cliente);

        assertFalse(resultado);
        verify(cartaoRepository, times(2)).save(any(CartaoFidelidade.class));
        verify(configRepository).save(any(ConfiguracaoFidelidade.class));
    }

    @Test
    @DisplayName("Deve registrar corte em cartão existente sem atingir limite de desconto")
    void testRegistrarCorte_CartaoExistente_SemDesconto() {
        CartaoFidelidade cartaoExistente = new CartaoFidelidade();
        cartaoExistente.setId(1L);
        cartaoExistente.setCliente(cliente);
        cartaoExistente.setPontos(2);
        cartaoExistente.setTotalCortesRealizados(5);

        when(configRepository.findById(1L)).thenReturn(Optional.of(config));
        when(cartaoRepository.findByClienteId(1L)).thenReturn(Optional.of(cartaoExistente));
        when(cartaoRepository.save(any(CartaoFidelidade.class))).thenReturn(cartaoExistente);

        boolean resultado = fidelidadeService.registrarCorte(cliente);

        assertFalse(resultado);
        assertEquals(3, cartaoExistente.getPontos());
        assertEquals(6, cartaoExistente.getTotalCortesRealizados());
        verify(cartaoRepository).save(any(CartaoFidelidade.class));
    }

    @Test
    @DisplayName("Deve gerar desconto ao atingir limite de cortes")
    void testRegistrarCorte_AtingeLimite_GeraDesconto() {
        CartaoFidelidade cartaoExistente = new CartaoFidelidade();
        cartaoExistente.setId(1L);
        cartaoExistente.setCliente(cliente);
        cartaoExistente.setPontos(4);
        cartaoExistente.setTotalCortesRealizados(4);

        when(configRepository.findById(1L)).thenReturn(Optional.of(config));
        when(cartaoRepository.findByClienteId(1L)).thenReturn(Optional.of(cartaoExistente));
        when(cartaoRepository.save(any(CartaoFidelidade.class))).thenReturn(cartaoExistente);
        when(descontoRepository.findByClienteIdAndUtilizadoFalse(1L)).thenReturn(Optional.empty());
        when(descontoRepository.save(any(DescontoFidelidade.class))).thenReturn(desconto);

        boolean resultado = fidelidadeService.registrarCorte(cliente);

        assertTrue(resultado);
        assertEquals(0, cartaoExistente.getPontos());
        assertEquals(5, cartaoExistente.getTotalCortesRealizados());
        verify(descontoRepository).save(any(DescontoFidelidade.class));
        verify(cartaoRepository, times(2)).save(any(CartaoFidelidade.class));
    }

    @Test
    @DisplayName("Não deve gerar novo desconto se já existir um disponível")
    void testRegistrarCorte_JaTemDesconto_NaoGeraNovo() {
        CartaoFidelidade cartaoExistente = new CartaoFidelidade();
        cartaoExistente.setId(1L);
        cartaoExistente.setCliente(cliente);
        cartaoExistente.setPontos(4);
        cartaoExistente.setTotalCortesRealizados(4);

        when(configRepository.findById(1L)).thenReturn(Optional.of(config));
        when(cartaoRepository.findByClienteId(1L)).thenReturn(Optional.of(cartaoExistente));
        when(cartaoRepository.save(any(CartaoFidelidade.class))).thenReturn(cartaoExistente);
        when(descontoRepository.findByClienteIdAndUtilizadoFalse(1L)).thenReturn(Optional.of(desconto));

        boolean resultado = fidelidadeService.registrarCorte(cliente);

        assertTrue(resultado);
        assertEquals(0, cartaoExistente.getPontos());
        verify(descontoRepository, never()).save(any(DescontoFidelidade.class));
    }

    @Test
    @DisplayName("Deve buscar configuração existente")
    void testBuscarConfiguracaoAtual_Existente() {
        when(configRepository.findById(1L)).thenReturn(Optional.of(config));

        ConfiguracaoFidelidade resultado = fidelidadeService.buscarConfiguracaoAtual();

        assertNotNull(resultado);
        assertEquals(40.0, resultado.getPercentualDesconto());
        assertEquals(5, resultado.getCortesParaDesconto());
        verify(configRepository).findById(1L);
    }

    @Test
    @DisplayName("Deve criar nova configuração se não existir")
    void testBuscarConfiguracaoAtual_NaoExistente_CriaNova() {
        when(configRepository.findById(1L)).thenReturn(Optional.empty());
        when(configRepository.save(any(ConfiguracaoFidelidade.class))).thenReturn(config);

        ConfiguracaoFidelidade resultado = fidelidadeService.buscarConfiguracaoAtual();

        assertNotNull(resultado);
        verify(configRepository).save(any(ConfiguracaoFidelidade.class));
    }

    @Test
    @DisplayName("Deve buscar cartão existente por cliente")
    void testBuscarCartao_Existente() {
        when(cartaoRepository.findByClienteId(1L)).thenReturn(Optional.of(cartao));

        Optional<CartaoFidelidade> resultado = fidelidadeService.buscarCartao(1L);

        assertTrue(resultado.isPresent());
        assertEquals(1L, resultado.get().getId());
        verify(cartaoRepository).findByClienteId(1L);
    }

    @Test
    @DisplayName("Deve retornar vazio quando cartão não existe")
    void testBuscarCartao_NaoExistente() {
        when(cartaoRepository.findByClienteId(1L)).thenReturn(Optional.empty());

        Optional<CartaoFidelidade> resultado = fidelidadeService.buscarCartao(1L);

        assertFalse(resultado.isPresent());
        verify(cartaoRepository).findByClienteId(1L);
    }

    @Test
    @DisplayName("Deve buscar desconto disponível não utilizado")
    void testBuscarDescontoDisponivel_Existente() {
        when(descontoRepository.findByClienteIdAndUtilizadoFalse(1L)).thenReturn(Optional.of(desconto));

        Optional<DescontoFidelidade> resultado = fidelidadeService.buscarDescontoDisponivel(1L);

        assertTrue(resultado.isPresent());
        assertFalse(resultado.get().isUtilizado());
        verify(descontoRepository).findByClienteIdAndUtilizadoFalse(1L);
    }

    @Test
    @DisplayName("Deve retornar vazio quando não há desconto disponível")
    void testBuscarDescontoDisponivel_NaoExistente() {
        when(descontoRepository.findByClienteIdAndUtilizadoFalse(1L)).thenReturn(Optional.empty());

        Optional<DescontoFidelidade> resultado = fidelidadeService.buscarDescontoDisponivel(1L);

        assertFalse(resultado.isPresent());
        verify(descontoRepository).findByClienteIdAndUtilizadoFalse(1L);
    }

    @Test
    @DisplayName("Deve calcular economia total com descontos")
    void testEconomiaTotal_ComDescontos() {
        when(descontoRepository.somarEconomiaByClienteId(1L)).thenReturn(50.0);

        Double resultado = fidelidadeService.economiaTotal(1L);

        assertEquals(50.0, resultado);
        verify(descontoRepository).somarEconomiaByClienteId(1L);
    }

    @Test
    @DisplayName("Deve retornar zero quando não há economia")
    void testEconomiaTotal_SemDescontos() {
        when(descontoRepository.somarEconomiaByClienteId(1L)).thenReturn(null);

        Double resultado = fidelidadeService.economiaTotal(1L);

        assertEquals(0.0, resultado);
        verify(descontoRepository).somarEconomiaByClienteId(1L);
    }

    @Test
    @DisplayName("Deve aplicar desconto disponível e marcar como utilizado")
    void testAplicarDesconto_ComDescontoDisponivel() {
        DescontoFidelidade descontoDisponivel = new DescontoFidelidade();
        descontoDisponivel.setId(1L);
        descontoDisponivel.setCliente(cliente);
        descontoDisponivel.setPercentualDesconto(40.0);
        descontoDisponivel.setUtilizado(false);
        descontoDisponivel.setDataGeracao(LocalDateTime.now());

        when(descontoRepository.findByClienteIdAndUtilizadoFalse(1L)).thenReturn(Optional.of(descontoDisponivel));
        when(descontoRepository.save(any(DescontoFidelidade.class))).thenReturn(descontoDisponivel);

        Double precoOriginal = 100.0;
        Double resultado = fidelidadeService.aplicarDesconto(1L, precoOriginal);

        assertEquals(60.0, resultado);
        assertTrue(descontoDisponivel.isUtilizado());
        assertNotNull(descontoDisponivel.getDataUtilizacao());
        assertEquals(40.0, descontoDisponivel.getValorEconomizado());
        verify(descontoRepository).save(any(DescontoFidelidade.class));
    }

    @Test
    @DisplayName("Deve retornar preço original quando não há desconto disponível")
    void testAplicarDesconto_SemDescontoDisponivel() {
        when(descontoRepository.findByClienteIdAndUtilizadoFalse(1L)).thenReturn(Optional.empty());

        Double precoOriginal = 100.0;
        Double resultado = fidelidadeService.aplicarDesconto(1L, precoOriginal);

        assertEquals(100.0, resultado);
        verify(descontoRepository, never()).save(any(DescontoFidelidade.class));
    }

    @Test
    @DisplayName("Deve calcular desconto corretamente com percentual diferente")
    void testAplicarDesconto_CalculoCorreto() {
        DescontoFidelidade descontoVinte = new DescontoFidelidade();
        descontoVinte.setId(1L);
        descontoVinte.setCliente(cliente);
        descontoVinte.setPercentualDesconto(20.0);
        descontoVinte.setUtilizado(false);
        descontoVinte.setDataGeracao(LocalDateTime.now());

        when(descontoRepository.findByClienteIdAndUtilizadoFalse(1L)).thenReturn(Optional.of(descontoVinte));
        when(descontoRepository.save(any(DescontoFidelidade.class))).thenReturn(descontoVinte);

        Double precoOriginal = 150.0;
        Double resultado = fidelidadeService.aplicarDesconto(1L, precoOriginal);

        assertEquals(120.0, resultado);
        assertEquals(30.0, descontoVinte.getValorEconomizado());
    }
}
