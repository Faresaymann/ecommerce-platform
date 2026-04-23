import { Component, signal } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule, FormGroup } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { TokenService } from '../../../core/services/token.service';

@Component({
  selector: 'app-auth',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './auth.component.html',
  styleUrls: ['./auth.component.scss']
})
export class AuthComponent {
  isSignUp = false;

  submittedLogin = false;
  submittedRegister = false;

  loginForm: FormGroup;
  registerForm: FormGroup;

  loginLoading = signal(false);
  registerLoading = signal(false);

  loginError = signal<string | null>(null);
  registerError = signal<string | null>(null);
  registerSuccess = signal<string | null>(null);

  avatarPreview = signal<string | null>(null);

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private tokenService: TokenService,
    private router: Router
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });

    this.registerForm = this.fb.group({
      firstName: ['', [Validators.required, Validators.minLength(2)]],
      lastName: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      photo: [null as File | null]
    });
  }

  showSignUp() {
    this.isSignUp = true;
    this.submittedLogin = false;
    this.loginError.set(null);
    this.registerSuccess.set(null);
  }

  showSignIn() {
    this.isSignUp = false;
    this.submittedRegister = false;
    this.registerError.set(null);
  }

  onFileChange(event: Event) {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] || null;

    this.registerForm.patchValue({ photo: file });

    if (!file) {
      this.avatarPreview.set(null);
      return;
    }

    const reader = new FileReader();
    reader.onload = () => {
      this.avatarPreview.set(reader.result as string);
    };
    reader.readAsDataURL(file);
  }

  submitLogin() {
    this.submittedLogin = true;
   if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.loginLoading.set(true);
    this.loginError.set(null);

    const { email, password } = this.loginForm.value;

    this.authService.login({ email, password }).subscribe({
      next: (res) => {
        this.tokenService.setTokens(res.accessToken, res.refreshToken);
        this.tokenService.setEmail(email);
        this.loginLoading.set(false);
        this.router.navigate(['/home']);
      },
      error: (err) => {
        this.loginLoading.set(false);

        let errorMessage = 'Login failed. Please try again.';

        if (err.status === 0) {
          errorMessage = 'Cannot connect to backend. Is it running on http://localhost:8080?';
        } else if (err.status === 401) {
          errorMessage = 'Invalid email or password';
        } else if (err.status === 403) {
          errorMessage = 'Please verify your email first';
        } else if (err.status === 404) {
          errorMessage = 'User not found';
        } else if (err.status === 500) {
          errorMessage = 'Server error. Please try again later';
        } else if (err.error?.message) {
          errorMessage = err.error.message;
        }

        this.loginError.set(errorMessage);
      }
    });
  }

  submitRegister() {
    this.submittedRegister = true;
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }


    this.registerLoading.set(true);
    this.registerError.set(null);
    this.registerSuccess.set(null);

    const { firstName, lastName, email, password, photo } = this.registerForm.value;

    const formData = new FormData();
    formData.append('firstName', String(firstName ?? ''));
    formData.append('lastName', String(lastName ?? ''));
    formData.append('email', String(email ?? ''));
    formData.append('password', String(password ?? ''));

    if (photo) {
      formData.append('file', photo);
    }

    this.authService.register(formData).subscribe({
      next: () => {
        this.registerLoading.set(false);
        this.tokenService.setEmail(email);
        this.registerSuccess.set('Registration successful! Please check your email to verify your account.');
        this.registerForm.reset();
        this.avatarPreview.set(null);

        setTimeout(() => {
          this.isSignUp = false;
          this.registerSuccess.set(null);
        }, 3000);
      },
      error: (err) => {
        this.registerLoading.set(false);

        let errorMessage = 'Registration failed. Please try again.';

        if (err.status === 0) {
          errorMessage = 'Cannot connect to backend. Is it running on http://localhost:8080?';
        } else if (err.status === 400) {
          errorMessage = err.error?.message || 'Invalid data provided';
        } else if (err.status === 409) {
          errorMessage = 'Email already exists';
        } else if (err.status === 500) {
          errorMessage = 'Server error. Please try again later';
        } else if (err.error?.message) {
          errorMessage = err.error.message;
        }

        this.registerError.set(errorMessage);
      }
    });
  }

  invalid(form: any, controlName: string, submitted: boolean) {
    const control = form.get(controlName);
    return !!control && control.invalid && (control.touched || submitted);
  }
}