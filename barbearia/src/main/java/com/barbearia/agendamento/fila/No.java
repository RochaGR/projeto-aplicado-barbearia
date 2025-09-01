package com.barbearia.agendamento.fila;

import com.barbearia.agendamento.model.Agendamento;

public class No {
    private Agendamento valor;
    private No proximo;

    public No(Agendamento valor) {
        this.valor = valor;
    }

    public Agendamento getValor() {
        return valor;
    }

    public void setValor(Agendamento valor) {
        this.valor = valor;
    }

    public No getProximo() {
        return proximo;
    }

    public void setProximo(No proximo) {
        this.proximo = proximo;
    }
}
