import { DecimalPipe } from '@angular/common';
import { Component, OnInit, OnDestroy, inject, signal } from '@angular/core';
import { ApiService } from '../../../core/api.service';
import { Agendamento, DashboardStats } from '../../../core/models';
import { ConfirmService } from '../../../shared/confirm-modal/confirm.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [DecimalPipe],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
})
export class AdminDashboardComponent implements OnInit, OnDestroy {
  private readonly api = inject(ApiService);
  private readonly confirm = inject(ConfirmService);

  readonly stats = signal<DashboardStats | null>(null);
  readonly carregando = signal(true);
  readonly erro = signal<string | null>(null);
  readonly acaoId = signal<number | null>(null);
  private refreshInterval: ReturnType<typeof setInterval> | null = null;

  readonly filtroPeriodo = signal<string>('TUDO');

  ngOnInit(): void {
    this.carregar();
    this.refreshInterval = setInterval(() => this.carregar(), 30000);
  }

  ngOnDestroy(): void {
    if (this.refreshInterval) {
      clearInterval(this.refreshInterval);
    }
  }

  filtrarPorPeriodo(p: string): void {
    this.filtroPeriodo.set(p);
    this.carregar();
  }

  carregar(): void {
    const p = this.filtroPeriodo();
    this.api.dashboardAdmin(p).subscribe({
      next: (s) => {
        this.stats.set(s);
        this.carregando.set(false);
        this.erro.set(null);
      },
      error: () => {
        this.carregando.set(false);
        this.erro.set('Não foi possível carregar o dashboard.');
      },
    });
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

  concluir(a: Agendamento): void {
    this.acaoId.set(a.id);
    this.api.concluirAgAdmin(a.id).subscribe({
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

  podeAcao(status: string | undefined): boolean {
    return (status ?? '').toUpperCase() === 'AGENDADO';
  }
}
