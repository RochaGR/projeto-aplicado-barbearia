import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth.service';

@Component({
  selector: 'app-main-navbar',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './main-navbar.component.html',
  styleUrl: './main-navbar.component.scss',
})
export class MainNavbarComponent {
  readonly auth = inject(AuthService);
  readonly user = this.auth.user;
  readonly menuOpen = signal(false);

  toggleMenu(): void {
    this.menuOpen.update((v) => !v);
  }

  closeMenu(): void {
    this.menuOpen.set(false);
  }

  sair(): void {
    this.auth.logout().subscribe(() => {
      window.location.href = '/';
    });
  }
}
