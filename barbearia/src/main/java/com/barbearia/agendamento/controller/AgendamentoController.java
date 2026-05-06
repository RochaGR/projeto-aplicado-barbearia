package com.barbearia.agendamento.controller;

import com.barbearia.agendamento.model.*;
import com.barbearia.agendamento.service.*;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.beans.PropertyEditorSupport;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/agendamentos")
public class AgendamentoController {

    private final AgendamentoService agendamentoService;
    private final ServicoService servicoService;
    private final BarbeiroService barbeiroService;
    private final ClienteService clienteService;
    private final FidelidadeService fidelidadeService;

    public AgendamentoController(AgendamentoService agendamentoService,
                                 ServicoService servicoService,
                                 BarbeiroService barbeiroService,
                                 ClienteService clienteService,
                                 FidelidadeService fidelidadeService) {
        this.agendamentoService = agendamentoService;
        this.servicoService = servicoService;
        this.barbeiroService = barbeiroService;
        this.clienteService = clienteService;
        this.fidelidadeService = fidelidadeService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(LocalDateTime.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                setValue(LocalDateTime.parse(text, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            }
        });
    }

    @GetMapping("/novo")
    public String mostrarFormularioAgendamento(Model model,
                                               Authentication authentication) {
        model.addAttribute("agendamento", new Agendamento());
        carregarDadosFormulario(model, authentication);
        return "agendamento-cliente-form";
    }

    @PostMapping("/salvar")
    public String salvarAgendamento(@Valid @ModelAttribute Agendamento agendamento,
                                    BindingResult result,
                                    Model model,
                                    RedirectAttributes redirectAttributes,
                                    Authentication authentication) {

        if (result.hasErrors()) {
            carregarDadosFormulario(model, authentication);
            return "agendamento-cliente-form";
        }

        if (agendamento.getDataHora().isBefore(LocalDateTime.now())) {
            result.rejectValue("dataHora", "error.dataHora", "A data/hora deve ser futura");
            carregarDadosFormulario(model, authentication);
            return "agendamento-cliente-form";
        }

        if (agendamentoService.existeConflitoHorario(
                agendamento.getBarbeiro().getId(), agendamento.getDataHora())) {
            result.rejectValue("dataHora", "error.dataHora", "Horário já ocupado para este barbeiro");
            carregarDadosFormulario(model, authentication);
            return "agendamento-cliente-form";
        }

        try {
            String email = getUsernameFromAuthentication(authentication);
            Cliente cliente = clienteService.buscarPorEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));

            agendamento.setCliente(cliente);

            Agendamento agendamentoSalvo = agendamentoService.agendar(agendamento);
            double precoOriginal = agendamentoSalvo.getServico().getPreco();

            Optional<DescontoFidelidade> descontoDisponivel =
                    fidelidadeService.buscarDescontoDisponivel(cliente.getId());

            double precoFinal = fidelidadeService.aplicarDesconto(cliente.getId(), precoOriginal);

            if (descontoDisponivel.isPresent() && precoFinal < precoOriginal) {
                redirectAttributes.addFlashAttribute("descontoAplicado", true);
                redirectAttributes.addFlashAttribute("precoFinal", precoFinal);
                redirectAttributes.addFlashAttribute("percentualDesconto",
                        descontoDisponivel.get().getPercentualDesconto());
            }

            redirectAttributes.addFlashAttribute("sucesso", "Agendamento realizado com sucesso!");
            return "redirect:/agendamentos/confirmacao?agendamentoId=" + agendamentoSalvo.getId();
        } catch (Exception e) {
            model.addAttribute("erro", "Erro ao agendar: " + e.getMessage());
            carregarDadosFormulario(model, authentication);
            return "agendamento-cliente-form";
        }
    }

    @GetMapping("/confirmacao")
    public String mostrarConfirmacao(@RequestParam Long agendamentoId, Model model) {
        Agendamento agendamento = agendamentoService.buscarPorId(agendamentoId)
                .orElseThrow(() -> new IllegalArgumentException("Agendamento não encontrado"));
        model.addAttribute("agendamento", agendamento);
        return "confirmacao-agendamento";
    }

    // LISTA DE AGENDAMENTOS DO CLIENTE COM FILTRO POR DATA
    @GetMapping
    public String listarAgendamentos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            Model model,
            Authentication authentication) {

        String email = getUsernameFromAuthentication(authentication);
        Cliente cliente = clienteService.buscarPorEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));

        List<Agendamento> agendamentos = agendamentoService.listarPorBarbeiro(cliente.getId());

        // Filtrar por data se fornecida
        if (data != null) {
            agendamentos = agendamentos.stream()
                    .filter(a -> a.getDataHora().toLocalDate().equals(data))
                    .collect(Collectors.toList());
        }

        model.addAttribute("agendamentos", agendamentos);
        model.addAttribute("dataFiltro", data);
        model.addAttribute("cartao", fidelidadeService.buscarCartao(cliente.getId()).orElse(null));
        model.addAttribute("temDesconto", fidelidadeService.buscarDescontoDisponivel(cliente.getId()).isPresent());
        ConfiguracaoFidelidade configFidelidade = fidelidadeService.buscarConfiguracaoAtual();
        model.addAttribute("cortesParaDesconto", configFidelidade.getCortesParaDesconto());
        model.addAttribute("percentualDescontoAtual", configFidelidade.getPercentualDesconto());
        return "lista-agendamentos";
    }

    // LISTA DE AGENDAMENTOS DO BARBEIRO COM FILTRO POR DATA
    @GetMapping("/barbeiro")
    public String listarAgendamentosBarbeiro(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            Model model,
            Authentication authentication) {

        String email = getUsernameFromAuthentication(authentication);
        List<Agendamento> agendamentos = agendamentoService.findByBarbeiroEmail(email);

        // Filtrar por data se fornecida
        if (data != null) {
            agendamentos = agendamentos.stream()
                    .filter(a -> a.getDataHora().toLocalDate().equals(data))
                    .collect(Collectors.toList());
        }

        model.addAttribute("agendamentos", agendamentos);
        model.addAttribute("dataFiltro", data);
        return "lista-agendamentos-barbeiro";
    }

    // CONCLUIR AGENDAMENTO (BARBEIRO)
    @PostMapping("/barbeiro/concluir/{id}")
    public String concluirAgendamento(@PathVariable Long id,
                                      Authentication authentication,
                                      RedirectAttributes redirectAttributes) {
        try {
            Agendamento agendamento = agendamentoService.buscarPorId(id)
                    .orElseThrow(() -> new IllegalArgumentException("Agendamento não encontrado"));

            String email = getUsernameFromAuthentication(authentication);
            if (!agendamento.getBarbeiro().getEmail().equals(email)) {
                throw new IllegalArgumentException("Você não tem permissão para modificar este agendamento");
            }

            agendamento.setStatus("CONCLUIDO");
            agendamentoService.salvar(agendamento);

            redirectAttributes.addFlashAttribute("sucesso", "Agendamento marcado como concluído!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao concluir agendamento: " + e.getMessage());
        }
        return "redirect:/agendamentos/barbeiro";
    }

    // CANCELAR AGENDAMENTO (BARBEIRO)
    @PostMapping("/barbeiro/cancelar/{id}")
    public String cancelarAgendamentoBarbeiro(@PathVariable Long id,
                                                Authentication authentication,
                                                RedirectAttributes redirectAttributes) {
        try {
            Agendamento agendamento = agendamentoService.buscarPorId(id)
                    .orElseThrow(() -> new IllegalArgumentException("Agendamento não encontrado"));

            String email = getUsernameFromAuthentication(authentication);
            if (!agendamento.getBarbeiro().getEmail().equals(email)) {
                throw new IllegalArgumentException("Você não tem permissão para modificar este agendamento");
            }

            agendamento.setStatus("CANCELADO");
            agendamentoService.salvar(agendamento);

            redirectAttributes.addFlashAttribute("sucesso", "Agendamento cancelado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao cancelar agendamento: " + e.getMessage());
        }
        return "redirect:/agendamentos/barbeiro";
    }

    // CANCELAR AGENDAMENTO (CLIENTE)
    @PostMapping("/cancelar/{id}")
    public String cancelarAgendamentoCliente(@PathVariable Long id,
                                               Authentication authentication,
                                               RedirectAttributes redirectAttributes) {
        try {
            Agendamento agendamento = agendamentoService.buscarPorId(id)
                    .orElseThrow(() -> new IllegalArgumentException("Agendamento não encontrado"));

            String email = getUsernameFromAuthentication(authentication);
            if (!agendamento.getCliente().getEmail().equals(email)) {
                throw new IllegalArgumentException("Você não tem permissão para cancelar este agendamento");
            }

            // Verificar se o agendamento pode ser cancelado (ex: com 2h de antecedência)
            if (agendamento.getDataHora().minusHours(2).isBefore(LocalDateTime.now())) {
                redirectAttributes.addFlashAttribute("erro",
                        "Cancelamento deve ser feito com no mínimo 2 horas de antecedência");
                return "redirect:/agendamentos";
            }

            agendamento.setStatus("CANCELADO");
            agendamentoService.salvar(agendamento);

            redirectAttributes.addFlashAttribute("sucesso", "Agendamento cancelado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao cancelar agendamento: " + e.getMessage());
        }
        return "redirect:/agendamentos";
    }

    // EDITAR AGENDAMENTO (CLIENTE)
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEdicao(@PathVariable Long id,
                                          Model model,
                                          Authentication authentication) {
        try {
            Agendamento agendamento = agendamentoService.buscarPorId(id)
                    .orElseThrow(() -> new IllegalArgumentException("Agendamento não encontrado"));

            String email = getUsernameFromAuthentication(authentication);
            if (!agendamento.getCliente().getEmail().equals(email)) {
                throw new IllegalArgumentException("Você não tem permissão para editar este agendamento");
            }

            if (!agendamento.getStatus().equals("AGENDADO")) {
                throw new IllegalArgumentException("Apenas agendamentos pendentes podem ser editados");
            }

            model.addAttribute("agendamento", agendamento);
            carregarDadosFormulario(model, authentication);
            return "agendamento-cliente-form";
        } catch (Exception e) {
            model.addAttribute("erro", e.getMessage());
            return "redirect:/agendamentos";
        }
    }

    private void carregarDadosFormulario(Model model, Authentication authentication) {
        model.addAttribute("servicos", servicoService.listarTodos());
        model.addAttribute("barbeiros", barbeiroService.listarTodos());

        if (authentication == null) {
            return;
        }

        String email = getUsernameFromAuthentication(authentication);
        if (email == null) {
            return;
        }

        Optional<Cliente> clienteOpt = clienteService.buscarPorEmail(email);
        if (clienteOpt.isEmpty()) {
            return;
        }

        fidelidadeService.buscarDescontoDisponivel(clienteOpt.get().getId()).ifPresent(desconto -> {
            model.addAttribute("temDesconto", true);
            model.addAttribute("percentualDesconto", desconto.getPercentualDesconto());
        });
    }

    private String getUsernameFromAuthentication(Authentication authentication) {
        if (authentication == null) {
            throw new IllegalArgumentException("Usuário não autenticado");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof org.springframework.security.oauth2.core.user.OAuth2User oauth2User) {
            return (String) oauth2User.getAttributes().get("email");
        }
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        return authentication.getName();
    }
}


