import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { ApiService } from './api.service';
import { LoginRequest, LoginResponse, RegisterRequest } from './models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly tokenKey = 'traveladviser-token';
  private readonly userNameKey = 'traveladviser-name';
  private readonly userIdKey = 'traveladviser-userId';

  constructor(private api: ApiService, private router: Router) {}

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.api.post<LoginResponse>('/auth/login', request).pipe(
      tap((response) => {
        this.setSession(response.token, response.name, response.userId);
      })
    );
  }

  register(request: RegisterRequest): Observable<LoginResponse> {
    return this.api.post<LoginResponse>('/auth/register', request).pipe(
      tap((response) => {
        this.setSession(response.token, response.name, response.userId);
      })
    );
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.userNameKey);
    localStorage.removeItem(this.userIdKey);
    this.router.navigate(['/auth']);
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  getUserName(): string | null {
    return localStorage.getItem(this.userNameKey);
  }

  getUserId(): number | null {
    const userId = localStorage.getItem(this.userIdKey);
    return userId ? parseInt(userId, 10) : null;
  }

  private setSession(token: string, name: string, userId: number): void {
    localStorage.setItem(this.tokenKey, token);
    localStorage.setItem(this.userNameKey, name);
    localStorage.setItem(this.userIdKey, userId.toString());
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }
}
