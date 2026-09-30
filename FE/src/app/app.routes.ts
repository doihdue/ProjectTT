import { Routes } from '@angular/router';
import { authGuard } from './services/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./pages/login/login.component').then((m) => m.LoginComponent),
  },
  {
    canActivate: [authGuard],
    path: '',
    loadComponent: () => import('./layout/shell.component').then((m) => m.ShellComponent),
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      {
        path: 'dashboard',
        loadComponent: () => import('./pages/section/section.component').then((m) => m.SectionComponent),
      },
      // Phân hệ Quản trị (Admin)
      {
        path: 'quan-ly/khoa',
        loadComponent: () =>
          import('./pages/admin/khoa/admin-khoa.component').then((m) => m.AdminKhoaComponent),
      },
      {
        path: 'quan-ly/lop',
        loadComponent: () =>
          import('./pages/admin/lop/admin-lop.component').then((m) => m.AdminLopComponent),
      },
      {
        path: 'quan-ly/sinh-vien',
        loadComponent: () =>
          import('./pages/admin/sinh-vien/admin-sinh-vien.component').then((m) => m.AdminSinhVienComponent),
      },
      {
        path: 'quan-ly/diem-ren-luyen',
        loadComponent: () =>
          import('./pages/admin/diem-ren-luyen/admin-drl.component').then((m) => m.AdminDrlComponent),
      },
      // Cổng Thông tin Sinh viên (Student Portal)
      {
        path: 'sinh-vien/danh-gia-drl',
        data: { mode: 'evaluation' },
        loadComponent: () =>
          import('./pages/student-portal/student-portal.component').then((m) => m.StudentPortalComponent),
      },
      {
        path: 'sinh-vien/ket-qua-drl',
        data: { mode: 'results' },
        loadComponent: () =>
          import('./pages/student-portal/student-portal.component').then((m) => m.StudentPortalComponent),
      },
      {
        path: 'sinh-vien/ho-so-ca-nhan',
        data: { mode: 'profile' },
        loadComponent: () =>
          import('./pages/student-portal/student-portal.component').then((m) => m.StudentPortalComponent),
      },
    ],
  },
  { path: '**', redirectTo: 'dashboard' },
];
