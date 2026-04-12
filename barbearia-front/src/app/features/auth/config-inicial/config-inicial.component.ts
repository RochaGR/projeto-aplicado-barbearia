import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ApiService } from '../../../core/api.service';

@Component({
  selector: 'app-config-inicial',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './config-inicial.component.html',
  styleUrl: './config-inicial.component.scss',
})
export class ConfigInicialComponent implements OnInit {
  private readonly api = inject(ApiService);
  private readonly router = inject(Router);

  nome = '';
  email = '';
  telefone = '';
  senha = '';

  readonly carregando = signal(true);
  readonly required = signal<boolean | null>(null);
  readonly erro = signal<string | null>(null);
  readonly enviando = signal(false);

  ngOnInit(): void {
    this.api.setupRequired().subscribe({
      next: (r) => {
        this.required.set(r.required);
        this.carregando.set(false);
        if (!r.required) {
          void this.router.navigateByUrl('/');
        }
      },
      error: () => {
        this.carregando.set(false);
        this.erro.set('Não foi possível verificar a configuração.');
      },
    });
  }

  criarAdmin(): void {
    this.erro.set(null);
    this.enviando.set(true);
    this.api
      .primeiroAdmin({
        nome: this.nome.trim(),
        email: this.email.trim(),
        telefone: this.telefone.trim(),
        senha: this.senha,
      })
      .subscribe({
        next: () => {
          this.enviando.set(false);
          void this.router.navigateByUrl('/login');
        },
        error: () => {
          this.enviando.set(false);
          this.erro.set('Não foi possível criar o administrador.');
        },
      });
  }
}
