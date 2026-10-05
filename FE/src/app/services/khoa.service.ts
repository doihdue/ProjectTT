import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { PageResponse } from '../pages/admin/sinh-vien/admin-sinh-vien.component';

export type Khoa = {
  id: number;
  maKhoa: string;
  tenKhoa: string;
  moTa: string | null;
  active: boolean;
};

export type KhoaPayload = {
  maKhoa: string;
  tenKhoa: string;
  moTa: string;
  active: boolean;
};

@Injectable({ providedIn: 'root' })
export class KhoaService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8080/api/khoa';

  findAll(): Observable<Khoa[]> {
    return this.http.get<Khoa[]>(this.apiUrl);
  }

  search(params: Record<string, string>): Observable<PageResponse<Khoa>> {
    return this.http.get<PageResponse<Khoa>>(`${this.apiUrl}/search`, { params });
  }

  create(payload: KhoaPayload): Observable<Khoa> {
    return this.http.post<Khoa>(this.apiUrl, payload);
  }

  update(id: number, payload: KhoaPayload): Observable<Khoa> {
    return this.http.put<Khoa>(`${this.apiUrl}/${id}`, payload);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
