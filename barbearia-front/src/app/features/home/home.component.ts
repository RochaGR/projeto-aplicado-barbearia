import { DecimalPipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { Servico } from '../../core/models';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink, DecimalPipe],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss',
})
export class HomeComponent implements OnInit {
  private readonly api = inject(ApiService);
  readonly auth = inject(AuthService);

  readonly ano = new Date().getFullYear();
  readonly servicos = signal<Servico[]>([]);
  readonly setupNecessario = signal<boolean | null>(null);
  readonly carregando = signal(true);

  ngOnInit(): void {
    this.api.setupRequired().subscribe({
      next: (r) => this.setupNecessario.set(r.required),
      error: () => this.setupNecessario.set(null),
    });
    this.api.servicosPublicos().subscribe({
      next: (s) => {
        this.servicos.set(s);
        this.carregando.set(false);
      },
      error: () => this.carregando.set(false),
    });
  }
}
