package com.barbearia.agendamento.config;

import com.barbearia.agendamento.model.Servico;
import com.barbearia.agendamento.service.ServicoService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ServicoService servicoService;

    public DataInitializer(ServicoService servicoService) {
        this.servicoService = servicoService;
    }

    @Override
    public void run(String... args) throws Exception {
        inicializarServicos();
    }

    private void inicializarServicos() {
        List<Servico> servicosBasicos = List.of(
                criarServico("Corte Masculino Simples", "Corte tradicional com máquina e tesoura", 30, 25.00,
                        "https://i.pinimg.com/564x/33/30/d8/3330d88a6613fbb905ccfc5277cdc415.jpg"),
                criarServico("Corte Degradado", "Corte moderno com efeito desvanecido", 45, 35.00,
                        "https://moda20.com.br/wp-content/uploads/2023/10/Luca-Lyra_Easy-Resize.com_.jpg"),
                criarServico("Corte e Barba", "Pacote completo com corte e barba", 60, 50.00,
                        "https://i.pinimg.com/564x/5b/9d/01/5b9d01ccecdaab4b3e1c7e513e8c224d.jpg"),
                criarServico("Barba Completa", "Barba feita com navalha e produtos específicos", 30, 30.00,
                        "https://images.unsplash.com/photo-1622287162006-2aeac74df1ae?auto=format&fit=crop&q=80&w=900"),
                criarServico("Corte Infantil", "Corte especial para crianças", 25, 20.00,
                        "https://images.unsplash.com/photo-1503951914875-402c5b3b3b6c?auto=format&fit=crop&q=80&w=900"),
                criarServico("Tratamento de Sobrancelha", "Design e tratamento de sobrancelha masculina", 15, 15.00,
                        "https://images.unsplash.com/photo-1519415305795-8078ce9113ac?auto=format&fit=crop&q=80&w=900"));

        for (Servico servico : servicosBasicos) {
            try {
                servicoService.cadastrar(servico);
            } catch (IllegalArgumentException e) {
                // Serviço já existe, ignora
            }
        }
    }

    private Servico criarServico(String nome, String descricao, Integer duracaoMinutos, Double preco, String imageUrl) {
        Servico servico = new Servico();
        servico.setNome(nome);
        servico.setDescricao(descricao);
        servico.setDuracaoMinutos(duracaoMinutos);
        servico.setPreco(preco);
        servico.setImageUrl(imageUrl);
        servico.setAtivo(true);
        return servico;
    }
}
