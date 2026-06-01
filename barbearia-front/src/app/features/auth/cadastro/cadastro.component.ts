import { Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { Router, RouterLink } from '@angular/router';
import { ApiService } from '../../../core/api.service';

@Component({
  selector: 'app-cadastro',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, FormsModule],
  templateUrl: './cadastro.component.html',
  styleUrl: './cadastro.component.scss',
})
export class CadastroComponent {
  private readonly api = inject(ApiService);
  private readonly router = inject(Router);

  readonly form = new FormGroup({
    nome: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.minLength(3), Validators.maxLength(60)] }),
    telefone: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.minLength(14)] }),
    email: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.email, Validators.pattern(/^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/)] }),
    senha: new FormControl('', {
      nonNullable: true,
      validators: [
        Validators.required,
        Validators.minLength(8),
        Validators.pattern(/.*[A-Z].*/),
        Validators.pattern(/.*\d.*/),
        Validators.pattern(/.*[^a-zA-Z0-9].*/),
      ],
    }),
  });
  readonly submitted = signal(false);
  readonly erro = signal<string | null>(null);
  readonly ok = signal<string | null>(null);
  readonly carregando = signal(false);
  readonly mostrarSenha = signal(false);
  private readonly senhaControl = this.form.controls.senha;
  private readonly senhaValue = toSignal(this.senhaControl.valueChanges, { initialValue: this.senhaControl.value });
  readonly senhaTemMinimo = computed(() => this.senhaValue().length >= 8);
  readonly senhaTemMaiuscula = computed(() => /[A-Z]/.test(this.senhaValue()));
  readonly senhaTemNumero = computed(() => /\d/.test(this.senhaValue()));
  readonly senhaTemEspecial = computed(() => /[^a-zA-Z0-9]/.test(this.senhaValue()));

  enviar(): void {
    this.submitted.set(true);
    this.form.markAllAsTouched();
    if (this.form.invalid) {
      return;
    }

    const { nome, telefone, email, senha } = this.form.getRawValue();
    this.erro.set(null);
    this.ok.set(null);
    this.carregando.set(true);
    this.api
      .cadastroCliente({
        nome: nome.trim(),
        telefone: telefone.trim(),
        email: email.trim(),
        senha,
      })
      .subscribe({
        next: () => {
          this.carregando.set(false);
          this.submitted.set(false);
          this.form.reset({ nome: '', telefone: '', email: '', senha: '' });
          this.ok.set('Cadastro realizado! Você já pode entrar.');
          setTimeout(() => void this.router.navigateByUrl('/login'), 1500);
        },
        error: (err: HttpErrorResponse) => {
          this.carregando.set(false);
          const backendErrors = err.error?.errors as Record<string, string> | undefined;
          if (backendErrors) {
            for (const [k, message] of Object.entries(backendErrors)) {
              const c = this.form.get(k);
              if (c) {
                c.setErrors({ ...(c.errors ?? {}), backend: message });
              }
            }
          }
          this.erro.set(err.error?.message ?? 'Não foi possível concluir o cadastro. Verifique os dados.');
        },
      });
  }

  controlInvalid(name: 'nome' | 'telefone' | 'email' | 'senha'): boolean {
    const c = this.form.controls[name];
    return c.invalid && (c.touched || this.submitted());
  }

  controlMessage(name: 'nome' | 'telefone' | 'email' | 'senha'): string {
    const c = this.form.controls[name];
    if (!(c.touched || this.submitted()) || !c.errors) return '';
    if (c.errors['backend']) return String(c.errors['backend']);
    if (c.errors['required']) {
      if (name === 'nome') return 'Nome é obrigatório.';
      if (name === 'telefone') return 'Telefone é obrigatório.';
      if (name === 'email') return 'Email é obrigatório.';
      return 'Senha é obrigatória.';
    }
    if (name === 'nome' && c.errors['minlength']) return 'Nome deve ter no mínimo 3 caracteres.';
    if (name === 'nome' && c.errors['maxlength']) return 'Nome deve ter no máximo 60 caracteres.';
    if (name === 'telefone' && c.errors['minlength']) return 'Telefone inválido.';
    if (name === 'email' && c.errors['email']) return 'Informe um email válido.';
    if (name === 'email' && c.errors['pattern']) return 'Informe um email válido (ex: nome@dominio.com).';
    if (name === 'senha') {
      if (c.errors['minlength']) return 'A senha deve ter no mínimo 8 caracteres.';
      if (!/.*[A-Z].*/.test(c.value)) return 'A senha deve conter pelo menos 1 letra maiúscula.';
      if (!/.*\d.*/.test(c.value)) return 'A senha deve conter pelo menos 1 número.';
      if (!/.*[^a-zA-Z0-9].*/.test(c.value)) return 'A senha deve conter 1 caractere especial.';
    }
    return 'Campo inválido.';
  }

  senhaStrength(): 'fraca' | 'media' | 'forte' | null {
    const senha = this.form.controls.senha.value;
    if (!senha) return null;
    let score = 0;
    if (senha.length >= 8) score++;
    if (/[A-Z]/.test(senha)) score++;
    if (/\d/.test(senha)) score++;
    if (/[^a-zA-Z0-9]/.test(senha)) score++;
    if (score <= 2) return 'fraca';
    if (score === 3) return 'media';
    return 'forte';
  }

  onTelefoneInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    let value = input.value.replace(/\D/g, '');
    if (value.length > 11) { value = value.substring(0, 11); }

    let formatted = '';
    if (value.length <= 10) {
      if (value.length > 6) {
        formatted = '(' + value.substring(0,2) + ') ' + value.substring(2,6) + '-' + value.substring(6);
      } else if (value.length > 2) {
        formatted = '(' + value.substring(0,2) + ') ' + value.substring(2);
      } else if (value.length > 0) {
        formatted = '(' + value;
      }
    } else {
      if (value.length > 7) {
        formatted = '(' + value.substring(0,2) + ') ' + value.substring(2,7) + '-' + value.substring(7);
      } else if (value.length > 2) {
        formatted = '(' + value.substring(0,2) + ') ' + value.substring(2);
      } else if (value.length > 0) {
        formatted = '(' + value;
      }
    }

    this.form.patchValue({ telefone: formatted }, { emitEvent: false });
    input.value = formatted;
  }
}
