import { Routes } from '@angular/router';
import { AuthGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: 'auth',
    loadComponent: () =>
      import('./features/auth/pages/auth.component').then(m => m.AuthComponent),
  },
  {
    path: 'home',
    loadComponent: () =>
      import('./features/home/pages/home/home.component').then(m => m.HomeComponent),
    canActivate: [AuthGuard],
  },
  {
    path: '',
    redirectTo: 'auth',
    pathMatch: 'full',
  },
];