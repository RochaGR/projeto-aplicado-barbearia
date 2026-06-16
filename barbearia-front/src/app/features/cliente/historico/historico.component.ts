import { DatePipe, CurrencyPipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { ApiService } from '../../../core/api.service';
import { Agendamento } from '../../../core/models';

function agendamentosDe(res: Record<string, unknown>): Agendamento[] {
  for (const k of ['agendamentos', 'historico', 'itens', 'Agendamentos']) {
    const v = res[k];
    if (Array.isArray(v)) {
      return v as Agendamento[];
    }
  }
  return [];
}

@Component({
  selector: 'app-historico',
  standalone: true,
  imports: [FormsModule, DatePipe, CurrencyPipe],
  templateUrl: './historico.component.html',
  styleUrl: './historico.component.scss',
})
export class HistoricoComponent implements OnInit {
  private readonly api = inject(ApiService);

  dataInicio = '';
  dataFim = '';
  status = '';

  readonly lista = signal<Agendamento[]>([]);
  readonly carregando = signal(false);
  readonly erro = signal<string | null>(null);
  readonly fidelidadeData = signal<Record<string, unknown> | null>(null);

  readonly statusOpts = ['', 'AGENDADO', 'CONCLUIDO', 'CANCELADO'];

  ngOnInit(): void {
    this.buscar();
  }

  buscar(): void {
    this.carregando.set(true);
    this.erro.set(null);
    
    // Buscar histórico e fidelidade em paralelo
    forkJoin({
      historico: this.api.historicoCliente({
        dataInicio: this.dataInicio || undefined,
        dataFim: this.dataFim || undefined,
        status: this.status || undefined,
      }),
      fidelidade: this.api.fidelidadeCliente()
    }).subscribe({
      next: (result: { historico: Record<string, unknown>; fidelidade: Record<string, unknown> }) => {
        this.lista.set(agendamentosDe(result.historico));
        this.fidelidadeData.set(result.fidelidade);
        this.carregando.set(false);
      },
      error: () => {
        this.carregando.set(false);
        this.erro.set('Não foi possível carregar o histórico.');
      },
    });
  }

  // Helper methods for fidelidade data
  private getCartao(dados: Record<string, unknown>): Record<string, unknown> | null {
    for (const k of ['cartao', 'fidelidade', 'data']) {
      const v = dados[k];
      if (v && typeof v === 'object') {
        return v as Record<string, unknown>;
      }
    }
    return null;
  }

  private num(dados: Record<string, unknown> | null, key: string): number {
    if (!dados) return 0;
    const v = dados[key];
    return typeof v === 'number' ? v : 0;
  }

  getCortesRealizados(dados: Record<string, unknown> | null): number {
    if (!dados) return 0;
    const cartao = this.getCartao(dados);
    return this.num(cartao, 'cortesRealizados');
  }

  getCortesParaDesconto(dados: Record<string, unknown> | null): number {
    if (!dados) return 0;
    const cartao = this.getCartao(dados);
    return this.num(cartao, 'cortesParaDesconto');
  }

  getPercentualDesconto(dados: Record<string, unknown> | null): number {
    if (!dados) return 0;
    const cartao = this.getCartao(dados);
    return this.num(cartao, 'percentualDesconto');
  }

  getEconomiaTotal(dados: Record<string, unknown> | null): number {
    if (!dados) return 0;
    const cartao = this.getCartao(dados);
    return this.num(cartao, 'economiaTotal');
  }
}
