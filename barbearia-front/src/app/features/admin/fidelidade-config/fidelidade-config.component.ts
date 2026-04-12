import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../../core/api.service';

@Component({
  selector: 'app-admin-fidelidade-config',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './fidelidade-config.component.html',
  styleUrl: './fidelidade-config.component.scss',
})
export class AdminFidelidadeConfigComponent implements OnInit {
  private readonly api = inject(ApiService);

  percentualDesconto: number | null = null;
  cortesParaDesconto: number | null = null;

  readonly carregando = signal(true);
  readonly salvando = signal(false);
  readonly erro = signal<string | null>(null);
  readonly ok = signal<string | null>(null);

  ngOnInit(): void {
    this.api.fidelidadeConfigAdmin().subscribe({
      next: (r) => {
        const p = r['percentualDesconto'];
        const c = r['cortesParaDesconto'];
        this.percentualDesconto = typeof p === 'number' ? p : null;
        this.cortesParaDesconto = typeof c === 'number' ? c : null;
        this.carregando.set(false);
      },
      error: () => {
        this.carregando.set(false);
        this.erro.set('Não foi possível carregar a configuração.');
      },
    });
  }

  salvar(): void {
    const p = this.percentualDesconto;
    const c = this.cortesParaDesconto;
    if (p == null || c == null) {
      this.erro.set('Preencha os dois campos numéricos.');
      return;
    }
    this.erro.set(null);
    this.ok.set(null);
    this.salvando.set(true);
    this.api.salvarFidelidadeAdmin({ percentualDesconto: p, cortesParaDesconto: c }).subscribe({
      next: () => {
        this.salvando.set(false);
        this.ok.set('Configuração salva.');
      },
      error: () => {
        this.salvando.set(false);
        this.erro.set('Não foi possível salvar.');
      },
    });
  }
}
