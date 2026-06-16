import { Component, OnInit, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { ApiService } from '../../../core/api.service';
import { Barbeiro } from '../../../core/models';
import { ConfirmService } from '../../../shared/confirm-modal/confirm.service';

@Component({
  selector: 'app-admin-barbeiros',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './barbeiros.component.html',
  styleUrl: './barbeiros.component.scss',
})
export class AdminBarbeirosComponent implements OnInit {
  private readonly api = inject(ApiService);
  private readonly confirm = inject(ConfirmService);

  readonly lista = signal<Barbeiro[]>([]);
  readonly carregando = signal(true);
  readonly erro = signal<string | null>(null);
  readonly acaoId = signal<number | null>(null);

  readonly form = new FormGroup({
    nome: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.minLength(3)] }),
    email: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.email] }),
    telefone: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
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
    ativo: new FormControl(true, { nonNullable: true }),
  });
  readonly submitted = signal(false);
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
    this.submitted.set(true);
    this.form.markAllAsTouched();
    if (this.form.invalid) {
      return;
    }

    const { nome, email, telefone, senha, ativo } = this.form.getRawValue();
    this.erro.set(null);
    this.criando.set(true);
    const payload = {
      nome: nome.trim(),
      email: email.trim(),
      telefone: telefone.trim(),
      ativo,
      senha,
    };

    this.api
      .cadastrarBarbeiroAdmin(payload)
      .subscribe({
        next: () => {
          this.criando.set(false);
          this.submitted.set(false);
          this.form.reset({ nome: '', email: '', telefone: '', senha: '', ativo: true });
          this.carregar();
        },
        error: (err: HttpErrorResponse) => {
          this.criando.set(false);
          const backendErrors = err.error?.errors as Record<string, string> | undefined;
          if (backendErrors) {
            for (const [k, message] of Object.entries(backendErrors)) {
              const c = this.form.get(k);
              if (c) {
                c.setErrors({ ...(c.errors ?? {}), backend: message });
              }
            }
          }
          const errorMsg = err.error?.message || err.message || 'Não foi possível cadastrar o barbeiro.';
          this.erro.set(errorMsg);
        },
      });
  }

  controlInvalid(name: 'nome' | 'email' | 'telefone' | 'senha'): boolean {
    const c = this.form.controls[name];
    return c.invalid && (c.touched || this.submitted());
  }

  controlMessage(name: 'nome' | 'email' | 'telefone' | 'senha'): string {
    const c = this.form.controls[name];
    if (!(c.touched || this.submitted()) || !c.errors) {
      return '';
    }
    if (c.errors['backend']) {
      return String(c.errors['backend']);
    }
    if (c.errors['required']) {
      if (name === 'senha') return 'Senha é obrigatória.';
      if (name === 'email') return 'Email é obrigatório.';
      if (name === 'nome') return 'Nome é obrigatório.';
      return 'Telefone é obrigatório.';
    }
    if (name === 'nome' && c.errors['minlength']) {
      return 'Nome deve ter no mínimo 3 caracteres.';
    }
    if (name === 'email' && c.errors['email']) {
      return 'Informe um email válido.';
    }
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

  async excluir(b: Barbeiro): Promise<void> {
    if (!(await this.confirm.confirm(`Excluir permanentemente ${b.nome}?`))) {
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
