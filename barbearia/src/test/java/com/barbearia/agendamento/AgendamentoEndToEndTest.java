package com.barbearia.agendamento;

import com.barbearia.agendamento.BarbeariaAgendamentoApplication;
import com.barbearia.agendamento.model.*;
import com.barbearia.agendamento.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = BarbeariaAgendamentoApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AgendamentoEndToEndTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private BarbeiroService barbeiroService;

    @Autowired
    private ServicoService servicoService;

    @Autowired
    private AgendamentoService agendamentoService;

    @Autowired
    private FidelidadeService fidelidadeService;

    @Autowired
    private com.barbearia.agendamento.repository.CartaoFidelidadeRepository cartaoFidelidadeRepository;

    private Cliente clienteTeste;
    private Barbeiro barbeiroTeste;
    private Servico servicoTeste;

    private static final Double PRECO_SERVICO = 50.0;
    private static final Double DESCONTO_FIDELIDADE = 40.0;
    private static final Double PRECO_COM_DESCONTO = 30.0;
    private static final Double VALOR_DESCONTADO = 20.0;
    private static final Integer DURACAO_SERVICO = 30;

    @BeforeEach
    void setUp() {
        // Criar cliente de teste
        clienteTeste = new Cliente();
        clienteTeste.setNome("Cliente Teste");
        clienteTeste.setEmail("cliente@teste.com");
        clienteTeste.setTelefone("11999999999");
        clienteTeste.setSenha("senha123");
        clienteTeste = clienteService.cadastrarCliente(clienteTeste);

        // Criar barbeiro de teste
        barbeiroTeste = new Barbeiro();
        barbeiroTeste.setNome("Barbeiro Teste");
        barbeiroTeste.setEmail("barbeiro@teste.com");
        barbeiroTeste.setTelefone("11988888888");
        barbeiroTeste.setSenha("senha123");
        barbeiroTeste.setAtivo(true);
        barbeiroTeste = barbeiroService.salvar(barbeiroTeste);

        // Criar serviço de teste
        servicoTeste = new Servico();
        servicoTeste.setNome("Corte Teste");
        servicoTeste.setDescricao("Corte de cabelo teste");
        servicoTeste.setPreco(PRECO_SERVICO);
        servicoTeste.setDuracaoMinutos(DURACAO_SERVICO);
        servicoTeste.setAtivo(true);
        servicoTeste = servicoService.cadastrar(servicoTeste);
    }

    @Test
    @DisplayName("Fluxo completo: Setup -> Cadastro -> Agendamento -> Cancelamento")
    void testFluxoCompletoAgendamento() throws Exception {
        // 1. Verificar se setup é necessário
        mockMvc.perform(get("/api/setup/required"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.required").isBoolean());

        // 2. Listar serviços públicos
        mockMvc.perform(get("/api/servicos/publicos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].nome").exists())
                .andExpect(jsonPath("$[0].preco").exists());

        // 3. Cadastrar novo cliente
        Map<String, String> cadastroRequest = new HashMap<>();
        cadastroRequest.put("nome", "João Silva");
        cadastroRequest.put("email", "joao@teste.com");
        cadastroRequest.put("telefone", "11977777777");
        cadastroRequest.put("senha", "Senha456@");

        mockMvc.perform(post("/api/clientes/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cadastroRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));

        // Set up authentication for the newly created client
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "joao@teste.com",
                null,
                java.util.Collections.singletonList(new SimpleGrantedAuthority("ROLE_CLIENTE"))
        );

        // 5. Listar horários disponíveis para amanhã
        LocalDate amanha = LocalDate.now().plusDays(1);
        mockMvc.perform(get("/api/cliente/agendamentos/horarios-disponiveis")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication(auth))
                        .param("barbeiroId", barbeiroTeste.getId().toString())
                        .param("servicoId", servicoTeste.getId().toString())
                        .param("data", amanha.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.horarios").isArray());

        // 6. Criar agendamento para uma data futura
        LocalDateTime dataAgendamento = LocalDateTime.now().plusDays(2).withHour(10).withMinute(0);
        
        Map<String, Object> agendamentoRequest = new HashMap<>();
        agendamentoRequest.put("barbeiroId", barbeiroTeste.getId());
        agendamentoRequest.put("servicoId", servicoTeste.getId());
        agendamentoRequest.put("dataHora", dataAgendamento.toString());

        String agendamentoResponse = mockMvc.perform(post("/api/cliente/agendamentos")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(agendamentoRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long agendamentoId = objectMapper.readTree(agendamentoResponse).get("id").asLong();

        // 7. Verificar o agendamento criado
        mockMvc.perform(get("/api/cliente/agendamentos/{id}", agendamentoId)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(agendamentoId))
                .andExpect(jsonPath("$.status").value("AGENDADO"))
                .andExpect(jsonPath("$.precoOriginal").value(50.0))
                .andExpect(jsonPath("$.precoFinal").value(50.0));

        // 8. Listar agendamentos do cliente
        mockMvc.perform(get("/api/cliente/agendamentos")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agendamentos").isArray())
                .andExpect(jsonPath("$.agendamentos", hasSize(greaterThanOrEqualTo(1))));

        // 9. Cancelar o agendamento
        mockMvc.perform(post("/api/cliente/agendamentos/{id}/cancelar", agendamentoId)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));

        // 10. Verificar que o agendamento foi cancelado
        mockMvc.perform(get("/api/cliente/agendamentos/{id}", agendamentoId)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"));
    }

    @Test
    @DisplayName("Fluxo de fidelidade: 5 cortes -> gera desconto -> aplica desconto")
    void testFluxoFidelidade() throws Exception {
        // Delete existing cartão to ensure clean state
        cartaoFidelidadeRepository.findByClienteId(clienteTeste.getId()).ifPresent(cartao -> {
            cartaoFidelidadeRepository.delete(cartao);
        });

        // Set up authentication for clienteTeste
        Authentication auth = new UsernamePasswordAuthenticationToken(
                clienteTeste.getEmail(),
                null,
                java.util.Collections.singletonList(new SimpleGrantedAuthority("ROLE_CLIENTE"))
        );

        // Registrar 5 cortes para gerar desconto (cenário artificial de teste)
        for (int i = 0; i < 5; i++) {
            LocalDateTime dataAgendamento = LocalDateTime.now().minusDays(5 - i)
                    .withHour(9 + i).withMinute(0);

            Agendamento ag = new Agendamento();
            ag.setCliente(clienteTeste);
            ag.setBarbeiro(barbeiroTeste);
            ag.setServico(servicoTeste);
            ag.setDataHora(dataAgendamento);
            ag.setPrecoOriginal(PRECO_SERVICO);
            ag.setPrecoFinal(PRECO_SERVICO);
            ag.setStatus("CONCLUIDO");
            agendamentoService.salvar(ag);
            fidelidadeService.registrarCorte(clienteTeste);
        }

        // Verificar que desconto foi gerado
        mockMvc.perform(get("/api/cliente/fidelidade")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.temDesconto").value(true))
                .andExpect(jsonPath("$.cartao.pontosAtuais").value(0))
                .andExpect(jsonPath("$.cartao.cortesRealizados").value(10));

        // Criar novo agendamento com desconto via API
        LocalDateTime dataComDesconto = LocalDateTime.now().plusDays(10).withHour(14).withMinute(0);
        
        Map<String, Object> agendamentoRequest = new HashMap<>();
        agendamentoRequest.put("barbeiroId", barbeiroTeste.getId());
        agendamentoRequest.put("servicoId", servicoTeste.getId());
        agendamentoRequest.put("dataHora", dataComDesconto.toString());

        String agendamentoResponse = mockMvc.perform(post("/api/cliente/agendamentos")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(agendamentoRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long agendamentoId = objectMapper.readTree(agendamentoResponse).get("id").asLong();

        // Verificar que desconto foi aplicado
        mockMvc.perform(get("/api/cliente/agendamentos/{id}", agendamentoId)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.precoOriginal").value(PRECO_SERVICO))
                .andExpect(jsonPath("$.precoFinal").value(PRECO_COM_DESCONTO))
                .andExpect(jsonPath("$.valorDescontado").value(VALOR_DESCONTADO))
                .andExpect(jsonPath("$.percentualDesconto").value(DESCONTO_FIDELIDADE));
    }

    @Test
    @DisplayName("Fluxo de histórico: agendamentos com diferentes status")
    void testFluxoHistorico() throws Exception {
        // Set up authentication for clienteTeste
        Authentication auth = new UsernamePasswordAuthenticationToken(
                clienteTeste.getEmail(),
                null,
                java.util.Collections.singletonList(new SimpleGrantedAuthority("ROLE_CLIENTE"))
        );

        // Criar agendamentos com diferentes status (cenário artificial de teste)
        LocalDateTime data1 = LocalDateTime.now().minusDays(5).withHour(10).withMinute(0);
        LocalDateTime data2 = LocalDateTime.now().minusDays(3).withHour(14).withMinute(0);
        LocalDateTime data3 = LocalDateTime.now().minusDays(1).withHour(9).withMinute(0);

        Agendamento ag1 = new Agendamento();
        ag1.setCliente(clienteTeste);
        ag1.setBarbeiro(barbeiroTeste);
        ag1.setServico(servicoTeste);
        ag1.setDataHora(data1);
        ag1.setPrecoOriginal(PRECO_SERVICO);
        ag1.setPrecoFinal(PRECO_SERVICO);
        ag1.setStatus("CONCLUIDO");
        agendamentoService.salvar(ag1);

        Agendamento ag2 = new Agendamento();
        ag2.setCliente(clienteTeste);
        ag2.setBarbeiro(barbeiroTeste);
        ag2.setServico(servicoTeste);
        ag2.setDataHora(data2);
        ag2.setPrecoOriginal(PRECO_SERVICO);
        ag2.setPrecoFinal(PRECO_SERVICO);
        ag2.setStatus("CANCELADO");
        agendamentoService.salvar(ag2);

        Agendamento ag3 = new Agendamento();
        ag3.setCliente(clienteTeste);
        ag3.setBarbeiro(barbeiroTeste);
        ag3.setServico(servicoTeste);
        ag3.setDataHora(data3);
        ag3.setPrecoOriginal(PRECO_SERVICO);
        ag3.setPrecoFinal(PRECO_SERVICO);
        ag3.setStatus("CONCLUIDO");
        agendamentoService.salvar(ag3);

        // Verificar histórico via API
        mockMvc.perform(get("/api/cliente/historico")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.historico").isArray())
                .andExpect(jsonPath("$.historico", hasSize(3)))
                .andExpect(jsonPath("$.totalAgendamentos").value(3))
                .andExpect(jsonPath("$.concluidos").value(2))
                .andExpect(jsonPath("$.cancelados").value(1));

        // Filtrar por status
        mockMvc.perform(get("/api/cliente/historico")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication(auth))
                        .param("status", "CONCLUIDO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.historico", hasSize(2)))
                .andExpect(jsonPath("$.concluidos").value(2));
    }

    @Test
    @DisplayName("Fluxo de edição de agendamento")
    void testFluxoEdicaoAgendamento() throws Exception {
        // Set up authentication for clienteTeste
        Authentication auth = new UsernamePasswordAuthenticationToken(
                clienteTeste.getEmail(),
                null,
                java.util.Collections.singletonList(new SimpleGrantedAuthority("ROLE_CLIENTE"))
        );

        // Criar agendamento inicial
        LocalDateTime dataInicial = LocalDateTime.now().plusDays(3).withHour(10).withMinute(0);
        
        Map<String, Object> agendamentoRequest = new HashMap<>();
        agendamentoRequest.put("barbeiroId", barbeiroTeste.getId());
        agendamentoRequest.put("servicoId", servicoTeste.getId());
        agendamentoRequest.put("dataHora", dataInicial.toString());

        String response = mockMvc.perform(post("/api/cliente/agendamentos")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(agendamentoRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long agendamentoId = objectMapper.readTree(response).get("id").asLong();

        // Editar para novo horário
        LocalDateTime novaData = LocalDateTime.now().plusDays(3).withHour(15).withMinute(0);
        
        Map<String, Object> edicaoRequest = new HashMap<>();
        edicaoRequest.put("barbeiroId", barbeiroTeste.getId());
        edicaoRequest.put("servicoId", servicoTeste.getId());
        edicaoRequest.put("dataHora", novaData.toString());

        mockMvc.perform(put("/api/cliente/agendamentos/{id}", agendamentoId)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(edicaoRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));

        // Verificar edição - confirmar que data foi alterada
        mockMvc.perform(get("/api/cliente/agendamentos/{id}", agendamentoId)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataHora").exists())
                .andExpect(jsonPath("$.dataHora").value(containsString("15:00")));
    }

    @Test
    @DisplayName("Validação: não permitir agendamento no passado")
    void testValidacaoAgendamentoPassado() throws Exception {
        // Set up authentication for clienteTeste
        Authentication auth = new UsernamePasswordAuthenticationToken(
                clienteTeste.getEmail(),
                null,
                java.util.Collections.singletonList(new SimpleGrantedAuthority("ROLE_CLIENTE"))
        );

        LocalDateTime dataPassada = LocalDateTime.now().minusDays(1).withHour(10).withMinute(0);
        
        Map<String, Object> agendamentoRequest = new HashMap<>();
        agendamentoRequest.put("barbeiroId", barbeiroTeste.getId());
        agendamentoRequest.put("servicoId", servicoTeste.getId());
        agendamentoRequest.put("dataHora", dataPassada.toString());

        mockMvc.perform(post("/api/cliente/agendamentos")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(agendamentoRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Validação: não permitir cancelamento com menos de 2 horas")
    void testValidacaoCancelamentoTarde() throws Exception {
        // Set up authentication for clienteTeste
        Authentication auth = new UsernamePasswordAuthenticationToken(
                clienteTeste.getEmail(),
                null,
                java.util.Collections.singletonList(new SimpleGrantedAuthority("ROLE_CLIENTE"))
        );

        // Criar agendamento para daqui a 90 minutos (menos de 2 horas)
        LocalDateTime dataProxima = LocalDateTime.now().plusMinutes(90);
        
        Map<String, Object> agendamentoRequest = new HashMap<>();
        agendamentoRequest.put("barbeiroId", barbeiroTeste.getId());
        agendamentoRequest.put("servicoId", servicoTeste.getId());
        agendamentoRequest.put("dataHora", dataProxima.toString());

        String response = mockMvc.perform(post("/api/cliente/agendamentos")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(agendamentoRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long agendamentoId = objectMapper.readTree(response).get("id").asLong();

        // Tentar cancelar (deve falhar por ser menos de 2 horas)
        mockMvc.perform(post("/api/cliente/agendamentos/{id}/cancelar", agendamentoId)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication(auth)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("2 horas")));
    }
}
