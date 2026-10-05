import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ToastService } from '../../services/toast.service';

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
  private readonly toastService = inject(ToastService);

  protected readonly form = this.fb.nonNullable.group({
    username: ['', [Validators.required]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    remember: [true],
  });

  constructor() {
    this.form.valueChanges.subscribe(() => {
      this.clearInvalidCredentialsError();
    });
  }

  private clearInvalidCredentialsError(): void {
    (['username', 'password'] as const).forEach((field) => {
      const ctrl = this.form.controls[field];
      if (ctrl.hasError('invalidCredentials')) {
        const errors = { ...ctrl.errors };
        delete errors['invalidCredentials'];
        ctrl.setErrors(Object.keys(errors).length > 0 ? errors : null);
      }
    });
  }

  protected submit(): void {
    this.clearInvalidCredentialsError();

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
        this.toastService.success('Đăng nhập thành công', `Xin chào, ${result.fullName || result.username}!`);
        if (result.role === 'STUDENT') {
          void this.router.navigate(['/sinh-vien/ho-so-ca-nhan']);
        } else if (result.role === 'LECTURER') {
          void this.router.navigate(['/giang-vien/lop-mon-hoc']);
        } else {
          void this.router.navigate(['/dashboard']);
        }
      },
      error: (err) => {
        this.form.controls.username.setErrors({ invalidCredentials: true });
        this.form.controls.username.markAsTouched();
        this.form.controls.password.setErrors({ invalidCredentials: true });
        this.form.controls.password.markAsTouched();
        if (err.status === 0) {
          this.toastService.error('Không thể kết nối đến máy chủ backend');
        }
      },
    });
  }
}
