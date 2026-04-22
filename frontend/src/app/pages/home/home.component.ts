import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { TokenService } from '../../features/auth/services/token.service';

@Component({
  selector: 'app-home',
  template: `
    <div class="home-container">
      <div class="home-card">
        <h1>🎉 Welcome to Homepage</h1>
        <p class="welcome-text">You have successfully logged in!</p>
        
        <div class="user-info" *ngIf="email">
          <p><strong>Email:</strong> {{ email }}</p>
        </div>

        <button class="logout-btn" (click)="logout()">
          Logout
        </button>
      </div>
    </div>
  `,
  styles: [`
    .home-container {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 100vh;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    }

    .home-card {
      background: white;
      padding: 40px;
      border-radius: 10px;
      box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
      text-align: center;
      max-width: 500px;
    }

    h1 {
      color: #333;
      margin-bottom: 20px;
      font-size: 2.5rem;
    }

    .welcome-text {
      color: #666;
      font-size: 1.1rem;
      margin-bottom: 30px;
    }

    .user-info {
      background: #f5f5f5;
      padding: 20px;
      border-radius: 8px;
      margin: 20px 0;
      text-align: left;
    }

    .user-info p {
      margin: 10px 0;
      color: #555;
    }

    .logout-btn {
      background-color: #667eea;
      color: white;
      border: none;
      padding: 12px 30px;
      font-size: 1rem;
      border-radius: 5px;
      cursor: pointer;
      transition: background-color 0.3s;
      margin-top: 20px;
    }

    .logout-btn:hover {
      background-color: #764ba2;
    }
  `],
  imports: [CommonModule],
})
export class HomeComponent {
  email: string | null = null;

  constructor(
    private tokenService: TokenService,
    private router: Router
  ) {
    this.email = this.tokenService.getEmail();
  }

  logout() {
    this.tokenService.clear();
    this.router.navigate(['/login']);
  }
}
