import { DecimalPipe, JsonPipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { ApiService } from '../../../core/api.service';

@Component({
  selector: 'app-fidelidade',
  standalone: true,
  imports: [DecimalPipe, JsonPipe],
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
}
