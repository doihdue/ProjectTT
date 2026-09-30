import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';

export const authGuard: CanActivateFn = () => {
  const router = inject(Router);
  return localStorage.getItem('accessToken')
    ? true
    : router.createUrlTree(['/login']);
};
