package com.barbearia.agendamento.service;

import com.barbearia.agendamento.model.Agendamento;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class    EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    @Value("${app.mail.from-name}")
    private String fromName;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Enviado logo após o cliente criar um agendamento.
     */
    @Async
    public void enviarConfirmacaoAgendamento(Agendamento ag) {
        String para = ag.getCliente().getEmail();
        String assunto = "✅ Agendamento confirmado — Barbearia Souza";
        String corpo = buildHtmlConfirmacao(ag);
        enviar(para, assunto, corpo);
    }

    /**
     * Enviado quando o admin ou barbeiro cancela um agendamento.
     */
    @Async
    public void enviarCancelamentoAgendamento(Agendamento ag) {
        String para = ag.getCliente().getEmail();
        String assunto = "❌ Agendamento cancelado — Barbearia Souza";
        String corpo = buildHtmlCancelamento(ag);
        enviar(para, assunto, corpo);
    }

    // ─────────────────────────────────────────────
    // Métodos internos
    // ─────────────────────────────────────────────

    private void enviar(String para, String assunto, String corpoHtml) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(from, fromName);
            helper.setTo(para);
            helper.setSubject(assunto);
            helper.setText(corpoHtml, true); // true = é HTML
            mailSender.send(msg);
            log.info("E-mail enviado para {} | Assunto: {}", para, assunto);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            // Loga o erro mas não deixa a exception derrubar o fluxo principal
            log.error("Falha ao enviar e-mail para {}: {}", para, e.getMessage());
        }
    }

    private String buildHtmlConfirmacao(Agendamento ag) {
        String dataHora = ag.getDataHora().format(FMT);
        double preco = ag.getServico().getPreco();

        return """
            <!DOCTYPE html>
            <html lang="pt-br">
            <head><meta charset="UTF-8"></head>
            <body style="margin:0;padding:0;background:#0D121E;font-family:'Segoe UI',Arial,sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0">
                <tr>
                  <td align="center" style="padding:40px 16px;">
                    <table width="520" cellpadding="0" cellspacing="0"
                           style="background:#1a1f2e;border-radius:16px;overflow:hidden;
                                  border:1px solid #2d3446;">

                      <!-- Cabeçalho -->
                      <tr>
                        <td style="background:#F0B35B;padding:28px;text-align:center;">
                          <h1 style="margin:0;color:#0D121E;font-size:22px;letter-spacing:1px;">
                            ✂️ Barbearia Souza
                          </h1>
                        </td>
                      </tr>

                      <!-- Ícone de sucesso -->
                      <tr>
                        <td style="padding:32px 32px 0;text-align:center;">
                          <div style="width:72px;height:72px;background:#28a745;border-radius:50%%;
                                      display:inline-flex;align-items:center;justify-content:center;
                                      font-size:36px;margin-bottom:16px;">✓</div>
                          <h2 style="margin:0 0 8px;color:#ffffff;font-size:20px;">
                            Agendamento Confirmado!
                          </h2>
                          <p style="margin:0;color:#8b93a5;font-size:14px;">
                            Olá, <strong style="color:#F0B35B;">%s</strong>!
                            Seu horário está reservado.
                          </p>
                        </td>
                      </tr>

                      <!-- Detalhes -->
                      <tr>
                        <td style="padding:24px 32px;">
                          <table width="100%%" cellpadding="0" cellspacing="0"
                                 style="background:#252b3b;border-radius:12px;
                                        border:1px solid #2d3446;">
                            %s
                          </table>
                        </td>
                      </tr>

                      <!-- Aviso -->
                      <tr>
                        <td style="padding:0 32px 24px;">
                          <p style="margin:0;background:#1e3a5f;border:1px solid #2d6a9f;
                                    border-radius:8px;padding:12px 16px;color:#7ec8e3;
                                    font-size:13px;text-align:center;">
                            ⚠️ Cancelamentos devem ser feitos com no mínimo 2 horas de antecedência.
                          </p>
                        </td>
                      </tr>

                      <!-- Rodapé -->
                      <tr>
                        <td style="padding:20px;text-align:center;border-top:1px solid #2d3446;">
                          <p style="margin:0;color:#4a5568;font-size:12px;">
                            © 2025 Barbearia Souza · Todos os direitos reservados
                          </p>
                        </td>
                      </tr>

                    </table>
                  </td>
                </tr>
              </table>
            </body>
            </html>
            """.formatted(
                ag.getCliente().getNome(),
                buildLinhasDetalhes(ag, dataHora, preco)
        );
    }

    private String buildHtmlCancelamento(Agendamento ag) {
        String dataHora = ag.getDataHora().format(FMT);
        double preco = ag.getServico().getPreco();

        return """
            <!DOCTYPE html>
            <html lang="pt-br">
            <head><meta charset="UTF-8"></head>
            <body style="margin:0;padding:0;background:#0D121E;font-family:'Segoe UI',Arial,sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0">
                <tr>
                  <td align="center" style="padding:40px 16px;">
                    <table width="520" cellpadding="0" cellspacing="0"
                           style="background:#1a1f2e;border-radius:16px;overflow:hidden;
                                  border:1px solid #2d3446;">
                      <tr>
                        <td style="background:#dc3545;padding:28px;text-align:center;">
                          <h1 style="margin:0;color:#ffffff;font-size:22px;letter-spacing:1px;">
                            ✂️ Barbearia Souza
                          </h1>
                        </td>
                      </tr>
                      <tr>
                        <td style="padding:32px 32px 0;text-align:center;">
                          <div style="width:72px;height:72px;background:#dc3545;border-radius:50%%;
                                      display:inline-flex;align-items:center;justify-content:center;
                                      font-size:36px;margin-bottom:16px;">✕</div>
                          <h2 style="margin:0 0 8px;color:#ffffff;font-size:20px;">
                            Agendamento Cancelado
                          </h2>
                          <p style="margin:0;color:#8b93a5;font-size:14px;">
                            Olá, <strong style="color:#F0B35B;">%s</strong>.
                            Infelizmente seu agendamento foi cancelado.
                          </p>
                        </td>
                      </tr>
                      <tr>
                        <td style="padding:24px 32px;">
                          <table width="100%%" cellpadding="0" cellspacing="0"
                                 style="background:#252b3b;border-radius:12px;
                                        border:1px solid #2d3446;">
                            %s
                          </table>
                        </td>
                      </tr>
                      <tr>
                        <td style="padding:0 32px 24px;">
                          <p style="margin:0;background:#3a1a1a;border:1px solid #6b2d2d;
                                    border-radius:8px;padding:12px 16px;color:#e88;
                                    font-size:13px;text-align:center;">
                            Deseja reagendar? Acesse nosso site e escolha um novo horário.
                          </p>
                        </td>
                      </tr>
                      <tr>
                        <td style="padding:20px;text-align:center;border-top:1px solid #2d3446;">
                          <p style="margin:0;color:#4a5568;font-size:12px;">
                            © 2025 Barbearia Souza · Todos os direitos reservados
                          </p>
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>
              </table>
            </body>
            </html>
            """.formatted(
                ag.getCliente().getNome(),
                buildLinhasDetalhes(ag, dataHora, preco)
        );
    }

    private String buildLinhasDetalhes(Agendamento ag, String dataHora, double preco) {
        return linhaDetalhe("✂️", "Serviço", ag.getServico().getNome())
                + linhaDetalhe("👤", "Barbeiro", ag.getBarbeiro().getNome())
                + linhaDetalhe("📅", "Data e horário", dataHora)
                + linhaDetalhe("💰", "Valor", String.format(Locale.forLanguageTag("pt-BR"),
                "R$ %.2f", preco));
    }

    private String linhaDetalhe(String icone, String rotulo, String valor) {
        return """
            <tr>
              <td style="padding:14px 18px;border-bottom:1px solid #2d3446;">
                <span style="color:#8b93a5;font-size:13px;">%s %s</span><br>
                <strong style="color:#ffffff;font-size:15px;">%s</strong>
              </td>
            </tr>
            """.formatted(icone, rotulo, valor);
    }
}