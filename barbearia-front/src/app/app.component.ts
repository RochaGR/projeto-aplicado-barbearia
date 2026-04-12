import { Component, OnInit, inject, signal } from '@angular/core';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';
import { AuthService } from './core/auth.service';
import { MainFooterComponent } from './shared/layout/main-footer/main-footer.component';
import { MainNavbarComponent } from './shared/layout/main-navbar/main-navbar.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, MainNavbarComponent, MainFooterComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss',
})
export class AppComponent implements OnInit {
  private auth = inject(AuthService);
  private router = inject(Router);

  readonly showMiniFooter = signal(true);

  ngOnInit(): void {
    this.auth.refreshUser().subscribe();
    const setFooter = () => {
      const path = this.router.url.split('?')[0];
      this.showMiniFooter.set(path !== '/' && path !== '');
    };
    setFooter();
    this.router.events.pipe(filter((e) => e instanceof NavigationEnd)).subscribe(() => setFooter());
  }
}
