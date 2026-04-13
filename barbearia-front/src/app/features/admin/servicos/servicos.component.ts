import { DecimalPipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { ApiService } from '../../../core/api.service';
import { Servico } from '../../../core/models';

@Component({
  selector: 'app-admin-servicos',
  standalone: true,
  imports: [ReactiveFormsModule, DecimalPipe],
  templateUrl: './servicos.component.html',
  styleUrl: './servicos.component.scss',
})
export class AdminServicosComponent implements OnInit {
  private readonly api = inject(ApiService);

  readonly lista = signal<Servico[]>([]);
  readonly carregando = signal(true);
  readonly erro = signal<string | null>(null);
  readonly acaoId = signal<number | null>(null);

  readonly form = new FormGroup({
    nome: new FormControl('', { nonNullable: true, validators: [Validators.required, Validators.minLength(3)] }),
    descricao: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    preco: new FormControl<number | null>(null, { validators: [Validators.required, Validators.min(0)] }),
    duracaoMinutos: new FormControl<number | null>(null, { validators: [Validators.required, Validators.min(1)] }),
    ativo: new FormControl(true, { nonNullable: true }),
  });
  readonly submitted = signal(false);
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
    this.submitted.set(true);
    this.form.markAllAsTouched();
    if (this.form.invalid) {
      return;
    }

    const { nome, descricao, preco, duracaoMinutos, ativo } = this.form.getRawValue();
    const p = preco as number;
    const d = duracaoMinutos as number;

    this.erro.set(null);
    this.criando.set(true);
    this.api
      .cadastrarServicoAdmin({
        nome: nome.trim(),
        descricao: descricao.trim(),
        preco: p,
        duracaoMinutos: d,
        ativo,
      })
      .subscribe({
        next: () => {
          this.criando.set(false);
          this.submitted.set(false);
          this.form.reset({ nome: '', descricao: '', preco: null, duracaoMinutos: null, ativo: true });
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
          this.erro.set(err.error?.message ?? 'Não foi possível cadastrar o serviço.');
        },
      });
  }

  controlInvalid(name: 'nome' | 'descricao' | 'preco' | 'duracaoMinutos'): boolean {
    const c = this.form.controls[name];
    return c.invalid && (c.touched || this.submitted());
  }

  controlMessage(name: 'nome' | 'descricao' | 'preco' | 'duracaoMinutos'): string {
    const c = this.form.controls[name];
    if (!(c.touched || this.submitted()) || !c.errors) {
      return '';
    }
    if (c.errors['backend']) return String(c.errors['backend']);
    if (c.errors['required']) return 'Campo obrigatório.';
    if (name === 'nome' && c.errors['minlength']) return 'Nome deve ter no mínimo 3 caracteres.';
    if (name === 'preco' && c.errors['min']) return 'Preço não pode ser negativo.';
    if (name === 'duracaoMinutos' && c.errors['min']) return 'Duração mínima é 1 minuto.';
    return 'Campo inválido.';
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
