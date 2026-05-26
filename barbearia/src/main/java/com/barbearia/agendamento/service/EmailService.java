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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    @Value("${app.mail.from-name}")
    private String fromName;

    @Value("${app.url:http://localhost:4200}")
    private String appUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void enviarConfirmacaoAgendamento(Agendamento ag) {
        enviar(
                ag.getCliente().getEmail(),
                "✅ Agendamento confirmado — Barbearia Souza",
                buildHtmlConfirmacao(ag)
        );
    }

    @Async
    public void enviarCancelamentoAgendamento(Agendamento ag) {
        enviar(
                ag.getCliente().getEmail(),
                "❌ Agendamento cancelado — Barbearia Souza",
                buildHtmlCancelamento(ag)
        );
    }

    @Async
    public void enviarCancelamentoFeriado(Agendamento ag, String motivo, LocalDate dataFeriado) {
        String dataFmt = dataFeriado.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        var dest = ag.getCliente().getEmail();
        enviar(dest,
                "📢 Agendamento cancelado",
                buildHtmlCancelamentoFeriado(ag, motivo, dataFmt)
        );
    }

    @Async
    public void enviarNotificacaoBarbeiro(Agendamento ag, String motivo, LocalDate dataFeriado) {
        String dataFmt = dataFeriado.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        var barbeiroNome = ag.getBarbeiro().getNome();
        var clienteNome = ag.getCliente().getNome();
        var servicoNome = ag.getServico().getNome();
        var hora = ag.getDataHora().format(DateTimeFormatter.ofPattern("HH:mm"));
        var dest = ag.getBarbeiro().getEmail();
        enviar(dest,
                "📢 Agendamento cancelado — " + dataFmt,
                buildHtmlNotificacaoBarbeiro(ag, motivo, dataFmt, clienteNome, servicoNome, hora)
        );
    }

    private void enviar(String para, String assunto, String corpoHtml) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(from, fromName);
            helper.setTo(para);
            helper.setSubject(assunto);
            helper.setText(corpoHtml, true);
            mailSender.send(msg);
            log.info("E-mail enviado para {} | Assunto: {}", para, assunto);
        } catch (Exception e) {
            log.error("Falha ao enviar e-mail para {}: {} ({})", para, e.getMessage(), e.getClass().getSimpleName());
        }
    }

    private String buildHtmlConfirmacao(Agendamento ag) {
        String dataHora = ag.getDataHora().format(FMT);
        String preco    = String.format(Locale.forLanguageTag("pt-BR"), "R$\u00a0%.2f", ag.getServico().getPreco());
        String nome     = ag.getCliente().getNome();
        String servico  = ag.getServico().getNome();
        String barbeiro = ag.getBarbeiro().getNome();
        String link     = appUrl + "/agendamentos";

        return """
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="pt-BR">
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <meta name="color-scheme" content="dark" />
  <meta name="supported-color-schemes" content="dark" />
  <title>Agendamento Confirmado — Barbearia Souza</title>
  <style type="text/css">
    /* Reset */
    body, table, td, a { -webkit-text-size-adjust: 100%%; -ms-text-size-adjust: 100%%; }
    table, td { mso-table-lspace: 0pt; mso-table-rspace: 0pt; }
    img { -ms-interpolation-mode: bicubic; border: 0; outline: none; text-decoration: none; }
    body { margin: 0 !important; padding: 0 !important; background-color: #0B0F19; width: 100%% !important; }

    /* Responsive */
    @media only screen and (max-width: 620px) {
      .email-container { width: 100%% !important; margin: auto !important; }
      .fluid { max-width: 100%% !important; height: auto !important; margin: auto !important; }
      .stack-column, .stack-column-center { display: block !important; width: 100%% !important; max-width: 100%% !important; }
      .stack-column-center { text-align: center !important; }
      .center-on-narrow { text-align: center !important; display: block !important; margin: 0 auto !important; float: none !important; }
      .hero-padding { padding: 32px 20px !important; }
      .body-padding { padding: 24px 16px !important; }
      .cta-btn { padding: 14px 24px !important; font-size: 15px !important; }
      .detail-label, .detail-value { display: block !important; width: 100%% !important; text-align: left !important; }
      .detail-value { padding-top: 2px !important; padding-left: 0 !important; }
    }
  </style>
</head>
<body style="margin:0;padding:0;background-color:#0B0F19;font-family:Georgia,'Times New Roman',serif;">

<!-- Preheader (hidden) -->
<div style="display:none;font-size:1px;color:#0B0F19;line-height:1px;max-height:0px;max-width:0px;opacity:0;overflow:hidden;">
  Seu horário na Barbearia Souza foi confirmado para %s.
</div>

<!-- Outer wrapper -->
<table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" style="background-color:#0B0F19;">
<tr>
<td align="center" style="padding:24px 12px;">

  <!-- Email container -->
  <table class="email-container" role="presentation" cellspacing="0" cellpadding="0" border="0" width="600"
         style="background-color:#111827;border-radius:16px;overflow:hidden;border:1px solid #1F2937;">

    <!-- ═══════════════ TOPO / LOGO BAR ═══════════════ -->
    <tr>
      <td style="background-color:#0B0F19;padding:0;">
        <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%">
          <tr>
            <td style="padding:18px 32px;border-bottom:2px solid #FBBF24;">
              <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%">
                <tr>
                  <td style="vertical-align:middle;">
                    <!-- Scissors icon SVG inline -->
                    <table role="presentation" cellspacing="0" cellpadding="0" border="0">
                      <tr>
                        <td style="vertical-align:middle;padding-right:10px;">
                          <div style="width:34px;height:34px;background-color:#FBBF24;border-radius:8px;text-align:center;line-height:34px;font-size:18px;">
                            &#9988;
                          </div>
                        </td>
                        <td style="vertical-align:middle;">
                          <span style="font-family:Georgia,'Times New Roman',serif;font-size:18px;font-weight:bold;color:#FFFFFF;letter-spacing:0.5px;">
                            Barbearia <span style="color:#FBBF24;">Souza</span>
                          </span>
                        </td>
                      </tr>
                    </table>
                  </td>
                  <td align="right" style="vertical-align:middle;">
                    <span style="font-family:Arial,sans-serif;font-size:11px;color:#6B7280;letter-spacing:1.5px;text-transform:uppercase;">
                      Confirmação
                    </span>
                  </td>
                </tr>
              </table>
            </td>
          </tr>
        </table>
      </td>
    </tr>

    <!-- ═══════════════ HERO BANNER ═══════════════ -->
    <tr>
      <td class="hero-padding" align="center"
          style="padding:52px 40px 44px;background-color:#111827;background-image:linear-gradient(135deg,#111827 0%%,#0f1929 50%%,#111827 100%%);">

        <!-- Status badge -->
        <table role="presentation" cellspacing="0" cellpadding="0" border="0" align="center" style="margin-bottom:24px;">
          <tr>
            <td style="background-color:#052e16;border:1px solid #16a34a;border-radius:999px;padding:6px 16px;">
              <table role="presentation" cellspacing="0" cellpadding="0" border="0">
                <tr>
                  <td style="padding-right:6px;vertical-align:middle;font-size:13px;line-height:1;">&#10004;</td>
                  <td style="font-family:Arial,sans-serif;font-size:12px;font-weight:bold;color:#4ade80;letter-spacing:1.5px;text-transform:uppercase;white-space:nowrap;vertical-align:middle;">
                    Agendado com sucesso
                  </td>
                </tr>
              </table>
            </td>
          </tr>
        </table>

        <!-- Main heading -->
        <h1 style="margin:0 0 12px;font-family:Georgia,'Times New Roman',serif;font-size:32px;font-weight:bold;color:#FFFFFF;line-height:1.2;letter-spacing:-0.5px;">
          Seu horário está<br/>
          <span style="color:#FBBF24;">confirmado!</span>
        </h1>

        <!-- Subtext -->
        <p style="margin:0;font-family:Arial,sans-serif;font-size:15px;color:#9CA3AF;line-height:1.6;">
          Olá, <strong style="color:#FBBF24;">%s</strong>! Estamos te esperando.<br/>
          Confira os detalhes do seu agendamento abaixo.
        </p>

      </td>
    </tr>

    <!-- ═══════════════ DIVIDER com ornamento ═══════════════ -->
    <tr>
      <td style="padding:0 32px;">
        <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%">
          <tr>
            <td style="height:1px;background:linear-gradient(to right,transparent,#FBBF24,transparent);font-size:0;line-height:0;">&nbsp;</td>
          </tr>
        </table>
      </td>
    </tr>

    <!-- ═══════════════ DETAILS CARD ═══════════════ -->
    <tr>
      <td class="body-padding" style="padding:32px 32px 28px;">

        <!-- Card container -->
        <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%"
               style="background-color:#0B0F19;border-radius:12px;border:1px solid #1F2937;overflow:hidden;">

          <!-- Card header -->
          <tr>
            <td style="padding:16px 24px;background-color:#161D2F;border-bottom:1px solid #1F2937;">
              <span style="font-family:Arial,sans-serif;font-size:11px;font-weight:bold;color:#FBBF24;letter-spacing:2px;text-transform:uppercase;">
                Detalhes do Agendamento
              </span>
            </td>
          </tr>

          <!-- Row: Serviço -->
          <tr>
            <td style="padding:20px 24px 0;">
              <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%">
                <tr>
                  <td class="detail-label" width="38%%" style="vertical-align:top;padding-bottom:20px;">
                    <table role="presentation" cellspacing="0" cellpadding="0" border="0">
                      <tr>
                        <td style="padding-right:10px;vertical-align:middle;">
                          <div style="width:32px;height:32px;background-color:#161D2F;border-radius:8px;border:1px solid #374151;text-align:center;line-height:32px;font-size:14px;">
                            &#9986;
                          </div>
                        </td>
                        <td style="vertical-align:middle;">
                          <span style="font-family:Arial,sans-serif;font-size:11px;color:#6B7280;letter-spacing:1px;text-transform:uppercase;display:block;">Serviço</span>
                        </td>
                      </tr>
                    </table>
                  </td>
                  <td class="detail-value" align="right" style="vertical-align:top;padding-bottom:20px;">
                    <span style="font-family:Georgia,'Times New Roman',serif;font-size:16px;font-weight:bold;color:#FFFFFF;">%s</span>
                  </td>
                </tr>
              </table>
            </td>
          </tr>

          <!-- Divider row -->
          <tr><td style="padding:0 24px;"><div style="height:1px;background-color:#1F2937;font-size:0;line-height:0;">&nbsp;</div></td></tr>

          <!-- Row: Barbeiro -->
          <tr>
            <td style="padding:20px 24px 0;">
              <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%">
                <tr>
                  <td class="detail-label" width="38%%" style="vertical-align:top;padding-bottom:20px;">
                    <table role="presentation" cellspacing="0" cellpadding="0" border="0">
                      <tr>
                        <td style="padding-right:10px;vertical-align:middle;">
                          <div style="width:32px;height:32px;background-color:#161D2F;border-radius:8px;border:1px solid #374151;text-align:center;line-height:32px;font-size:14px;">
                            &#128104;
                          </div>
                        </td>
                        <td style="vertical-align:middle;">
                          <span style="font-family:Arial,sans-serif;font-size:11px;color:#6B7280;letter-spacing:1px;text-transform:uppercase;display:block;">Profissional</span>
                        </td>
                      </tr>
                    </table>
                  </td>
                  <td class="detail-value" align="right" style="vertical-align:top;padding-bottom:20px;">
                    <span style="font-family:Georgia,'Times New Roman',serif;font-size:16px;color:#FFFFFF;">%s</span>
                  </td>
                </tr>
              </table>
            </td>
          </tr>

          <!-- Divider row -->
          <tr><td style="padding:0 24px;"><div style="height:1px;background-color:#1F2937;font-size:0;line-height:0;">&nbsp;</div></td></tr>

          <!-- Row: Data -->
          <tr>
            <td style="padding:20px 24px 0;">
              <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%">
                <tr>
                  <td class="detail-label" width="38%%" style="vertical-align:top;padding-bottom:20px;">
                    <table role="presentation" cellspacing="0" cellpadding="0" border="0">
                      <tr>
                        <td style="padding-right:10px;vertical-align:middle;">
                          <div style="width:32px;height:32px;background-color:#161D2F;border-radius:8px;border:1px solid #374151;text-align:center;line-height:32px;font-size:14px;">
                            &#128197;
                          </div>
                        </td>
                        <td style="vertical-align:middle;">
                          <span style="font-family:Arial,sans-serif;font-size:11px;color:#6B7280;letter-spacing:1px;text-transform:uppercase;display:block;">Data e Hora</span>
                        </td>
                      </tr>
                    </table>
                  </td>
                  <td class="detail-value" align="right" style="vertical-align:top;padding-bottom:20px;">
                    <span style="font-family:Georgia,'Times New Roman',serif;font-size:16px;color:#FFFFFF;">%s</span>
                  </td>
                </tr>
              </table>
            </td>
          </tr>

          <!-- Divider row -->
          <tr><td style="padding:0 24px;"><div style="height:1px;background-color:#1F2937;font-size:0;line-height:0;">&nbsp;</div></td></tr>

          <!-- Row: Valor (destaque dourado) -->
          <tr>
            <td style="padding:20px 24px;background-color:#161D2F;border-radius:0 0 12px 12px;">
              <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%">
                <tr>
                  <td class="detail-label" width="38%%" style="vertical-align:middle;">
                    <table role="presentation" cellspacing="0" cellpadding="0" border="0">
                      <tr>
                        <td style="padding-right:10px;vertical-align:middle;">
                          <div style="width:32px;height:32px;background-color:#161D2F;border-radius:8px;border:1px solid #374151;text-align:center;line-height:32px;font-size:14px;">
                            &#128176;
                          </div>
                        </td>
                        <td style="vertical-align:middle;">
                          <span style="font-family:Arial,sans-serif;font-size:11px;color:#9CA3AF;letter-spacing:1px;text-transform:uppercase;display:block;">Valor</span>
                        </td>
                      </tr>
                    </table>
                  </td>
                  <td class="detail-value" align="right" style="vertical-align:middle;">
                    <span style="font-family:Georgia,'Times New Roman',serif;font-size:22px;font-weight:bold;color:#FBBF24;">%s</span>
                  </td>
                </tr>
              </table>
            </td>
          </tr>

        </table>
      </td>
    </tr>

    <!-- ═══════════════ LEMBRETE ═══════════════ -->
    <tr>
      <td style="padding:0 32px 28px;">
        <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%"
               style="background-color:#1a1200;border-radius:10px;border-left:3px solid #FBBF24;">
          <tr>
            <td style="padding:14px 18px;">
              <p style="margin:0;font-family:Arial,sans-serif;font-size:13px;color:#D97706;line-height:1.6;">
                <strong>&#9888; Lembrete:</strong> Em caso de imprevistos, cancele com pelo menos
                <strong>2 horas de antecedência!</strong>
              </p>
            </td>
          </tr>
        </table>
      </td>
    </tr>

    <!-- ═══════════════ CTA BUTTON ═══════════════ -->
    <tr>
      <td align="center" style="padding:4px 32px 40px;">
        <table role="presentation" cellspacing="0" cellpadding="0" border="0">
          <tr>
            <td style="border-radius:10px;background-color:#FBBF24;box-shadow:0 4px 24px rgba(251,191,36,0.35);">
              <a class="cta-btn" href="%s"
                 style="display:inline-block;padding:16px 36px;font-family:Arial,sans-serif;font-size:15px;font-weight:bold;color:#000000;text-decoration:none;border-radius:10px;letter-spacing:0.5px;white-space:nowrap;">
                Ver meus agendamentos &rarr;
              </a>
            </td>
          </tr>
        </table>
      </td>
    </tr>

    <!-- ═══════════════ DIVIDER ═══════════════ -->
    <tr>
      <td style="padding:0 32px;">
        <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%">
          <tr>
            <td style="height:1px;background-color:#1F2937;font-size:0;line-height:0;">&nbsp;</td>
          </tr>
        </table>
      </td>
    </tr>

    <!-- ═══════════════ FOOTER ═══════════════ -->
    <tr>
      <td align="center" style="padding:28px 32px;">
        <p style="margin:0 0 6px;font-family:Georgia,'Times New Roman',serif;font-size:15px;color:#FBBF24;font-weight:bold;letter-spacing:0.5px;">
          Barbearia Souza
        </p>
        <p style="margin:0 0 10px;font-family:Arial,sans-serif;font-size:12px;color:#4B5563;line-height:1.6;">
          Este e-mail foi enviado automaticamente. Por favor, não responda diretamente.
        </p>
        <p style="margin:0;font-family:Arial,sans-serif;font-size:11px;color:#374151;">
          &copy; 2026 Barbearia Souza. Todos os direitos reservados.
        </p>
      </td>
    </tr>

  </table>
  <!-- /Email container -->

</td>
</tr>
</table>
<!-- /Outer wrapper -->

</body>
</html>
""".formatted(dataHora, nome, servico, barbeiro, dataHora, preco, link);
    }

    private String buildHtmlCancelamento(Agendamento ag) {
        String dataHora = ag.getDataHora().format(FMT);
        String nome     = ag.getCliente().getNome();
        String servico  = ag.getServico().getNome();
        String barbeiro = ag.getBarbeiro().getNome();
        String link     = appUrl + "/agendamentos/novo";

        return """
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="pt-BR">
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Agendamento Cancelado — Barbearia Souza</title>
  <style type="text/css">
    body, table, td, a { -webkit-text-size-adjust: 100%%; -ms-text-size-adjust: 100%%; }
    table, td { mso-table-lspace: 0pt; mso-table-rspace: 0pt; }
    body { margin: 0 !important; padding: 0 !important; background-color: #0B0F19; width: 100%% !important; }
    @media only screen and (max-width: 620px) {
      .email-container { width: 100%% !important; }
      .hero-padding { padding: 32px 20px !important; }
      .body-padding { padding: 24px 16px !important; }
      .cta-btn { padding: 14px 24px !important; }
    }
  </style>
</head>
<body style="margin:0;padding:0;background-color:#0B0F19;font-family:Georgia,'Times New Roman',serif;">

<table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" style="background-color:#0B0F19;">
<tr>
<td align="center" style="padding:24px 12px;">

  <table class="email-container" role="presentation" cellspacing="0" cellpadding="0" border="0" width="600"
         style="background-color:#111827;border-radius:16px;overflow:hidden;border:1px solid #1F2937;">

    <!-- Header -->
    <tr>
      <td style="background-color:#0B0F19;padding:0;">
        <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%">
          <tr>
            <td style="padding:18px 32px;border-bottom:2px solid #ef4444;">
              <table role="presentation" cellspacing="0" cellpadding="0" border="0">
                <tr>
                  <td style="vertical-align:middle;padding-right:10px;">
                    <div style="width:34px;height:34px;background-color:#FBBF24;border-radius:8px;text-align:center;line-height:34px;font-size:18px;">&#9988;</div>
                  </td>
                  <td style="vertical-align:middle;">
                    <span style="font-family:Georgia,'Times New Roman',serif;font-size:18px;font-weight:bold;color:#FFFFFF;">
                      Barbearia <span style="color:#FBBF24;">Souza</span>
                    </span>
                  </td>
                </tr>
              </table>
            </td>
          </tr>
        </table>
      </td>
    </tr>

    <!-- Hero -->
    <tr>
      <td class="hero-padding" align="center" style="padding:52px 40px 44px;background-color:#111827;">

        <table role="presentation" cellspacing="0" cellpadding="0" border="0" align="center" style="margin-bottom:24px;">
          <tr>
            <td style="background-color:#2d0707;border:1px solid #ef4444;border-radius:999px;padding:6px 16px;">
              <span style="font-family:Arial,sans-serif;font-size:12px;font-weight:bold;color:#f87171;letter-spacing:1.5px;text-transform:uppercase;">
                &#10007; Agendamento cancelado
              </span>
            </td>
          </tr>
        </table>

        <h1 style="margin:0 0 12px;font-family:Georgia,'Times New Roman',serif;font-size:30px;font-weight:bold;color:#FFFFFF;line-height:1.2;">
          Seu agendamento foi<br/>
          <span style="color:#f87171;">cancelado</span>
        </h1>

        <p style="margin:0;font-family:Arial,sans-serif;font-size:15px;color:#9CA3AF;line-height:1.6;">
          Olá, <strong style="color:#FBBF24;">%s</strong>. Seu horário foi removido.<br/>
          Que tal agendar um novo horário?
        </p>
      </td>
    </tr>

    <!-- Divider -->
    <tr>
      <td style="padding:0 32px;">
        <div style="height:1px;background-color:#1F2937;font-size:0;line-height:0;">&nbsp;</div>
      </td>
    </tr>

    <!-- Details -->
    <tr>
      <td class="body-padding" style="padding:32px 32px 28px;">
        <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%"
               style="background-color:#0B0F19;border-radius:12px;border:1px solid #1F2937;">
          <tr>
            <td style="padding:16px 24px;background-color:#161D2F;border-bottom:1px solid #1F2937;">
              <span style="font-family:Arial,sans-serif;font-size:11px;font-weight:bold;color:#9CA3AF;letter-spacing:2px;text-transform:uppercase;">
                Agendamento Cancelado
              </span>
            </td>
          </tr>
          <tr>
            <td style="padding:20px 24px 0;">
              <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%">
                <tr>
                  <td style="padding-bottom:16px;border-bottom:1px solid #1F2937;">
                    <span style="font-family:Arial,sans-serif;font-size:11px;color:#6B7280;letter-spacing:1px;text-transform:uppercase;display:block;margin-bottom:4px;">Serviço</span>
                    <span style="font-family:Georgia,'Times New Roman',serif;font-size:16px;color:#9CA3AF;text-decoration:line-through;">%s</span>
                  </td>
                </tr>
                <tr>
                  <td style="padding-top:16px;padding-bottom:16px;border-bottom:1px solid #1F2937;">
                    <span style="font-family:Arial,sans-serif;font-size:11px;color:#6B7280;letter-spacing:1px;text-transform:uppercase;display:block;margin-bottom:4px;">Profissional</span>
                    <span style="font-family:Georgia,'Times New Roman',serif;font-size:16px;color:#9CA3AF;">%s</span>
                  </td>
                </tr>
                <tr>
                  <td style="padding-top:16px;padding-bottom:20px;">
                    <span style="font-family:Arial,sans-serif;font-size:11px;color:#6B7280;letter-spacing:1px;text-transform:uppercase;display:block;margin-bottom:4px;">Data cancelada</span>
                    <span style="font-family:Georgia,'Times New Roman',serif;font-size:16px;color:#9CA3AF;text-decoration:line-through;">%s</span>
                  </td>
                </tr>
              </table>
            </td>
          </tr>
        </table>
      </td>
    </tr>

    <!-- CTA -->
    <tr>
      <td align="center" style="padding:4px 32px 40px;">
        <table role="presentation" cellspacing="0" cellpadding="0" border="0">
          <tr>
            <td style="border-radius:10px;background-color:#FBBF24;">
              <a class="cta-btn" href="%s"
                 style="display:inline-block;padding:16px 36px;font-family:Arial,sans-serif;font-size:15px;font-weight:bold;color:#000000;text-decoration:none;border-radius:10px;white-space:nowrap;">
                Fazer novo agendamento &rarr;
              </a>
            </td>
          </tr>
        </table>
      </td>
    </tr>

    <!-- Footer -->
    <tr>
      <td style="padding:0 32px;">
        <div style="height:1px;background-color:#1F2937;font-size:0;line-height:0;">&nbsp;</div>
      </td>
    </tr>
    <tr>
      <td align="center" style="padding:28px 32px;">
        <p style="margin:0 0 6px;font-family:Georgia,'Times New Roman',serif;font-size:15px;color:#FBBF24;font-weight:bold;">Barbearia Souza</p>
        <p style="margin:0;font-family:Arial,sans-serif;font-size:11px;color:#374151;">&copy; 2026 Barbearia Souza. Todos os direitos reservados.</p>
      </td>
    </tr>

  </table>
</td>
</tr>
</table>
</body>
</html>
""".formatted(nome, servico, barbeiro, dataHora, link);
    }

    private String buildHtmlCancelamentoFeriado(Agendamento ag, String motivo, String dataFmt) {
        String nome = ag.getCliente().getNome();
        String servico = ag.getServico().getNome();
        String barbeiro = ag.getBarbeiro().getNome();
        String hora = ag.getDataHora().format(DateTimeFormatter.ofPattern("HH:mm"));
        String link = appUrl + "/agendamentos/novo";

        return """
<!DOCTYPE html>
<html lang="pt-BR">
<head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Agendamento Cancelado</title>
<style>
  body,table,td,a{-webkit-text-size-adjust:100%%;-ms-text-size-adjust:100%%}
  body{margin:0;padding:0;background-color:#0B0F19;font-family:Georgia,'Times New Roman',serif}
  @media only screen and (max-width:620px){.email-container{width:100%%!important}.hero-padding{padding:32px 20px!important}.body-padding{padding:24px 16px!important}.cta-btn{padding:14px 24px!important}}
</style>
</head>
<body style="margin:0;padding:0;background-color:#0B0F19;">
<table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" style="background-color:#0B0F19;"><tr><td align="center" style="padding:24px 12px;">
<table class="email-container" role="presentation" cellspacing="0" cellpadding="0" border="0" width="600" style="background-color:#111827;border-radius:16px;overflow:hidden;border:1px solid #1F2937;">
<tr><td style="padding:18px 32px;border-bottom:2px solid #ef4444;">
<span style="font-family:Georgia,'Times New Roman',serif;font-size:18px;font-weight:bold;color:#FFFFFF;">Barbearia <span style="color:#FBBF24;">Souza</span></span>
</td></tr>
<tr><td class="hero-padding" align="center" style="padding:40px 32px 32px;">
<span style="display:inline-block;background-color:#2d0707;border:1px solid #ef4444;border-radius:999px;padding:6px 16px;font-family:Arial,sans-serif;font-size:12px;font-weight:bold;color:#f87171;text-transform:uppercase;letter-spacing:1.5px;">&#10007; Cancelado</span>
<h1 style="margin:24px 0 12px;font-size:28px;font-weight:bold;color:#FFFFFF;">Agendamento cancelado</h1>
<p style="margin:0 0 4px;font-family:Arial,sans-serif;font-size:15px;color:#9CA3AF;line-height:1.6;">Olá, <strong style="color:#FBBF24;">%s</strong>!</p>
<p style="margin:0;font-family:Arial,sans-serif;font-size:15px;color:#9CA3AF;line-height:1.6;">Seu agendamento do dia <strong>%s</strong> às <strong>%s</strong> foi cancelado.</p>
<p style="margin:12px 0 0;font-family:Arial,sans-serif;font-size:14px;color:#D97706;line-height:1.5;"><strong>Motivo:</strong> %s</p>
</td></tr>
<tr><td class="body-padding" style="padding:0 32px 24px;">
<table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" style="background-color:#0B0F19;border-radius:12px;border:1px solid #1F2937;">
<tr><td style="padding:12px 20px;background-color:#161D2F;border-bottom:1px solid #1F2937;"><span style="font-family:Arial,sans-serif;font-size:11px;font-weight:bold;color:#9CA3AF;letter-spacing:2px;text-transform:uppercase;">Detalhes do cancelamento</span></td></tr>
<tr><td style="padding:16px 20px;">
<span style="font-family:Arial,sans-serif;font-size:11px;color:#6B7280;text-transform:uppercase;letter-spacing:1px;">Serviço</span>
<p style="margin:4px 0 12px;font-family:Georgia,serif;font-size:15px;color:#9CA3AF;text-decoration:line-through;">%s</p>
<span style="font-family:Arial,sans-serif;font-size:11px;color:#6B7280;text-transform:uppercase;letter-spacing:1px;">Profissional</span>
<p style="margin:4px 0 0;font-family:Georgia,serif;font-size:15px;color:#9CA3AF;">%s</p>
</td></tr>
</table>
</td></tr>
<tr><td align="center" style="padding:4px 32px 36px;">
<table role="presentation" cellspacing="0" cellpadding="0" border="0"><tr><td style="border-radius:10px;background-color:#FBBF24;">
<a class="cta-btn" href="%s" style="display:inline-block;padding:14px 32px;font-family:Arial,sans-serif;font-size:15px;font-weight:bold;color:#000000;text-decoration:none;border-radius:10px;">Agendar novo horário &rarr;</a>
</td></tr></table>
</td></tr>
<tr><td align="center" style="padding:24px 32px;border-top:1px solid #1F2937;">
<p style="margin:0;font-family:Georgia,serif;font-size:14px;color:#FBBF24;font-weight:bold;">Barbearia Souza</p>
<p style="margin:8px 0 0;font-family:Arial,sans-serif;font-size:11px;color:#374151;">&copy; 2026 Barbearia Souza. Todos os direitos reservados.</p>
</td></tr>
</table>
</td></tr></table>
</body>
</html>
""".formatted(nome, dataFmt, hora, motivo, servico, barbeiro, link);
    }

    private String buildHtmlNotificacaoBarbeiro(Agendamento ag, String motivo, String dataFmt, String clienteNome, String servicoNome, String hora) {
        String link = appUrl + "/barbeiro/agendamentos";

        return """
<!DOCTYPE html>
<html lang="pt-BR">
<head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Agendamento Cancelado</title>
<style>
  body,table,td,a{-webkit-text-size-adjust:100%%;-ms-text-size-adjust:100%%}
  body{margin:0;padding:0;background-color:#0B0F19;font-family:Georgia,'Times New Roman',serif}
  @media only screen and (max-width:620px){.email-container{width:100%%!important}.hero-padding{padding:32px 20px!important}}
</style>
</head>
<body style="margin:0;padding:0;background-color:#0B0F19;">
<table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%%" style="background-color:#0B0F19;"><tr><td align="center" style="padding:24px 12px;">
<table class="email-container" role="presentation" cellspacing="0" cellpadding="0" border="0" width="600" style="background-color:#111827;border-radius:16px;overflow:hidden;border:1px solid #1F2937;">
<tr><td style="padding:18px 32px;border-bottom:2px solid #ef4444;">
<span style="font-family:Georgia,'Times New Roman',serif;font-size:18px;font-weight:bold;color:#FFFFFF;">Barbearia <span style="color:#FBBF24;">Souza</span></span>
</td></tr>
<tr><td class="hero-padding" align="center" style="padding:40px 32px 32px;">
<span style="display:inline-block;background-color:#2d0707;border:1px solid #ef4444;border-radius:999px;padding:6px 16px;font-family:Arial,sans-serif;font-size:12px;font-weight:bold;color:#f87171;text-transform:uppercase;letter-spacing:1.5px;">&#10007; Cancelado</span>
<h1 style="margin:24px 0 12px;font-size:26px;font-weight:bold;color:#FFFFFF;">Agendamento cancelado</h1>
<p style="margin:0;font-family:Arial,sans-serif;font-size:15px;color:#9CA3AF;line-height:1.6;">Olá, <strong style="color:#FBBF24;">%s</strong>!</p>
<p style="margin:8px 0 0;font-family:Arial,sans-serif;font-size:15px;color:#9CA3AF;line-height:1.6;">O agendamento do cliente <strong>%s</strong> no dia <strong>%s</strong> às <strong>%s</strong> para o serviço <strong>%s</strong> foi cancelado.</p>
<p style="margin:12px 0 0;font-family:Arial,sans-serif;font-size:14px;color:#D97706;line-height:1.5;"><strong>Motivo:</strong> %s</p>
</td></tr>
<tr><td align="center" style="padding:4px 32px 36px;">
<table role="presentation" cellspacing="0" cellpadding="0" border="0"><tr><td style="border-radius:10px;background-color:#FBBF24;">
<a class="cta-btn" href="%s" style="display:inline-block;padding:14px 32px;font-family:Arial,sans-serif;font-size:15px;font-weight:bold;color:#000000;text-decoration:none;border-radius:10px;">Ver agendamentos &rarr;</a>
</td></tr></table>
</td></tr>
<tr><td align="center" style="padding:24px 32px;border-top:1px solid #1F2937;">
<p style="margin:0;font-family:Georgia,serif;font-size:14px;color:#FBBF24;font-weight:bold;">Barbearia Souza</p>
<p style="margin:8px 0 0;font-family:Arial,sans-serif;font-size:11px;color:#374151;">&copy; 2026 Barbearia Souza. Todos os direitos reservados.</p>
</td></tr>
</table>
</td></tr></table>
</body>
</html>
""".formatted(ag.getBarbeiro().getNome(), clienteNome, dataFmt, hora, servicoNome, motivo, link);
    }
}