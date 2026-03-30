package com.barbearia.agendamento.service;

import com.barbearia.agendamento.model.CartaoFidelidade;
import com.barbearia.agendamento.model.Cliente;
import com.barbearia.agendamento.model.ConfiguracaoFidelidade;
import com.barbearia.agendamento.model.DescontoFidelidade;
import com.barbearia.agendamento.repository.CartaoFidelidadeRepository;
import com.barbearia.agendamento.repository.ConfiguracaoFidelidadeRepository;
import com.barbearia.agendamento.repository.DescontoFidelidadeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class FidelidadeService {

    private final CartaoFidelidadeRepository cartaoRepository;
    private final DescontoFidelidadeRepository descontoRepository;
    private final ConfiguracaoFidelidadeRepository configRepository;

    public FidelidadeService(CartaoFidelidadeRepository cartaoRepository,
                             DescontoFidelidadeRepository descontoRepository,
                             ConfiguracaoFidelidadeRepository configRepository) {
        this.cartaoRepository = cartaoRepository;
        this.descontoRepository = descontoRepository;
        this.configRepository = configRepository;
    }

    /**
     * Chamado toda vez que um agendamento for marcado como CONCLUIDO.
     * Retorna true se um novo desconto foi gerado.
     */
    public boolean registrarCorte(Cliente cliente) {
        ConfiguracaoFidelidade config = getConfig();

        CartaoFidelidade cartao = cartaoRepository
                .findByClienteId(cliente.getId())
                .orElseGet(() -> criarCartao(cliente));

        cartao.setPontos(cartao.getPontos() + 1);
        cartao.setTotalCortesRealizados(cartao.getTotalCortesRealizados() + 1);
        cartaoRepository.save(cartao);

        if (cartao.getPontos() >= config.getCortesParaDesconto()) {
            gerarDesconto(cliente, config.getPercentualDesconto());
            cartao.setPontos(0);
            cartaoRepository.save(cartao);
            return true;
        }

        return false;
    }

    private void gerarDesconto(Cliente cliente, Double percentual) {
        if (descontoRepository.findByClienteIdAndUtilizadoFalse(cliente.getId()).isPresent()) {
            return;
        }

        DescontoFidelidade desconto = new DescontoFidelidade();
        desconto.setCliente(cliente);
        desconto.setPercentualDesconto(percentual);
        desconto.setUtilizado(false);
        desconto.setDataGeracao(LocalDateTime.now());
        descontoRepository.save(desconto);
    }

    private CartaoFidelidade criarCartao(Cliente cliente) {
        CartaoFidelidade cartao = new CartaoFidelidade();
        cartao.setCliente(cliente);
        cartao.setPontos(0);
        cartao.setTotalCortesRealizados(0);
        return cartaoRepository.save(cartao);
    }

    private ConfiguracaoFidelidade getConfig() {
        return configRepository.findById(1L)
                .orElseGet(() -> configRepository.save(new ConfiguracaoFidelidade()));
    }

    @Transactional(readOnly = true)
    public ConfiguracaoFidelidade buscarConfiguracaoAtual() {
        return getConfig();
    }

    @Transactional(readOnly = true)
    public Optional<CartaoFidelidade> buscarCartao(Long clienteId) {
        return cartaoRepository.findByClienteId(clienteId);
    }

    @Transactional(readOnly = true)
    public Optional<DescontoFidelidade> buscarDescontoDisponivel(Long clienteId) {
        return descontoRepository.findByClienteIdAndUtilizadoFalse(clienteId);
    }

    /**
     * Aplica o desconto no preco e marca como utilizado.
     */
    public Double aplicarDesconto(Long clienteId, Double precoOriginal) {
        Optional<DescontoFidelidade> descontoOpt =
                descontoRepository.findByClienteIdAndUtilizadoFalse(clienteId);

        if (descontoOpt.isPresent()) {
            DescontoFidelidade desconto = descontoOpt.get();
            Double precoComDesconto = precoOriginal * (1 - desconto.getPercentualDesconto() / 100);
            Double economizado = precoOriginal - precoComDesconto;

            desconto.setUtilizado(true);
            desconto.setDataUtilizacao(LocalDateTime.now());
            desconto.setValorEconomizado(economizado);
            descontoRepository.save(desconto);

            return precoComDesconto;
        }

        return precoOriginal;
    }
}
