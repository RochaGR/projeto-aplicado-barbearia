import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, signal } from '@angular/core';
import { Observable, catchError, map, of, switchMap, tap, throwError } from 'rxjs';
import { UserInfo } from './models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  readonly user = signal<UserInfo | null | undefined>(undefined);

  constructor(private http: HttpClient) {}

  refreshUser(): Observable<UserInfo | null> {
    return this.http.get<UserInfo>('/api/auth/me').pipe(
      tap((u) => this.user.set(u)),
      catchError(() => {
        this.user.set(null);
        return of(null);
      }),
    );
  }

  login(email: string, password: string): Observable<UserInfo> {
    return this.http.post<UserInfo>('/api/auth/login', { email, password }).pipe(
      switchMap((res) => {
        if (res?.username && res?.roles) {
          this.user.set(res);
          return of(res);
        }
        return this.refreshUser().pipe(
          map((u) => {
            if (!u) {
              throw new Error('Não foi possível entrar.');
            }
            return u;
          }),
        );
      }),
      catchError((e: HttpErrorResponse) => {
        const msg =
          e.error && typeof e.error === 'object' && 'message' in e.error
            ? String((e.error as { message: string }).message)
            : e.status === 401
              ? 'Email ou senha inválidos.'
              : 'Não foi possível entrar.';
        return throwError(() => new Error(msg));
      }),
    );
  }

  logout(): Observable<void> {
    return this.http
      .post('/logout', null)
      .pipe(
        tap(() => this.user.set(null)),
        map(() => undefined),
        catchError(() => {
          this.user.set(null);
          return of(undefined);
        }),
      );
  }

  hasRole(role: string): boolean {
    const u = this.user();
    return !!u?.roles?.includes(role);
  }
}
