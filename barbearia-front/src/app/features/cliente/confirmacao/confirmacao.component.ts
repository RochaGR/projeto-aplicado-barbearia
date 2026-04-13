import { CommonModule, DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ApiService } from '../../../core/api.service';
import { Agendamento } from '../../../core/models';

@Component({
  selector: 'app-confirmacao',
  standalone: true,
  imports: [CommonModule, RouterLink, DatePipe],
  templateUrl: './confirmacao.component.html',
  styleUrl: './confirmacao.component.scss',
})
export class ConfirmacaoComponent implements OnInit {
  private readonly api = inject(ApiService);
  private readonly route = inject(ActivatedRoute);

  readonly agendamento = signal<Agendamento | null>(null);
  readonly precoOriginal = signal<number | null>(null);
  readonly precoFinal = signal<number | null>(null);
  readonly valorDescontado = signal<number>(0);
  readonly percentualDesconto = signal<number | null>(null);
  readonly descontoAplicado = signal(false);
  readonly carregando = signal(true);
  readonly erro = signal<string | null>(null);

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!Number.isFinite(id)) {
      this.carregando.set(false);
      this.erro.set('Agendamento inválido.');
      return;
    }
    this.api.confirmacaoAgendamento(id).subscribe({
      next: (raw) => {
        const response = raw as {
          agendamento: Agendamento;
          precoOriginal?: number;
          precoFinal?: number;
          valorDescontado?: number;
          percentualDesconto?: number | null;
          descontoAplicado?: boolean;
        };
        this.agendamento.set(response.agendamento);
        this.precoOriginal.set(typeof response.precoOriginal === 'number' ? response.precoOriginal : null);
        this.precoFinal.set(typeof response.precoFinal === 'number' ? response.precoFinal : null);
        this.valorDescontado.set(typeof response.valorDescontado === 'number' ? response.valorDescontado : 0);
        this.percentualDesconto.set(typeof response.percentualDesconto === 'number' ? response.percentualDesconto : null);
        this.descontoAplicado.set(Boolean(response.descontoAplicado));
        this.carregando.set(false);
      },
      error: () => {
        this.carregando.set(false);
        this.erro.set('Não foi possível carregar a confirmação.');
      },
    });
  }

  formatMoney(value: number | null): string {
    if (value == null || Number.isNaN(value)) {
      return '0.00';
    }
    return value.toFixed(2);
  }
}
