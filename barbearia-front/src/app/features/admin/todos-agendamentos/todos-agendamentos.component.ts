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
  selector: 'app-todos-agendamentos',
  standalone: true,
  imports: [FormsModule, DatePipe],
  templateUrl: './todos-agendamentos.component.html',
  styleUrl: './todos-agendamentos.component.scss',
})
export class TodosAgendamentosComponent implements OnInit {
  private readonly api = inject(ApiService);

  filtroData = '';
  filtroStatus = '';
  readonly statusOpts = ['', 'AGENDADO', 'CONFIRMADO', 'CONCLUIDO', 'CANCELADO'];

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
    const st = this.filtroStatus.trim() || undefined;
    this.api.todosAgendamentosAdmin(d, st).subscribe({
      next: (res) => {
        this.lista.set(agendamentosDe(res));
        this.carregando.set(false);
      },
      error: () => {
        this.carregando.set(false);
        this.erro.set('Não foi possível listar agendamentos.');
      },
    });
  }

  confirmar(a: Agendamento): void {
    this.acaoId.set(a.id);
    this.api.confirmarAgAdmin(a.id).subscribe({
      next: () => {
        this.acaoId.set(null);
        this.carregar();
      },
      error: () => {
        this.acaoId.set(null);
        this.erro.set('Não foi possível confirmar.');
      },
    });
  }

  cancelar(a: Agendamento): void {
    if (!confirm('Cancelar este agendamento?')) {
      return;
    }
    this.acaoId.set(a.id);
    this.api.cancelarAgAdmin(a.id).subscribe({
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

  podeConfirmar(status: string | undefined): boolean {
    return (status ?? '').toUpperCase() === 'AGENDADO';
  }
}
