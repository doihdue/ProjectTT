import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export type StudentProfile = {
  id: number;
  mssv: string;
  hoTen: string;
  ngaySinh: string | null;
  gioiTinh: string | null;
  email: string;
  soDienThoai: string | null;
  diaChi: string | null;
  ngayNhapHoc: string | null;
  lopId?: number | null;
  lopTen: string | null;
  khoaId?: number | null;
  khoaTen: string | null;
  active?: boolean;
};

export type StudentProfileUpdate = {
  email: string;
  soDienThoai: string;
  diaChi: string;
};

export type ChangePasswordPayload = {
  oldPassword: string;
  newPassword: string;
};

@Injectable({ providedIn: 'root' })
export class StudentService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8080/api';

  getProfile(): Observable<StudentProfile> {
    return this.http.get<StudentProfile>(`${this.apiUrl}/sinh-vien/me`);
  }

  updateProfile(payload: StudentProfileUpdate): Observable<StudentProfile> {
    return this.http.patch<StudentProfile>(`${this.apiUrl}/sinh-vien/me/profile`, payload);
  }

  changePassword(payload: ChangePasswordPayload): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.apiUrl}/auth/change-password`, payload);
  }
}