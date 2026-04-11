import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../../core/api.service';
import { Barbeiro } from '../../../core/models';

@Component({
  selector: 'app-admin-barbeiros',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './barbeiros.component.html',
  styleUrl: './barbeiros.component.scss',
})
export class AdminBarbeirosComponent implements OnInit {
  private readonly api = inject(ApiService);

  readonly lista = signal<Barbeiro[]>([]);
  readonly carregando = signal(true);
  readonly erro = signal<string | null>(null);
  readonly acaoId = signal<number | null>(null);

  nome = '';
  email = '';
  telefone = '';
  senha = '';
  readonly criando = signal(false);

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.carregando.set(true);
    this.api.barbeirosAdmin().subscribe({
      next: (res) => {
        this.lista.set(res.barbeiros ?? []);
        this.carregando.set(false);
      },
      error: () => {
        this.carregando.set(false);
        this.erro.set('Não foi possível carregar barbeiros.');
      },
    });
  }

  cadastrar(): void {
    this.erro.set(null);
    this.criando.set(true);
    this.api
      .cadastrarBarbeiroAdmin({
        id: 0,
        nome: this.nome.trim(),
        email: this.email.trim(),
        telefone: this.telefone.trim(),
        ativo: true,
        senha: this.senha,
      })
      .subscribe({
        next: () => {
          this.criando.set(false);
          this.nome = '';
          this.email = '';
          this.telefone = '';
          this.senha = '';
          this.carregar();
        },
        error: () => {
          this.criando.set(false);
          this.erro.set('Não foi possível cadastrar o barbeiro.');
        },
      });
  }

  toggle(b: Barbeiro): void {
    this.acaoId.set(b.id);
    this.api.toggleBarbeiro(b.id).subscribe({
      next: () => {
        this.acaoId.set(null);
        this.carregar();
      },
      error: () => {
        this.acaoId.set(null);
        this.erro.set('Não foi possível alterar o status.');
      },
    });
  }

  excluir(b: Barbeiro): void {
    if (!confirm(`Excluir permanentemente ${b.nome}?`)) {
      return;
    }
    this.acaoId.set(b.id);
    this.api.excluirBarbeiro(b.id).subscribe({
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
