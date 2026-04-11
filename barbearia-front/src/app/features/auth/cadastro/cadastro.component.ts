import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ApiService } from '../../../core/api.service';

@Component({
  selector: 'app-cadastro',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './cadastro.component.html',
  styleUrl: './cadastro.component.scss',
})
export class CadastroComponent {
  private readonly api = inject(ApiService);
  private readonly router = inject(Router);

  nome = '';
  telefone = '';
  email = '';
  senha = '';
  readonly erro = signal<string | null>(null);
  readonly ok = signal<string | null>(null);
  readonly carregando = signal(false);

  enviar(): void {
    this.erro.set(null);
    this.ok.set(null);
    this.carregando.set(true);
    this.api
      .cadastroCliente({
        nome: this.nome.trim(),
        telefone: this.telefone.trim(),
        email: this.email.trim(),
        senha: this.senha,
      })
      .subscribe({
        next: () => {
          this.carregando.set(false);
          this.ok.set('Cadastro realizado! Você já pode entrar.');
          setTimeout(() => void this.router.navigateByUrl('/login'), 1500);
        },
        error: () => {
          this.carregando.set(false);
          this.erro.set('Não foi possível concluir o cadastro. Verifique os dados.');
        },
      });
  }
}
