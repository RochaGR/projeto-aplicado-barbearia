package com.barbearia.agendamento.controller;

import com.barbearia.agendamento.model.*;
import com.barbearia.agendamento.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ServicoService servicoService;
    private final BarbeiroService barbeiroService;
    private final AgendamentoService agendamentoService;
    private final ConfiguracaoHorarioService horarioService;
    private final ClienteService clienteService;
    private final FidelidadeService fidelidadeService;

    @Value("${GROQ_API_KEY:}")
    private String apiKey;

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    public ChatController(ServicoService servicoService,
                          BarbeiroService barbeiroService,
                          AgendamentoService agendamentoService,
                          ConfiguracaoHorarioService horarioService,
                          ClienteService clienteService,
                          FidelidadeService fidelidadeService) {
        this.servicoService = servicoService;
        this.barbeiroService = barbeiroService;
        this.agendamentoService = agendamentoService;
        this.horarioService = horarioService;
        this.clienteService = clienteService;
        this.fidelidadeService = fidelidadeService;
    }

    public record ChatRequest(String mensagem, List<Map<String, String>> historico) {}

    public static class AgendarRequest {
        private Long servicoId;
        private Long barbeiroId;
        private String data;
        private String horario;
        public Long getServicoId() { return servicoId; }
        public void setServicoId(Long v) { this.servicoId = v; }
        public Long getBarbeiroId() { return barbeiroId; }
        public void setBarbeiroId(Long v) { this.barbeiroId = v; }
        public String getData() { return data; }
        public void setData(String v) { this.data = v; }
        public String getHorario() { return horario; }
        public void setHorario(String v) { this.horario = v; }
    }

    @GetMapping("/auth-check")
    public ResponseEntity<?> authCheck() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return ResponseEntity.ok(Map.of("autenticado", false, "motivo", "auth null"));
        Object principal = auth.getPrincipal();
        return ResponseEntity.ok(Map.of(
            "autenticado", auth.isAuthenticated(),
            "tipo", principal.getClass().getSimpleName(),
            "nome", principal instanceof Cliente c ? c.getNome() : auth.getName()
        ));
    }

    @GetMapping("/iniciar")
    public ResponseEntity<?> iniciar() {
        List<Servico> servicos = servicoService.listarTodos().stream()
                .filter(Servico::isAtivo).collect(Collectors.toList());
        List<Barbeiro> barbeiros = barbeiroService.listarTodos().stream()
                .filter(Barbeiro::isAtivo).collect(Collectors.toList());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("servicos", servicos.stream().map(s -> Map.of(
                "id", s.getId(),
                "nome", s.getNome(),
                "preco", String.format("R$ %.2f", s.getPreco() != null ? s.getPreco() : 0.0),
                "duracao", (s.getDuracaoMinutos() != null ? s.getDuracaoMinutos() : 30) + " min"
        )).collect(Collectors.toList()));
        response.put("barbeiros", barbeiros.stream().map(b -> Map.of(
                "id", b.getId(),
                "nome", b.getNome()
        )).collect(Collectors.toList()));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/mensagem")
    public ResponseEntity<?> chat(@RequestBody ChatRequest req) {
        if (req.mensagem() == null || req.mensagem().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Mensagem vazia"));
        }
        try {
            String offTopic = detectarOffTopic(req.mensagem());
            if (offTopic != null) {
                String resposta = offTopic;
                List<Servico> servicos = servicoService.listarTodos().stream()
                        .filter(Servico::isAtivo).collect(Collectors.toList());
                List<Barbeiro> barbeiros = barbeiroService.listarTodos().stream()
                        .filter(Barbeiro::isAtivo).collect(Collectors.toList());
                Map<String, Object> response = new LinkedHashMap<>();
                response.put("resposta", resposta);
                response.put("servicoSelecionadoId", null);
                response.put("barbeiroSelecionadoId", null);
                response.put("servicos", servicos.stream().map(s -> Map.of(
                        "id", s.getId(),
                        "nome", s.getNome(),
                        "preco", String.format("R$ %.2f", s.getPreco() != null ? s.getPreco() : 0.0),
                        "duracao", (s.getDuracaoMinutos() != null ? s.getDuracaoMinutos() : 30) + " min"
                )).collect(Collectors.toList()));
                response.put("barbeiros", barbeiros.stream().map(b -> Map.of(
                        "id", b.getId(),
                        "nome", b.getNome()
                )).collect(Collectors.toList()));
                return ResponseEntity.ok(response);
            }
            String contexto = montarContexto();
            String resposta = chamarLLM(req.mensagem(), req.historico(), contexto);
            List<Servico> servicos = servicoService.listarTodos().stream()
                    .filter(Servico::isAtivo).collect(Collectors.toList());
            List<Barbeiro> barbeiros = barbeiroService.listarTodos().stream()
                    .filter(Barbeiro::isAtivo).collect(Collectors.toList());

            Long servicoDetectado = detectarServicoNaConversa(req.mensagem(), req.historico());
            Long barbeiroDetectado = detectarBarbeiroNaConversa(req.mensagem(), req.historico());

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("resposta", resposta);
            response.put("servicoSelecionadoId", servicoDetectado);
            response.put("barbeiroSelecionadoId", barbeiroDetectado);
            response.put("servicos", servicos.stream().map(s -> Map.of(
                    "id", s.getId(),
                    "nome", s.getNome(),
                    "preco", String.format("R$ %.2f", s.getPreco() != null ? s.getPreco() : 0.0),
                    "duracao", (s.getDuracaoMinutos() != null ? s.getDuracaoMinutos() : 30) + " min"
            )).collect(Collectors.toList()));
            response.put("barbeiros", barbeiros.stream().map(b -> Map.of(
                    "id", b.getId(),
                    "nome", b.getNome()
            )).collect(Collectors.toList()));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Erro ao processar mensagem: " + e.getMessage()));
        }
    }

    public static class DisponibilidadeRequest {
        private Long barbeiroId;
        private String data;
        private String horario;
        public Long getBarbeiroId() { return barbeiroId; }
        public void setBarbeiroId(Long v) { this.barbeiroId = v; }
        public String getData() { return data; }
        public void setData(String v) { this.data = v; }
        public String getHorario() { return horario; }
        public void setHorario(String v) { this.horario = v; }
    }

    @GetMapping("/horarios-disponiveis")
    public ResponseEntity<?> horariosDisponiveis(@RequestParam Long barbeiroId, @RequestParam String data) {
        try {
            LocalDate dia = LocalDate.parse(data);
            List<String> horarios = new java.util.ArrayList<>();
            LocalTime ABERTURA = LocalTime.of(8, 0);
            LocalTime FECHAMENTO = LocalTime.of(19, 0);
            LocalTime hora = ABERTURA;
            while (!hora.isAfter(FECHAMENTO)) {
                LocalDateTime dt = LocalDateTime.of(dia, hora);
                if (dt.isAfter(LocalDateTime.now()) && !agendamentoService.existeConflitoHorario(barbeiroId, dt)) {
                    horarios.add(hora.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
                }
                hora = hora.plusMinutes(30);
            }
            return ResponseEntity.ok(horarios);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/disponibilidade")
    public ResponseEntity<?> verificarDisponibilidade(@RequestBody DisponibilidadeRequest req) {
        try {
            LocalDateTime dataHora = LocalDateTime.parse(req.getData() + "T" + req.getHorario() + ":00");
            boolean ocupado = agendamentoService.existeConflitoHorario(req.getBarbeiroId(), dataHora);
            List<String> sugestoes = new java.util.ArrayList<>();
            if (ocupado) {
                LocalTime base = LocalTime.parse(req.getHorario());
                LocalTime ABERTURA = LocalTime.of(8, 0);
                LocalTime FECHAMENTO = LocalTime.of(19, 0);
                java.util.Set<LocalTime> jaIncluidas = new java.util.HashSet<>();
                // Tenta para frente e para trás, alternando
                for (int passo = 1; passo <= 6; passo++) {
                    for (int sinal : new int[]{1, -1}) {
                        LocalTime alt = base.plusMinutes(passo * 30 * sinal);
                        if (alt.isBefore(ABERTURA) || alt.isAfter(FECHAMENTO)) continue;
                        if (alt.equals(base)) continue;
                        if (!jaIncluidas.add(alt)) continue;
                        LocalDateTime altDt = LocalDateTime.of(dataHora.toLocalDate(), alt);
                        if (altDt.isAfter(LocalDateTime.now()) && !agendamentoService.existeConflitoHorario(req.getBarbeiroId(), altDt)) {
                            sugestoes.add(alt.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
                            if (sugestoes.size() >= 3) break;
                        }
                    }
                    if (sugestoes.size() >= 3) break;
                }
            }
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("disponivel", !ocupado);
            resp.put("sugestoes", sugestoes);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/agendar")
    public ResponseEntity<?> agendar(@RequestBody AgendarRequest req) {
        log.info("agendar chamado: servicoId={}, barbeiroId={}, data={}, horario={}",
            req.getServicoId(), req.getBarbeiroId(), req.getData(), req.getHorario());
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() ||
                auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken) {
                Map<String, Object> debug = new LinkedHashMap<>();
                debug.put("auth-null", auth == null);
                if (auth != null) {
                    debug.put("authenticated", auth.isAuthenticated());
                    debug.put("principal-type", auth.getPrincipal().getClass().getName());
                    debug.put("principal-name", auth.getName());
                    debug.put("anonymous", auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken);
                }
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(debug);
            }
            Cliente cliente = null;
            Object principal = auth.getPrincipal();
            if (principal instanceof Cliente c) {
                cliente = c;
            } else if (principal instanceof org.springframework.security.core.userdetails.User u) {
                String email = u.getUsername();
                cliente = clienteService.buscarPorEmail(email).orElse(null);
            }
            if (cliente == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error", "Usuário não autenticado",
                    "tipo", principal.getClass().getName()
                ));
            }
            if (req.getServicoId() == null || req.getBarbeiroId() == null ||
                req.getData() == null || req.getHorario() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Dados incompletos"));
            }
            Servico servico = servicoService.buscarPorId(req.getServicoId())
                    .orElseThrow(() -> new IllegalArgumentException("Serviço não encontrado"));
            Barbeiro barbeiro = barbeiroService.buscarPorId(req.getBarbeiroId())
                    .orElseThrow(() -> new IllegalArgumentException("Barbeiro não encontrado"));

            LocalDateTime dataHora = LocalDateTime.parse(req.getData() + "T" + req.getHorario() + ":00");
            if (dataHora.isBefore(LocalDateTime.now())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Data/hora deve ser futura"));
            }
            if (agendamentoService.existeConflitoHorario(req.getBarbeiroId(), dataHora)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Horário já ocupado para este barbeiro"));
            }

            double precoOriginal = servico.getPreco() != null ? servico.getPreco() : 0.0;
            double precoFinal = precoOriginal;
            Double percentualAplicado = null;
            double valorDescontado = 0.0;
            var desconto = fidelidadeService.buscarDescontoDisponivel(cliente.getId());
            if (desconto.isPresent()) {
                percentualAplicado = desconto.get().getPercentualDesconto();
                precoFinal = fidelidadeService.aplicarDesconto(cliente.getId(), precoOriginal);
                valorDescontado = Math.max(0.0, precoOriginal - precoFinal);
            }

            Agendamento ag = new Agendamento();
            ag.setCliente(cliente);
            ag.setBarbeiro(barbeiro);
            ag.setServico(servico);
            ag.setDataHora(dataHora);
            ag.setStatus("AGENDADO");
            ag.setPrecoOriginal(precoOriginal);
            ag.setPrecoFinal(precoFinal);
            ag.setPercentualDescontoAplicado(percentualAplicado);
            ag.setValorDescontado(valorDescontado);

            Agendamento salvo = agendamentoService.agendar(ag);
            java.util.Map<String, Object> resp = new java.util.HashMap<>();
            resp.put("id", salvo.getId());
            resp.put("servico", servico.getNome());
            resp.put("barbeiro", barbeiro.getNome());
            resp.put("dataHora", dataHora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm")));
            resp.put("preco", String.format("R$ %.2f", precoFinal));
            if (valorDescontado > 0) {
                resp.put("desconto", String.format("R$ %.2f", valorDescontado));
            }
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            log.error("ERRO no agendar: {} tipo={}", e.getMessage(), e.getClass().getName(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : e.getClass().getName()));
        }
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleJsonError(org.springframework.http.converter.HttpMessageNotReadableException e) {
        log.error("Erro de JSON: {}", e.getMessage());
        return ResponseEntity.badRequest().body(Map.of("error", "Erro ao ler JSON: " + e.getMessage()));
    }

    private String textoCompletoConversa(String mensagemAtual, List<Map<String, String>> historico) {
        String texto = mensagemAtual.toLowerCase();
        if (historico != null) {
            for (Map<String, String> h : historico) {
                texto += " " + h.getOrDefault("content", "").toLowerCase();
            }
        }
        return texto;
    }

    private Long detectarServicoNaConversa(String mensagemAtual, List<Map<String, String>> historico) {
        String texto = textoCompletoConversa(mensagemAtual, historico);
        List<Servico> servicos = servicoService.listarTodos().stream().filter(Servico::isAtivo).collect(Collectors.toList());
        for (Servico s : servicos) {
            if (texto.contains(s.getNome().toLowerCase())) return s.getId();
        }
        return null;
    }

    private Long detectarBarbeiroNaConversa(String mensagemAtual, List<Map<String, String>> historico) {
        String texto = textoCompletoConversa(mensagemAtual, historico);
        List<Barbeiro> barbeiros = barbeiroService.listarTodos().stream().filter(Barbeiro::isAtivo).collect(Collectors.toList());
        for (Barbeiro b : barbeiros) {
            if (texto.contains(b.getNome().toLowerCase())) return b.getId();
        }
        return null;
    }

    private String detectarOffTopic(String mensagem) {
        String m = mensagem.toLowerCase().trim();
        String[] injecoes = {
            "esqueça", "esquece", "ignore", "desconsidere", "saia daqui", "sai daqui",
            "liste", "mostre o código", "código fonte", "estrutura do projeto",
            "me conte uma piada", "conte uma piada", "faça uma piada",
            "você é um", "qual é o seu propósito", "você é uma ia",
            "seu prompt", "sistema", "system prompt",
            "fale sobre", "o que você sabe", "o que é",
            "copa", "jogadores", "futebol", "notícias", "clima",
            "receita", "culinária", "bife", "strogonoff",
            "leandro",
        };
        for (String padrao : injecoes) {
            if (m.contains(padrao)) {
                return "Desculpe, só posso ajudar com agendamentos na Barbearia Souza. 😊 Escolha um serviço abaixo:";
            }
        }
        return null;
    }

    private String montarContexto() {
        StringBuilder sb = new StringBuilder();
        List<Servico> servicos = servicoService.listarTodos().stream()
                .filter(Servico::isAtivo).collect(Collectors.toList());

        sb.append("SERVIÇOS DISPONÍVEIS:\n");
        for (Servico s : servicos) {
            sb.append(String.format("- %s (R$ %.2f, %d min)\n",
                    s.getNome(), s.getPreco() != null ? s.getPreco() : 0.0,
                    s.getDuracaoMinutos() != null ? s.getDuracaoMinutos() : 30));
        }
        sb.append("\nBARBEIROS DISPONÍVEIS:\n");
        barbeiroService.listarTodos().stream()
                .filter(Barbeiro::isAtivo)
                .forEach(b -> sb.append("- ").append(b.getNome()).append("\n"));

        sb.append("\nHORÁRIOS DE FUNCIONAMENTO:\n");
        horarioService.listarTodos().forEach(h -> {
            if (h.getAtivo()) {
                sb.append(String.format("- %s: %s às %s\n", h.getDiaNome(),
                        h.getAbertura() != null ? h.getAbertura().format(DateTimeFormatter.ofPattern("HH:mm")) : "08:00",
                        h.getFechamento() != null ? h.getFechamento().format(DateTimeFormatter.ofPattern("HH:mm")) : "19:00"));
            } else {
                sb.append(String.format("- %s: FECHADO\n", h.getDiaNome()));
            }
        });
        sb.append(String.format("\nHOJE: %s (%s)\n",
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                LocalDate.now().getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL, new Locale("pt", "BR"))));
        return sb.toString();
    }

    private String chamarLLM(String mensagem, List<Map<String, String>> historico, String contexto) {
        String systemPrompt = """
                Você é o assistente da Barbearia Souza. Ajude o cliente a agendar seu horário.

                REGRAS (ordem OBRIGATÓRIA - verifique cada etapa UMA POR UMA):
                1. Responda SEMPRE em português brasileiro, amigável e direto. Máximo 2 frases. NUNCA repita informações.
                2. Use TODO o histórico para saber o que JÁ foi informado, mas ignore SUAS PRÓPRIAS mensagens. Considere APENAS o que o CLIENTE disse.
                3. Etapa SERVIÇO: Se o cliente NÃO informou o serviço → PERGUNTE qual serviço ele deseja. PARE AQUI.
                4. Etapa BARBEIRO: Se o cliente já informou o serviço MAS NÃO informou o barbeiro → PERGUNTE qual barbeiro ele prefere. PARE AQUI.
                5. Etapa DATA: Se o cliente já informou serviço E barbeiro MAS NÃO informou o DIA → pergunte APENAS o 📅 dia (ex: "Qual o dia?"). PARE AQUI. Se o cliente JÁ informou o DIA → diga APENAS "Ótimo! Verifique os horários disponíveis abaixo." e PARE. NUNCA pergunte o horário. NUNCA liste horários.
                6. ETAPA HORARIO só mande o template , só template dos horarios disponiveis DEPOIS DE JA TER AS OUTRAS INFOS SERVICO BARBEIRO E O DATA DO DIA, LISTE OS HORARIOS DISPONIVEIS COM O TEMPLATE DE LISTAR HORARIIOS E DEPOIS DE  ESOCLHER O HORARIO E CONFIRMAR COM O SIM JA ABRIR O RESUMO.
                7. Etapa RESUMO: Se o cliente já informou serviço, barbeiro, DIA E HORÁRIO → apenas diga "Ótimo! Verifique o resumo abaixo:" e PARE. NUNCA pergunte "Confirma?" ou "Confirma?". O resumo é exibido automaticamente pelo sistema com botões Sim/Não.
                8. NUNCA pule etapas. Só avance quando a etapa anterior estiver COMPLETA.
                9. Formate com emojis: ✂️ serviços, 👤 barbeiros, 📅 dia, 🕐 horários.
                10. INTERPRETAÇÃO DE HORÁRIO: "18", "18h", "18:00", "as 18", "18 horas" = 18:00. "19:30" = 19:30. NUNCA pergunte se é ":00" ou ":18". Horários sem minutos = :00.

                DADOS DA BARBEARIA DE HOJE:
                """ + contexto;

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        if (historico != null) {
            for (Map<String, String> h : historico) {
                messages.add(Map.of("role", h.getOrDefault("role", "user"), "content", h.getOrDefault("content", "")));
            }
        }
        messages.add(Map.of("role", "user", "content", mensagem));

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("model", "llama-3.3-70b-versatile");
        requestBody.put("messages", messages);
        requestBody.put("max_tokens", 500);
        requestBody.put("temperature", 0.7);

        @SuppressWarnings("unchecked")
        Map<String, Object> response;
        try {
            response = RestClient.create().post()
                    .uri("https://api.groq.com/openai/v1/chat/completions")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            String body = e.getResponseBodyAsString();
            throw new RuntimeException("Erro API (" + e.getStatusCode() + "): " + body);
        }

        if (response == null) throw new RuntimeException("Resposta nula da API");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        if (choices == null || choices.isEmpty()) {
            throw new RuntimeException("Resposta vazia da API");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        return String.valueOf(message.get("content"));
    }
}
