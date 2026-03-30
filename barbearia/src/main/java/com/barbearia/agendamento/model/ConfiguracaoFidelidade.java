package com.barbearia.agendamento.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "configuracao_fidelidade")
public class ConfiguracaoFidelidade {

    @Id
    private Long id = 1L;

    @Column(nullable = false)
    private Double percentualDesconto = 40.0;

    @Column(nullable = false)
    private Integer cortesParaDesconto = 5;

    @Column(nullable = false)
    private LocalDateTime ultimaAtualizacao = LocalDateTime.now();

    @Column(length = 200)
    private String atualizadoPor;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getPercentualDesconto() {
        return percentualDesconto;
    }

    public void setPercentualDesconto(Double percentualDesconto) {
        this.percentualDesconto = percentualDesconto;
    }

    public Integer getCortesParaDesconto() {
        return cortesParaDesconto;
    }

    public void setCortesParaDesconto(Integer cortesParaDesconto) {
        this.cortesParaDesconto = cortesParaDesconto;
    }

    public LocalDateTime getUltimaAtualizacao() {
        return ultimaAtualizacao;
    }

    public void setUltimaAtualizacao(LocalDateTime ultimaAtualizacao) {
        this.ultimaAtualizacao = ultimaAtualizacao;
    }

    public String getAtualizadoPor() {
        return atualizadoPor;
    }

    public void setAtualizadoPor(String atualizadoPor) {
        this.atualizadoPor = atualizadoPor;
    }
}

