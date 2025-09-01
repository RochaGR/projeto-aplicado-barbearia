package com.barbearia.agendamento.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "agendamentos")
public class Agendamento {

       @Id
       @GeneratedValue(strategy = GenerationType.IDENTITY)
       private Long id;

       @ManyToOne
       @JoinColumn(name = "cliente_id", nullable = false)
       private Cliente cliente;

       @ManyToOne
       @JoinColumn(name = "barbeiro_id", nullable = false)
       private Barbeiro barbeiro;

       @ManyToOne
       @JoinColumn(name = "servico_id", nullable = false)
       private Servico servico;

       @Column(nullable = false)
       private LocalDateTime dataHora;

       @Column(nullable = false)
       private String status = "AGENDADO";

       public Long getId() {
              return id;
       }

       public void setId(Long id) {
              this.id = id;
       }

       public Cliente getCliente() {
              return cliente;
       }

       public void setCliente(Cliente cliente) {
              this.cliente = cliente;
       }

       public Barbeiro getBarbeiro() {
              return barbeiro;
       }

       public void setBarbeiro(Barbeiro barbeiro) {
              this.barbeiro = barbeiro;
       }

       public Servico getServico() {
              return servico;
       }

       public void setServico(Servico servico) {
              this.servico = servico;
       }

       public LocalDateTime getDataHora() {
              return dataHora;
       }

       public void setDataHora(LocalDateTime dataHora) {
              this.dataHora = dataHora;
       }

       public String getStatus() {
              return status;
       }

       public void setStatus(String status) {
              this.status = status;
       }
}
