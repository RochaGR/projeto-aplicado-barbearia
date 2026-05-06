import { Component, OnInit, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/auth.service';

@Component({
  selector: 'app-oauth2-callback',
  standalone: true,
  template: `
    <div class="callback-container">
      <div class="spinner"></div>
      <p>Autenticando com Google...</p>
    </div>
  `,
  styles: [`
    :host {
      display: block;
      min-height: 100vh;
      background: linear-gradient(135deg, #0a0b0f 0%, #1a1d29 50%, #0f1117 100%);
    }
    .callback-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      min-height: 100vh;
      color: rgba(255, 255, 255, 0.8);
      font-size: 1.1rem;
    }
    .spinner {
      width: 48px;
      height: 48px;
      border: 4px solid rgba(255, 255, 255, 0.15);
      border-top-color: var(--bb-accent, #c8913c);
      border-radius: 50%;
      animation: spin 0.8s linear infinite;
      margin-bottom: 1.5rem;
    }
    @keyframes spin {
      to { transform: rotate(360deg); }
    }
  `],
})
export class OAuth2CallbackComponent implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  ngOnInit(): void {
    // A sessão já foi criada pelo Spring Security no backend.
    // Basta chamar /api/auth/me para obter os dados do usuário logado.
    this.auth.refreshUser().subscribe({
      next: (user) => {
        if (user) {
          if (user.roles.includes('ADMIN')) {
            void this.router.navigateByUrl('/admin/dashboard');
          } else if (user.roles.includes('BARBEIRO')) {
            void this.router.navigateByUrl('/barbeiro/agendamentos');
          } else {
            void this.router.navigateByUrl('/agendamentos');
          }
        } else {
          void this.router.navigateByUrl('/login');
        }
      },
      error: () => {
        void this.router.navigateByUrl('/login');
      },
    });
  }
}
