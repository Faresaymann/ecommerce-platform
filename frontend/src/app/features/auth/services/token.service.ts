import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class TokenService {
  private ACCESS_TOKEN = 'accessToken';
  private REFRESH_TOKEN = 'refreshToken';
  private EMAIL = 'email';

  setTokens(access: string, refresh: string) {
    localStorage.setItem(this.ACCESS_TOKEN, access);
    localStorage.setItem(this.REFRESH_TOKEN, refresh);
  }

  setEmail(email: string) {
    localStorage.setItem(this.EMAIL, email);
  }

  getAccessToken(): string | null {
    return localStorage.getItem(this.ACCESS_TOKEN);
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(this.REFRESH_TOKEN);
  }

  getEmail(): string | null {
    return localStorage.getItem(this.EMAIL);
  }

  isLoggedIn(): boolean {
    return !!this.getAccessToken();
  }

  clear() {
    localStorage.removeItem(this.ACCESS_TOKEN);
    localStorage.removeItem(this.REFRESH_TOKEN);
    localStorage.removeItem(this.EMAIL);
  }
}