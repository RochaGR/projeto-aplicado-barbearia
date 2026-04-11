import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../../core/api.service';
import { Agendamento } from '../../../core/models';

function agendamentosDe(res: Record<string, unknown>): Agendamento[] {
  for (const k of ['agendamentos', 'Agendamentos']) {
    const v = res[k];
    if (Array.isArray(v)) {
      return v as Agendamento[];
    }
  }
  return [];
}

@Component({
  selector: 'app-agendamentos-barbeiro',
  standalone: true,
  imports: [FormsModule, DatePipe],
  templateUrl: './agendamentos-barbeiro.component.html',
  styleUrl: './agendamentos-barbeiro.component.scss',
})
export class AgendamentosBarbeiroComponent implements OnInit {
  private readonly api = inject(ApiService);

  filtroData = '';
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
    this.api.agendamentosBarbeiro(d).subscribe({
      next: (res) => {
        this.lista.set(agendamentosDe(res));
        this.carregando.set(false);
      },
      error: () => {
        this.carregando.set(false);
        this.erro.set('Não foi possível carregar os agendamentos.');
      },
    });
  }

  concluir(a: Agendamento): void {
    this.acaoId.set(a.id);
    this.api.concluirAgendamentoBarbeiro(a.id).subscribe({
      next: () => {
        this.acaoId.set(null);
        this.carregar();
      },
      error: () => {
        this.acaoId.set(null);
        this.erro.set('Não foi possível concluir.');
      },
    });
  }

  cancelar(a: Agendamento): void {
    if (!confirm('Cancelar este agendamento?')) {
      return;
    }
    this.acaoId.set(a.id);
    this.api.cancelarAgendamentoBarbeiro(a.id).subscribe({
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

  podeConcluir(status: string | undefined): boolean {
    const s = (status ?? '').toUpperCase();
    return s === 'CONFIRMADO' || s === 'AGENDADO';
  }
}
