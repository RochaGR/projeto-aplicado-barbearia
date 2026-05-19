package com.barbearia.agendamento.model;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "configuracao_horario")
public class ConfiguracaoHorario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Integer diaSemana;

    @Column(nullable = false)
    private String diaNome;

    @Column(nullable = false)
    private Boolean ativo = true;

    private LocalTime abertura;

    private LocalTime fechamento;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getDiaSemana() { return diaSemana; }
    public void setDiaSemana(Integer diaSemana) { this.diaSemana = diaSemana; }

    public String getDiaNome() { return diaNome; }
    public void setDiaNome(String diaNome) { this.diaNome = diaNome; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }

    public LocalTime getAbertura() { return abertura; }
    public void setAbertura(LocalTime abertura) { this.abertura = abertura; }

    public LocalTime getFechamento() { return fechamento; }
    public void setFechamento(LocalTime fechamento) { this.fechamento = fechamento; }
}
