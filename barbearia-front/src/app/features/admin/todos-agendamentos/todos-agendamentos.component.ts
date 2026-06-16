import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { ApiService } from '../../../core/api.service';
import { Agendamento } from '../../../core/models';
import { ConfirmService } from '../../../shared/confirm-modal/confirm.service';

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
  private readonly route = inject(ActivatedRoute);
  private readonly confirm = inject(ConfirmService);

  filtroData = '';
  filtroStatus = '';
  readonly statusOpts = ['', 'AGENDADO', 'CONCLUIDO', 'CANCELADO'];

  readonly lista = signal<Agendamento[]>([]);
  readonly carregando = signal(true);
  readonly erro = signal<string | null>(null);
  readonly acaoId = signal<number | null>(null);

  readonly currentPage = signal(0);
  readonly totalPages = signal(0);
  readonly totalElements = signal(0);
  readonly pageSize = signal(20);

  ngOnInit(): void {
    const statusParam = this.route.snapshot.queryParamMap.get('status');
    if (statusParam && this.statusOpts.includes(statusParam)) {
      this.filtroStatus = statusParam;
    }
    this.carregar();
  }

  carregar(pagina?: number): void {
    this.carregando.set(true);
    this.erro.set(null);
    const d = this.filtroData.trim() || undefined;
    const st = this.filtroStatus.trim() || undefined;
    const p = pagina ?? 0;
    this.api.todosAgendamentosAdmin(d, st, p, this.pageSize()).subscribe({
      next: (res) => {
        this.lista.set(agendamentosDe(res));
        this.currentPage.set(res['currentPage'] as number);
        this.totalPages.set(res['totalPages'] as number);
        this.totalElements.set(res['totalElements'] as number);
        this.carregando.set(false);
      },
      error: () => {
        this.carregando.set(false);
        this.erro.set('Não foi possível listar agendamentos.');
      },
    });
  }

  irParaPagina(p: number): void {
    if (p < 0 || p >= this.totalPages()) return;
    this.carregar(p);
  }

  paginaAnterior(): void {
    this.irParaPagina(this.currentPage() - 1);
  }

  proximaPagina(): void {
    this.irParaPagina(this.currentPage() + 1);
  }

  paginas(): number[] {
    const total = this.totalPages();
    const atual = this.currentPage();
    const delta = 2;
    const range: number[] = [];
    for (let i = Math.max(0, atual - delta); i <= Math.min(total - 1, atual + delta); i++) {
      range.push(i);
    }
    return range;
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
        this.carregar(this.currentPage());
      },
      error: () => {
        this.acaoId.set(null);
        this.erro.set('Não foi possível cancelar.');
      },
    });
  }

  podeCancelar(status: string | undefined): boolean {
    return (status ?? '').toUpperCase() === 'AGENDADO';
  }
}
