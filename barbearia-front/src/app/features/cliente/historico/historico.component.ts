import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
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
  imports: [FormsModule, DatePipe],
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

  readonly statusOpts = ['', 'AGENDADO', 'CONFIRMADO', 'CONCLUIDO', 'CANCELADO'];

  ngOnInit(): void {
    this.buscar();
  }

  buscar(): void {
    this.carregando.set(true);
    this.erro.set(null);
    this.api
      .historicoCliente({
        dataInicio: this.dataInicio || undefined,
        dataFim: this.dataFim || undefined,
        status: this.status || undefined,
      })
      .subscribe({
        next: (res) => {
          this.lista.set(agendamentosDe(res));
          this.carregando.set(false);
        },
        error: () => {
          this.carregando.set(false);
          this.erro.set('Não foi possível carregar o histórico.');
        },
      });
  }
}
