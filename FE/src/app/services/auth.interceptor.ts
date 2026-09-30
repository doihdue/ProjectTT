import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const router = inject(Router);
  const accessToken = localStorage.getItem('accessToken');

  const authorizedRequest = accessToken && !request.url.endsWith('/api/auth/login')
    ? request.clone({
        setHeaders: {
          Authorization: `Bearer ${accessToken}`,
        },
      })
    : request;

  return next(authorizedRequest).pipe(
    catchError((error: HttpErrorResponse) => {
      if ((error.status === 401 || error.status === 403) && !request.url.endsWith('/api/auth/login')) {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('currentUser');
        void router.navigate(['/login']);
      }
      return throwError(() => error);
    }),
  );
};
