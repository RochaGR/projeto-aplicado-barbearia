import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map, take } from 'rxjs';
import { AuthService } from './auth.service';

export function roleGuard(roles: string[]): CanActivateFn {
  return () => {
    const auth = inject(AuthService);
    const router = inject(Router);
    return auth.refreshUser().pipe(
      take(1),
      map((u) => {
        if (!u) {
          return router.parseUrl('/login');
        }
        const ok = roles.some((r) => auth.hasRole(r));
        return ok ? true : router.parseUrl('/');
      }),
    );
  };
}
