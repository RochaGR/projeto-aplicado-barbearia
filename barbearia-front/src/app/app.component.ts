import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';
import { FormsModule } from '@angular/forms';
import { AuthService } from './core/auth.service';
import { ApiService } from './core/api.service';
import { MainFooterComponent } from './shared/layout/main-footer/main-footer.component';
import { MainNavbarComponent } from './shared/layout/main-navbar/main-navbar.component';
import { ChatWidgetComponent } from './features/chat-ia/chat-widget.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, MainNavbarComponent, MainFooterComponent, FormsModule, ChatWidgetComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss',
})
export class AppComponent implements OnInit {
  private auth = inject(AuthService);
  private router = inject(Router);

  readonly showMiniFooter = signal(true);
  readonly showTelefoneModal = signal(false);
  readonly isLoggedIn = computed(() => {
    const u = this.auth.user();
    return u !== null && u !== undefined;
  });
  telefone = '';

  ngOnInit(): void {
    const setFooter = () => {
      const path = this.router.url.split('?')[0];
      this.showMiniFooter.set(path !== '/' && path !== '');
    };
    setFooter();
    this.router.events.pipe(filter((e) => e instanceof NavigationEnd)).subscribe(() => setFooter());

    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('oauth2') === 'success') {
      window.history.replaceState({}, document.title, window.location.pathname);
      const u = this.auth.user();
      const d = u as unknown as Record<string, unknown> | null;
      if (d && d['telefonePendente']) {
        this.showTelefoneModal.set(true);
      }
    }
  }

  private readonly api = inject(ApiService);

  salvarTelefone(): void {
    const telefoneLimpo = this.telefone.replace(/\D/g, '');
    if (!telefoneLimpo || telefoneLimpo.length < 10 || telefoneLimpo.length > 11) {
      alert('Telefone invalido. Digite um numero com 10 ou 11 digitos.');
      return;
    }
    this.api.completarCadastro({ telefone: telefoneLimpo }).subscribe({
      next: () => {
        this.showTelefoneModal.set(false);
        this.auth.refreshUser().subscribe();
        window.location.href = '/';
      },
      error: (err) => {
        alert('Erro ao salvar telefone: ' + (err.message || 'Tente novamente.'));
      },
    });
  }

  onTelefoneInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    let value = input.value.replace(/\D/g, '');
    
    if (value.length > 11) {
      value = value.substring(0, 11);
    }
    
    if (value.length <= 10) {
      if (value.length > 6) {
        this.telefone = '(' + value.substring(0,2) + ') ' + value.substring(2,6) + '-' + value.substring(6);
      } else if (value.length > 2) {
        this.telefone = '(' + value.substring(0,2) + ') ' + value.substring(2);
      } else if (value.length > 0) {
        this.telefone = '(' + value;
      } else {
        this.telefone = '';
      }
    } else {
      if (value.length > 7) {
        this.telefone = '(' + value.substring(0,2) + ') ' + value.substring(2,7) + '-' + value.substring(7);
      } else if (value.length > 2) {
        this.telefone = '(' + value.substring(0,2) + ') ' + value.substring(2);
      } else if (value.length > 0) {
        this.telefone = '(' + value;
      } else {
        this.telefone = '';
      }
    }
    
    input.value = this.telefone;
  }
}
