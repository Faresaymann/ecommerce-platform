import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { AuthRoutingModule } from './auth-routing-module';
import { Login } from './pages/login/login.component';
import { Register } from './pages/register/register.component';

@NgModule({
  declarations: [Login, Register],
  imports: [CommonModule, AuthRoutingModule],
})
export class AuthModule {}
