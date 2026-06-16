import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../../core/api.service';
import { Agendamento } from '../../../core/models';
import { ConfirmService } from '../../../shared/confirm-modal/confirm.service';

function agendamentosDe(res: Record<string, unknown>): Agendamento[] {
  const keys = ['agendamentos', 'Agendamentos', 'itens'];
  for (const k of keys) {
    const v = res[k];
    if (Array.isArray(v)) {
      return v as Agendamento[];
    }
  }
  return [];
}

@Component({
  selector: 'app-lista-agendamentos',
  standalone: true,
  imports: [FormsModule, RouterLink, DatePipe],
  templateUrl: './lista-agendamentos.component.html',
  styleUrl: './lista-agendamentos.component.scss',
})
export class ListaAgendamentosComponent implements OnInit {
  private readonly api = inject(ApiService);
  private readonly confirm = inject(ConfirmService);

  filtroData = '';
  filtroStatus = '';
  private todosAgendamentos: Agendamento[] = [];
  readonly lista = signal<Agendamento[]>([]);
  readonly carregando = signal(true);
  readonly erro = signal<string | null>(null);
  readonly acaoId = signal<number | null>(null);

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.carregando.set(true);
    this.erro.set(null);
    const d = this.filtroData.trim() || undefined;
    this.api.listarAgendamentosCliente(d).subscribe({
      next: (res) => {
        this.todosAgendamentos = agendamentosDe(res);
        this.filtrar();
        this.carregando.set(false);
      },
      error: () => {
        this.carregando.set(false);
        this.erro.set('Não foi possível listar os agendamentos.');
      },
    });
  }

  filtrar(): void {
    const s = this.filtroStatus;
    if (!s) {
      this.lista.set(this.todosAgendamentos);
    } else {
      this.lista.set(this.todosAgendamentos.filter(a => (a.status ?? '').toUpperCase() === s));
    }
  }

  async cancelar(a: Agendamento): Promise<void> {
    const detalhes = [
      a.servico?.nome ? `✂️ ${a.servico.nome}` : '',
      a.barbeiro?.nome ? `👤 ${a.barbeiro.nome}` : '',
      a.dataHora ? `📅 ${a.dataHora}` : '',
    ].filter(Boolean).join('\n');
    if (!(await this.confirm.confirm(`Tem certeza que deseja cancelar?\n\n${detalhes}`))) {
      return;
    }
    this.acaoId.set(a.id);
    this.api.cancelarAgendamentoCliente(a.id).subscribe({
      next: () => {
        this.acaoId.set(null);
        this.carregar();
      },
      error: () => {
        this.acaoId.set(null);
        this.erro.set('Não foi possível cancelar.');
      },
    });
  }

  podeCancelar(status: string | undefined): boolean {
    const s = (status ?? '').toUpperCase();
    return s === 'AGENDADO';
  }
}
