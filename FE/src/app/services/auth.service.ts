import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export type LoginPayload = {
  username: string;
  password: string;
};

export type LoginResult = {
  accessToken: string;
  tokenType: string;
  username: string;
  fullName: string;
  role: string;
};

export type CurrentUser = {
  username: string;
  fullName: string;
  role: string;
};

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8080/api/auth';

  login(payload: LoginPayload): Observable<LoginResult> {
    return this.http.post<LoginResult>(`${this.apiUrl}/login`, payload);
  }

  saveSession(result: LoginResult): void {
    if (result) {
      if (result.username === 'admin' && (!result.fullName || result.fullName.includes('?') || result.fullName !== 'Quản trị hệ thống')) {
        result.fullName = 'Quản trị hệ thống';
      }
    }
    localStorage.setItem('accessToken', result.accessToken);
    const user: CurrentUser = {
      username: result.username,
      fullName: result.fullName,
      role: result.role,
    };
    localStorage.setItem('currentUser', JSON.stringify(user));
  }

  get currentUser(): CurrentUser | null {
    const value = localStorage.getItem('currentUser');
    if (!value) return null;
    try {
      const user = JSON.parse(value) as CurrentUser;
      if (user) {
        let changed = false;
        if (user.username === 'admin' && (!user.fullName || user.fullName.includes('?') || user.fullName !== 'Quản trị hệ thống')) {
          user.fullName = 'Quản trị hệ thống';
          changed = true;
        }
        if (changed) {
          localStorage.setItem('currentUser', JSON.stringify(user));
        }
      }
      return user;
    } catch {
      return null;
    }
  }

  get isStudent(): boolean {
    return this.currentUser?.role === 'STUDENT';
  }

  get isLecturer(): boolean {
    return this.currentUser?.role === 'LECTURER';
  }

  get isAdmin(): boolean {
    return this.currentUser?.role === 'ADMIN';
  }

  logout(): void {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('currentUser');
  }
}
