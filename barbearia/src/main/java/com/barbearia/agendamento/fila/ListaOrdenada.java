package com.barbearia.agendamento.fila;

import com.barbearia.agendamento.model.Agendamento;

public class ListaOrdenada {
    private No inicio;
    private No fim;

    public boolean isEmpty() {
        return inicio == null;
    }

    public void enqueueOrdenado(Agendamento novoAgendamento) {
        No novoNo = new No(novoAgendamento);

        if (isEmpty() || novoAgendamento.getDataHora().isBefore(inicio.getValor().getDataHora())) {
            novoNo.setProximo(inicio);
            inicio = novoNo;
            if (fim == null)
                fim = novoNo;
        } else {
            No atual = inicio;
            while (atual.getProximo() != null &&
                    atual.getProximo().getValor().getDataHora().isBefore(novoAgendamento.getDataHora())) {
                atual = atual.getProximo();
            }
            novoNo.setProximo(atual.getProximo());
            atual.setProximo(novoNo);
            if (novoNo.getProximo() == null)
                fim = novoNo;
        }
    }

    public Agendamento dequeue() {
        if (!isEmpty()) {
            Agendamento valor = inicio.getValor();
            inicio = inicio.getProximo();
            if (inicio == null)
                fim = null;
            return valor;
        }
        return null;
    }
}
