import { DecimalPipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { ApiService } from '../../../core/api.service';

@Component({
  selector: 'app-fidelidade',
  standalone: true,
  imports: [DecimalPipe],
  templateUrl: './fidelidade.component.html',
  styleUrl: './fidelidade.component.scss',
})
export class FidelidadeComponent implements OnInit {
  private readonly api = inject(ApiService);

  readonly dados = signal<Record<string, unknown> | null>(null);
  readonly carregando = signal(true);
  readonly erro = signal<string | null>(null);

  ngOnInit(): void {
    this.api.fidelidadeCliente().subscribe({
      next: (r) => {
        this.dados.set(r);
        this.carregando.set(false);
      },
      error: () => {
        this.carregando.set(false);
        this.erro.set('Não foi possível carregar o programa de fidelidade.');
      },
    });
  }

  num(obj: Record<string, unknown> | null, ...keys: string[]): number | null {
    if (!obj) {
      return null;
    }
    for (const k of keys) {
      const v = obj[k];
      if (typeof v === 'number' && !Number.isNaN(v)) {
        return v;
      }
    }
    return null;
  }

  getCartao(dados: Record<string, unknown>): Record<string, unknown> | null {
    return dados['cartao'] as Record<string, unknown> | null;
  }

  getCortesRealizados(dados: Record<string, unknown>): number {
    const cartao = this.getCartao(dados);
    return this.num(cartao, 'cortesRealizados') || 0;
  }

  getPontosAtuais(dados: Record<string, unknown>): number {
    const cartao = this.getCartao(dados);
    return this.num(cartao, 'pontosAtuais') || 0;
  }

  getCortesParaDesconto(dados: Record<string, unknown>): number {
    const cartao = this.getCartao(dados);
    return this.num(cartao, 'cortesParaDesconto') || 5;
  }

  getPercentualDesconto(dados: Record<string, unknown>): number {
    const cartao = this.getCartao(dados);
    return this.num(cartao, 'percentualDesconto') || 10;
  }

  getTemDesconto(dados: Record<string, unknown>): boolean {
    const cartao = this.getCartao(dados);
    return Boolean(cartao?.['temDesconto']);
  }

  getEconomiaTotal(dados: Record<string, unknown>): number {
    const cartao = this.getCartao(dados);
    return this.num(cartao, 'economiaTotal') || 0;
  }

  getStarArray(dados: Record<string, unknown>): number[] {
    const meta = this.getCortesParaDesconto(dados);
    return Array.from({ length: meta }, (_, i) => i + 1);
  }
}
