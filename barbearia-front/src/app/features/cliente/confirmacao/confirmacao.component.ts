import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ApiService } from '../../../core/api.service';
import { Agendamento } from '../../../core/models';

@Component({
  selector: 'app-confirmacao',
  standalone: true,
  imports: [RouterLink, DatePipe],
  templateUrl: './confirmacao.component.html',
  styleUrl: './confirmacao.component.scss',
})
export class ConfirmacaoComponent implements OnInit {
  private readonly api = inject(ApiService);
  private readonly route = inject(ActivatedRoute);

  readonly agendamento = signal<Agendamento | null>(null);
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
        // Extract appointment data from nested response
        const response = raw as { agendamento: Agendamento };
        this.agendamento.set(response.agendamento);
        this.carregando.set(false);
      },
      error: () => {
        this.carregando.set(false);
        this.erro.set('Não foi possível carregar a confirmação.');
      },
    });
  }
}
