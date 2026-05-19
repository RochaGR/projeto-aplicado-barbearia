import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
})
export class LoginComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly form = new FormGroup({
    email: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.email] }),
    senha: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
  });
  readonly submitted = signal(false);
  readonly erro = signal<string | null>(null);
  readonly carregando = signal(false);
  readonly mostrarSenha = signal(false);

  entrar(): void {
    this.clearAuthError(this.form.controls.email);
    this.clearAuthError(this.form.controls.senha);
    this.submitted.set(true);
    this.form.markAllAsTouched();
    if (this.form.invalid) {
      return;
    }

    const { email, senha } = this.form.getRawValue();
    this.erro.set(null);
    this.carregando.set(true);
    this.auth.login(email.trim(), senha).subscribe({
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
        this.form.controls.email.setErrors({ ...(this.form.controls.email.errors ?? {}), auth: true });
        this.form.controls.senha.setErrors({ ...(this.form.controls.senha.errors ?? {}), auth: true });
        this.erro.set(e.message ?? 'Email ou senha inválidos');
      },
    });
  }

  controlInvalid(name: 'email' | 'senha'): boolean {
    const c = this.form.controls[name];
    return c.invalid && (c.touched || this.submitted());
  }

  controlMessage(name: 'email' | 'senha'): string {
    const c = this.form.controls[name];
    if (!(c.touched || this.submitted()) || !c.errors) return '';
    if (c.errors['auth']) return 'Email ou senha inválidos';
    if (c.errors['required']) return name === 'email' ? 'Email é obrigatório.' : 'Senha é obrigatória.';
    if (name === 'email' && c.errors['email']) return 'Informe um email válido.';
    return 'Campo inválido.';
  }

  private clearAuthError(control: FormControl<string>): void {
    if (!control.errors || !control.errors['auth']) {
      return;
    }
    const { auth, ...rest } = control.errors;
    control.setErrors(Object.keys(rest).length ? rest : null);
  }
}
