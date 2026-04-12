import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
})
export class LoginComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  email = '';
  senha = '';
  readonly erro = signal<string | null>(null);
  readonly carregando = signal(false);

  entrar(): void {
    this.erro.set(null);
    this.carregando.set(true);
    this.auth.login(this.email.trim(), this.senha).subscribe({
      next: (u) => {
        this.carregando.set(false);
        if (u.roles.includes('ADMIN')) {
          void this.router.navigateByUrl('/admin/dashboard');
        } else if (u.roles.includes('BARBEIRO')) {
          void this.router.navigateByUrl('/barbeiro/agendamentos');
        } else {
          void this.router.navigateByUrl('/agendamentos');
        }
      },
      error: (e: Error) => {
        this.carregando.set(false);
        this.erro.set(e.message ?? 'Não foi possível entrar.');
      },
    });
  }
}
