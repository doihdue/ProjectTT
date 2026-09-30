import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);

  protected readonly form = this.fb.nonNullable.group({
    username: ['', [Validators.required]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    remember: [true],
  });

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.authService.login({
      username: this.form.controls.username.value,
      password: this.form.controls.password.value,
    }).subscribe({
      next: (result) => {
        this.authService.saveSession(result);
        if (result.role === 'STUDENT') {
          void this.router.navigate(['/sinh-vien/ho-so-ca-nhan']);
        } else if (result.role === 'LECTURER') {
          void this.router.navigate(['/giang-vien/lop-mon-hoc']);
        } else {
          void this.router.navigate(['/dashboard']);
        }
      },
      error: () => {
        this.form.controls.password.setErrors({ invalidCredentials: true });
      },
    });
  }
}
