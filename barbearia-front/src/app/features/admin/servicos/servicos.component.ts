import { DecimalPipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../../core/api.service';
import { Servico } from '../../../core/models';

@Component({
  selector: 'app-admin-servicos',
  standalone: true,
  imports: [FormsModule, DecimalPipe],
  templateUrl: './servicos.component.html',
  styleUrl: './servicos.component.scss',
})
export class AdminServicosComponent implements OnInit {
  private readonly api = inject(ApiService);

  readonly lista = signal<Servico[]>([]);
  readonly carregando = signal(true);
  readonly erro = signal<string | null>(null);
  readonly acaoId = signal<number | null>(null);

  nome = '';
  descricao = '';
  preco: number | null = null;
  duracaoMinutos: number | null = null;
  ativo = true;
  readonly criando = signal(false);

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.carregando.set(true);
    this.api.servicosAdmin().subscribe({
      next: (res) => {
        this.lista.set(res.servicos ?? []);
        this.carregando.set(false);
      },
      error: () => {
        this.carregando.set(false);
        this.erro.set('Não foi possível carregar serviços.');
      },
    });
  }

  cadastrar(): void {
    const p = this.preco;
    const d = this.duracaoMinutos;
    if (p == null || d == null) {
      this.erro.set('Informe preço e duração.');
      return;
    }
    this.erro.set(null);
    this.criando.set(true);
    this.api
      .cadastrarServicoAdmin({
        nome: this.nome.trim(),
        descricao: this.descricao.trim(),
        preco: p,
        duracaoMinutos: d,
        ativo: this.ativo,
      })
      .subscribe({
        next: () => {
          this.criando.set(false);
          this.nome = '';
          this.descricao = '';
          this.preco = null;
          this.duracaoMinutos = null;
          this.ativo = true;
          this.carregar();
        },
        error: () => {
          this.criando.set(false);
          this.erro.set('Não foi possível cadastrar o serviço.');
        },
      });
  }

  excluir(s: Servico): void {
    if (!confirm(`Excluir o serviço "${s.nome}"?`)) {
      return;
    }
    this.acaoId.set(s.id);
    this.api.excluirServico(s.id).subscribe({
      next: () => {
        this.acaoId.set(null);
        this.carregar();
      },
      error: () => {
        this.acaoId.set(null);
        this.erro.set('Não foi possível excluir.');
      },
    });
  }
}
