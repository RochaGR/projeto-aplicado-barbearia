import {
  Component,
  ElementRef,
  ViewChild,
  AfterViewChecked,
  signal,
  inject,
  effect,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { ChatService, ChatMessage, Opcao } from './chat.service';

type Etapa = 'servico' | 'barbeiro' | 'horario' | 'resumo' | 'confirmacao';

@Component({
  selector: 'app-chat-widget',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './chat-widget.component.html',
  styleUrl: './chat-widget.component.scss',
})
export class ChatWidgetComponent implements AfterViewChecked {
  private chatService = inject(ChatService);
  private sanitizer = inject(DomSanitizer);

  @ViewChild('mensagensContainer') mensagensContainer!: ElementRef;

  readonly aberto = signal(false);
  readonly carregando = signal(false);
  readonly mensagens = signal<ChatMessage[]>([]);
  readonly servicos = signal<Opcao[]>([]);
  readonly barbeiros = signal<Opcao[]>([]);
  readonly horariosDisponiveis = signal<Opcao[]>([]);
  horarioSelecionado?: Opcao;
  dataAgendamento?: string;

  servicoSelecionado?: Opcao;
  barbeiroSelecionado?: Opcao;
  etapa: Etapa = 'servico';

  textoInput = '';
  agendando = signal(false);
  agendamentoOk = signal<{ servico: string; barbeiro: string; dataHora: string; preco: string; desconto?: string } | null>(null);

  constructor() {
    this.mensagens.set([
      {
        role: 'assistant',
        content: 'Olá! 👋 Sou o assistente da Barbearia Souza. Posso ajudar você a agendar um horário. Escolha um serviço abaixo:',
      },
    ]);
    this.chatService.iniciar().subscribe({
      next: res => {
        this.servicos.set(res.servicos);
        this.barbeiros.set(res.barbeiros);
      },
    });
    effect(() => {
      this.mensagens();
      this.servicos();
      this.barbeiros();
      this.horariosDisponiveis();
      this.shouldScroll = true;
    });
  }

  get podeConfirmar(): boolean {
    return this.etapa === 'confirmacao' && !this.agendando();
  }

  get dataExtraida(): string | null {
    const dt = this.extrairDataHora();
    return dt ? `${dt.data}T${dt.horario}:00` : null;
  }

  get dataExtraidaFormatada(): string {
    const dt = this.extrairDataHora();
    if (!dt) return '';
    const [ano, mes, dia] = dt.data.split('-');
    return `${dia}/${mes}/${ano} às ${dt.horario}`;
  }

  toggleChat(): void {
    this.aberto.update(v => !v);
  }

  fechar(): void {
    this.aberto.set(false);
  }

  enviar(): void {
    const texto = this.textoInput.trim();
    if (!texto || this.carregando()) return;
    this.enviarMensagemTexto(texto);
  }

  selecionarServico(opcao: Opcao): void {
    this.servicoSelecionado = opcao;
    this.etapa = 'barbeiro';
    this.servicos.set([]);
    this.enviarMensagemTexto(`Quero o serviço: ${opcao.nome}`);
  }

  selecionarBarbeiro(opcao: Opcao): void {
    this.barbeiroSelecionado = opcao;
    this.etapa = 'horario';
    this.barbeiros.set([]);
    this.enviarMensagemTexto(`Prefiro o barbeiro: ${opcao.nome}`);
  }

  private extrairDiaDoTexto(texto: string): string | null {
    const t = texto.toLowerCase().trim();
    const agora = new Date();
    const hoje = new Date(agora.getFullYear(), agora.getMonth(), agora.getDate());
    const fmt = (d: Date) => `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`;

    if (t === 'hoje') return fmt(agora);
    if (t === 'amanhã' || t === 'amanha') { const d = new Date(); d.setDate(d.getDate()+1); return fmt(d); }

    const diasSemana: Record<string, number> = {
      domingo: 0, segunda: 1, terça: 2, terca: 2, quarta: 3, quinta: 4, sexta: 5, sábado: 6, sabado: 6,
    };
    for (const [nome, idx] of Object.entries(diasSemana)) {
      if (t.includes(nome)) {
        const d = new Date();
        let diff = (idx - d.getDay() + 7) % 7;
        if (diff === 0) diff = 7;
        d.setDate(d.getDate() + diff);
        return fmt(d);
      }
    }

    const dataMatch = texto.match(/(\d{1,2})\/(\d{1,2})(?:\/(\d{4}))?/);
    if (dataMatch) {
      let ano = dataMatch[3] ? +dataMatch[3] : agora.getFullYear();
      let d = new Date(ano, +dataMatch[2]-1, +dataMatch[1]);
      if (d < hoje) d.setFullYear(ano+1);
      return fmt(d);
    }

    const num = parseInt(t, 10);
    if (!isNaN(num) && num >= 1 && num <= 31 && t === String(num)) {
      let d = new Date(agora.getFullYear(), agora.getMonth(), num);
      if (d < hoje) d = new Date(agora.getFullYear(), agora.getMonth()+1, num);
      return fmt(d);
    }

    return null;
  }

  private temDiaNoTexto(texto: string): boolean {
    return this.extrairDiaDoTexto(texto) !== null;
  }

  private verificarDiaNoTexto(texto: string): void {
    if (this.etapa !== 'horario' || !this.barbeiroSelecionado || this.horarioSelecionado || this.horariosDisponiveis().length > 0) return;
    const dia = this.extrairDiaDoTexto(texto);
    if (dia) {
      this.dataAgendamento = dia;
      this.chatService.getHorariosDisponiveis(this.barbeiroSelecionado.id, dia).subscribe({
        next: horarios => {
          if (horarios.length === 0) {
            this.mensagens.update(msgs => [...msgs, { role: 'assistant', content: '❌ Essa data já passou ou não há horários disponíveis. Escolha outra data.' }]);
          }
          this.horariosDisponiveis.set(horarios.map((h, i) => ({ id: i, nome: h })));
        },
      });
    }
  }

  selecionarHorario(h: Opcao): void {
    if (!this.dataAgendamento) return;
    this.horarioSelecionado = h;
  }

  confirmarHorario(): void {
    if (!this.horarioSelecionado || !this.dataAgendamento) return;
    this.etapa = 'resumo';
    this.horariosDisponiveis.set([]);
  }

  confirmarAgendamento(): void {
    if (!this.podeConfirmar) return;
    const dt = this.extrairDataHora();
    if (!dt) return;

    this.agendando.set(true);

    this.chatService.agendar({
      servicoId: this.servicoSelecionado!.id,
      barbeiroId: this.barbeiroSelecionado!.id,
      data: dt.data,
      horario: dt.horario,
    }).subscribe({
      next: res => {
        this.agendamentoOk.set(res);
        this.agendando.set(false);
        this.mensagens.update(msgs => [
          ...msgs,
          {
            role: 'assistant',
            content: `✅ **Agendamento Confirmado!**\n\n✂️ **${res.servico}**\n👤 **${res.barbeiro}**\n📅 **${res.dataHora}**\n💰 **${res.preco}**${res.desconto ? `\n🏷️ Desconto: ${res.desconto}` : ''}\n\nObrigado pela preferência! 🙏`,
          },
        ]);
      },
      error: err => {
        this.agendando.set(false);
        const status = err.status;
        const erro = err.error?.error || '';
        let mensagem: string;
        if (status === 401 || erro.toLowerCase().includes('autentic') || erro.toLowerCase().includes('auth')) {
          mensagem = 'Sua sessão expirou. 🔄 Recarregue a página e faça login novamente.';
        } else {
          mensagem = `❌ ${erro || ('Erro ' + status)}.`;
        }
        this.mensagens.update(msgs => [
          ...msgs,
          { role: 'assistant', content: mensagem },
        ]);
      },
    });
  }

  confirmarResumo(): void {
    this.etapa = 'confirmacao';
    this.mensagens.update(msgs => [
      ...msgs,
      { role: 'user', content: 'Sim, confirmar!' },
    ]);
  }

  cancelarResumo(): void {
    this.servicoSelecionado = undefined;
    this.barbeiroSelecionado = undefined;
    this.etapa = 'servico';
    this.mensagens.update(msgs => [
      ...msgs,
      { role: 'user', content: 'Não, quero mudar' },
      { role: 'assistant', content: 'OK, vamos recomeçar! Escolha um serviço abaixo:' },
    ]);
  }

  reiniciar(): void {
    this.agendamentoOk.set(null);
    this.servicoSelecionado = undefined;
    this.barbeiroSelecionado = undefined;
    this.horarioSelecionado = undefined;
    this.dataAgendamento = undefined;
    this.etapa = 'servico';
    this.mensagens.set([{
      role: 'assistant',
      content: 'Olá! 👋 Sou o assistente da Barbearia Souza. Posso ajudar você a agendar um horário. Escolha um serviço abaixo:',
    }]);
    this.chatService.iniciar().subscribe(res => {
      this.servicos.set(res.servicos);
      this.barbeiros.set(res.barbeiros);
    });
  }

  private extrairDataHoraDeTexto(text: string): { data: string; horario: string } | null {
    const agora = new Date();
    let horaMatch = text.match(/(\d{1,2})[:h](\d{2})/);
    if (!horaMatch) {
      const h2 = text.match(/(?:as\s*|às\s*)(\d{1,2})(?:\s*horas?)?(?:\s*$|[,\s.!?]|$)/i);
      if (h2) horaMatch = [h2[0], h2[1], '00'];
    }
    if (!horaMatch) return null;

    const diasSemana: Record<string, number> = {
      domingo: 0, segunda: 1, terça: 2, terca: 2, quarta: 3, quinta: 4, sexta: 5, sábado: 6, sabado: 6,
    };
    let dataAlvo: Date | null = null;
    if (/\bhoje\b/i.test(text)) {
      dataAlvo = new Date();
    } else if (/\bamanh[ãa]\b/i.test(text)) {
      dataAlvo = new Date();
      dataAlvo.setDate(dataAlvo.getDate() + 1);
    } else {
      for (const [nome, idx] of Object.entries(diasSemana)) {
        if (text.includes(nome)) {
          dataAlvo = new Date();
          let diff = (idx - dataAlvo.getDay() + 7) % 7;
          if (diff === 0) diff = 7;
          dataAlvo.setDate(dataAlvo.getDate() + diff);
          break;
        }
      }
    }
    if (!dataAlvo) {
      const dataMatch = text.match(/(\d{1,2})\/(\d{1,2})(?:\/(\d{4}))?/);
      if (dataMatch) {
        let ano = dataMatch[3] ? +dataMatch[3] : agora.getFullYear();
        let mes = +dataMatch[2] - 1;
        let dia = +dataMatch[1];
        dataAlvo = new Date(ano, mes, dia);
        if (dataAlvo < new Date(agora.getFullYear(), agora.getMonth(), agora.getDate())) {
          dataAlvo.setFullYear(ano + 1);
        }
      }
    }
    if (!dataAlvo) return null;

    return {
      data: `${dataAlvo.getFullYear()}-${String(dataAlvo.getMonth() + 1).padStart(2, '0')}-${String(dataAlvo.getDate()).padStart(2, '0')}`,
      horario: `${horaMatch[1].padStart(2, '0')}:${horaMatch[2].padStart(2, '0')}`,
    };
  }

  private enviarMensagemTexto(texto: string): void {
    if (!this.servicoSelecionado) {
      const servico = this.detectarNome(this.servicos(), texto);
      if (servico) {
        this.servicoSelecionado = servico;
        this.servicos.set([]);
      }
    }
    if (!this.barbeiroSelecionado && this.servicoSelecionado) {
      const barbeiro = this.detectarNome(this.barbeiros(), texto);
      if (barbeiro) {
        this.barbeiroSelecionado = barbeiro;
        this.barbeiros.set([]);
      }
    }

    this.mensagens.update(msgs => [...msgs, { role: 'user', content: texto }]);
    this.textoInput = '';
    this.carregando.set(true);

    if (this.etapa === 'horario' && this.barbeiroSelecionado && !this.horarioSelecionado && this.temDiaNoTexto(texto)) {
      this.verificarDiaNoTexto(texto);
      this.carregando.set(false);
      return;
    }

    this.chatService.enviarMensagem(texto, this.mensagens()).subscribe({
      next: res => {
        this.mensagens.update(msgs => [...msgs, { role: 'assistant', content: res.resposta }]);

        const resposta = res.resposta.toLowerCase();

        if (!this.servicoSelecionado && res.servicoSelecionadoId) {
          const servico = res.servicos?.find(s => s.id === res.servicoSelecionadoId);
          if (servico) {
            this.servicoSelecionado = servico;
            this.servicos.set([]);
          }
        }
        if (!this.barbeiroSelecionado && res.barbeiroSelecionadoId) {
          const barbeiro = res.barbeiros?.find(b => b.id === res.barbeiroSelecionadoId);
          if (barbeiro) {
            this.barbeiroSelecionado = barbeiro;
            this.barbeiros.set([]);
          }
        }

        if (this.etapa === 'servico' && (resposta.includes('barbeir') || resposta.includes('👤'))) {
          this.etapa = 'barbeiro';
          this.servicos.set([]);
        }
        if ((this.etapa === 'servico' || this.etapa === 'barbeiro') && (resposta.includes('data') || resposta.includes('horário') || resposta.includes('🕐'))) {
          this.etapa = 'horario';
          this.barbeiros.set([]);
        }
        if (this.etapa === 'servico') {
          this.servicos.set(res.servicos || []);
        }
        if (this.etapa === 'barbeiro') {
          this.barbeiros.set(res.barbeiros || []);
        }
        this.carregando.set(false);
      },
      error: err => {
        const erro = err.error?.error || err.message || 'Erro desconhecido';
        this.mensagens.update(msgs => [
          ...msgs,
          { role: 'assistant', content: `❌ ${erro}` },
        ]);
        this.carregando.set(false);
      },
    });
  }

  private detectarNome(itens: Opcao[], texto: string): Opcao | null {
    if (itens.length === 0) return null;
    const t = texto.toLowerCase();
    for (const item of itens) {
      if (t.includes(item.nome.toLowerCase())) return item;
    }
    for (const item of itens) {
      const palavras = item.nome.toLowerCase().split(/\s+/);
      const encontradas = palavras.filter(p => t.includes(p)).length;
      if (palavras.length > 0 && encontradas === palavras.length) return item;
    }
    return null;
  }

  private extrairDataHora(): { data: string; horario: string } | null {
    if (this.dataAgendamento && this.horarioSelecionado) {
      return { data: this.dataAgendamento, horario: this.horarioSelecionado.nome };
    }
    return null;
  }

  onKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.enviar();
    }
  }

  private shouldScroll = false;

  ngAfterViewChecked(): void {
    if (this.shouldScroll && this.mensagensContainer) {
      const el = this.mensagensContainer.nativeElement as HTMLElement;
      el.scrollTop = el.scrollHeight;
      this.shouldScroll = false;
    }
  }

  renderizarMensagem(texto: string): SafeHtml {
    const html = texto
      .replace(/✅|❌|🙏/g, '')
      .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
      .replace(/\n/g, '<br>');
    return this.sanitizer.bypassSecurityTrustHtml(html);
  }
}
