package com.barbearia.agendamento.api;

import com.barbearia.agendamento.model.Agendamento;
import com.barbearia.agendamento.model.Barbeiro;
import com.barbearia.agendamento.model.Cliente;
import com.barbearia.agendamento.model.Servico;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ApiMapper {

    private ApiMapper() {
    }

    public static Map<String, Object> cliente(Cliente c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.getId());
        m.put("nome", c.getNome());
        m.put("telefone", c.getTelefone());
        m.put("email", c.getEmail());
        return m;
    }

    public static Map<String, Object> barbeiro(Barbeiro b) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", b.getId());
        m.put("nome", b.getNome());
        m.put("email", b.getEmail());
        m.put("telefone", b.getTelefone());
        m.put("ativo", b.isAtivo());
        return m;
    }

    public static Map<String, Object> servico(Servico s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", s.getId());
        m.put("nome", s.getNome());
        m.put("descricao", s.getDescricao() != null ? s.getDescricao() : "");
        m.put("preco", s.getPreco() != null ? s.getPreco() : 0);
        m.put("duracaoMinutos", s.getDuracaoMinutos() != null ? s.getDuracaoMinutos() : 0);
        m.put("ativo", s.isAtivo());
        m.put("imageUrl", s.getImageUrl());
        return m;
    }

    public static Map<String, Object> agendamento(Agendamento a) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", a.getId());
        m.put("dataHora", a.getDataHora());
        m.put("status", a.getStatus());
        m.put("cliente", cliente(a.getCliente()));
        m.put("barbeiro", barbeiro(a.getBarbeiro()));
        m.put("servico", servico(a.getServico()));
        return m;
    }
}
