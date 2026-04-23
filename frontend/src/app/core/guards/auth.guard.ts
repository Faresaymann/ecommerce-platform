import { Injectable } from '@angular/core';
import { CanActivate, Router, UrlTree } from '@angular/router';
import { TokenService } from '../services/token.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {

  constructor(
    private router: Router,
    private tokenService: TokenService
  ) {}

  canActivate(): boolean | UrlTree {
    console.log('🔐 Auth Guard Running - Checking if user is logged in...');

    const isLoggedIn = this.tokenService.isLoggedIn();

    if (!isLoggedIn) {
      console.log('❌ Access Denied - No valid token found. Redirecting to /auth');
      return this.router.createUrlTree(['/auth']);
    }

    console.log('✅ Access Granted - User is authenticated');
    return true;
  }
}