import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AuthResponse, RegisterResponse } from '../models/auth-response.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  
  private baseUrl = 'http://localhost:8080/api/v1/auth';

  constructor(private http: HttpClient) {}

  login(data: { email: string; password: string }): Observable<AuthResponse> {
    console.log('🔐 Login attempt to:', `${this.baseUrl}/login`, data);
    return this.http.post<AuthResponse>(`${this.baseUrl}/login`, data);
  }

  register(formData: FormData): Observable<RegisterResponse> {
    console.log('📝 Register attempt to:', `${this.baseUrl}/register`);
    return this.http.post<RegisterResponse>(`${this.baseUrl}/register`, formData);
  }

  verifyEmail(token: string): Observable<any> {
    console.log('✅ Verify email to:', `${this.baseUrl}/verify-email?token=${token}`);
    return this.http.get(`${this.baseUrl}/verify-email?token=${token}`);
  }
}