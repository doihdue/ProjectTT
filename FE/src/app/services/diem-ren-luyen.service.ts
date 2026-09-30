import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export type DotDanhGiaItem = {
  id: number;
  tenDot: string;
  hocKy: number;
  namHoc: string;
  ngayBatDau?: string;
  ngayKetThuc?: string;
  trangThai: 'DANG_MO' | 'DONG';
  ghiChu?: string;
  thoiGianTao: string;
  soLuongPhieu: number;
};

export type DotDanhGiaRequest = {
  tenDot: string;
  hocKy: number;
  namHoc: string;
  ngayBatDau?: string;
  ngayKetThuc?: string;
  trangThai?: 'DANG_MO' | 'DONG';
  ghiChu?: string;
};

export type DiemRenLuyenItem = {
  id: number;
  sinhVienId: number;
  mssv: string;
  hoTen: string;
  email: string;
  lopId?: number;
  tenLop?: string;
  khoaId?: number;
  tenKhoa?: string;
  dotDanhGiaId?: number;
  tenDot?: string;
  hocKy: number;
  namHoc: string;
  tieuChi1Sv: number;
  tieuChi2Sv: number;
  tieuChi3Sv: number;
  tieuChi4Sv: number;
  tieuChi5Sv: number;
  tongDiemSv: number;
  tieuChi1Admin?: number;
  tieuChi2Admin?: number;
  tieuChi3Admin?: number;
  tieuChi4Admin?: number;
  tieuChi5Admin?: number;
  tongDiemAdmin?: number;
  xepLoai: string;
  ghiChuSinhVien?: string;
  minhChung?: string;
  nhanXetAdmin?: string;
  trangThai: 'CHO_DUYET' | 'DA_DUYET' | 'TU_CHOI';
  ngayNop: string;
  ngayDuyet?: string;
  nguoiDuyet?: string;
};

export type DiemRenLuyenSubmitRequest = {
  dotDanhGiaId?: number;
  hocKy: number;
  namHoc: string;
  tieuChi1Sv: number;
  tieuChi2Sv: number;
  tieuChi3Sv: number;
  tieuChi4Sv: number;
  tieuChi5Sv: number;
  ghiChuSinhVien?: string;
  minhChung?: string;
};

export type DiemRenLuyenDuyetRequest = {
  chapNhan: boolean;
  tieuChi1Admin?: number;
  tieuChi2Admin?: number;
  tieuChi3Admin?: number;
  tieuChi4Admin?: number;
  tieuChi5Admin?: number;
  nhanXetAdmin?: string;
};

export type DrlThongKe = {
  tongSoPhieu: number;
  soChoDuyet: number;
  soDaDuyet: number;
  soTuChoi: number;
  phanBoXepLoai: { [key: string]: number };
};

export type DrlFilterParams = {
  dotDanhGiaId?: number;
  trangThai?: string;
  hocKy?: number;
  namHoc?: string;
  khoaId?: number;
  lopId?: number;
  keyword?: string;
};

@Injectable({
  providedIn: 'root',
})
export class DiemRenLuyenService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = 'http://localhost:8080/api/diem-ren-luyen';
  private readonly dotBaseUrl = 'http://localhost:8080/api/dot-danh-gia';

  // --- QUẢN LÝ ĐỢT ĐÁNH GIÁ ĐRL ---
  getAllDots(): Observable<DotDanhGiaItem[]> {
    return this.http.get<DotDanhGiaItem[]>(this.dotBaseUrl);
  }

  getOpenDots(): Observable<DotDanhGiaItem[]> {
    return this.http.get<DotDanhGiaItem[]>(`${this.dotBaseUrl}/open`);
  }

  createDot(payload: DotDanhGiaRequest): Observable<DotDanhGiaItem> {
    return this.http.post<DotDanhGiaItem>(this.dotBaseUrl, payload);
  }

  updateDot(id: number, payload: DotDanhGiaRequest): Observable<DotDanhGiaItem> {
    return this.http.put<DotDanhGiaItem>(`${this.dotBaseUrl}/${id}`, payload);
  }

  toggleDotStatus(id: number): Observable<DotDanhGiaItem> {
    return this.http.put<DotDanhGiaItem>(`${this.dotBaseUrl}/${id}/toggle`, {});
  }

  deleteDot(id: number): Observable<void> {
    return this.http.delete<void>(`${this.dotBaseUrl}/${id}`);
  }

  // --- PHIẾU ĐIỂM RÈN LUYỆN ---
  submitDrl(payload: DiemRenLuyenSubmitRequest): Observable<DiemRenLuyenItem> {
    return this.http.post<DiemRenLuyenItem>(`${this.baseUrl}/submit`, payload);
  }

  getMyDrlList(): Observable<DiemRenLuyenItem[]> {
    return this.http.get<DiemRenLuyenItem[]>(`${this.baseUrl}/my`);
  }

  getAllForAdmin(filters?: DrlFilterParams): Observable<DiemRenLuyenItem[]> {
    let params = new HttpParams();
    if (filters) {
      if (filters.dotDanhGiaId) params = params.set('dotDanhGiaId', filters.dotDanhGiaId.toString());
      if (filters.trangThai) params = params.set('trangThai', filters.trangThai);
      if (filters.hocKy) params = params.set('hocKy', filters.hocKy.toString());
      if (filters.namHoc) params = params.set('namHoc', filters.namHoc);
      if (filters.khoaId) params = params.set('khoaId', filters.khoaId.toString());
      if (filters.lopId) params = params.set('lopId', filters.lopId.toString());
      if (filters.keyword) params = params.set('keyword', filters.keyword);
    }
    return this.http.get<DiemRenLuyenItem[]>(`${this.baseUrl}/admin`, { params });
  }

  getById(id: number): Observable<DiemRenLuyenItem> {
    return this.http.get<DiemRenLuyenItem>(`${this.baseUrl}/${id}`);
  }

  duyetDrl(id: number, payload: DiemRenLuyenDuyetRequest): Observable<DiemRenLuyenItem> {
    return this.http.put<DiemRenLuyenItem>(`${this.baseUrl}/${id}/duyet`, payload);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  getThongKe(): Observable<DrlThongKe> {
    return this.http.get<DrlThongKe>(`${this.baseUrl}/thong-ke`);
  }

  // --- XUẤT BÁO CÁO PDF (JASPER REPORT) ---
  exportPhieuPdf(id: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/${id}/export-pdf`, {
      responseType: 'blob',
    });
  }

  exportSummaryPdf(filters?: DrlFilterParams): Observable<Blob> {
    let params = new HttpParams();
    if (filters) {
      if (filters.dotDanhGiaId) params = params.set('dotDanhGiaId', filters.dotDanhGiaId.toString());
      if (filters.trangThai) params = params.set('trangThai', filters.trangThai);
      if (filters.hocKy) params = params.set('hocKy', filters.hocKy.toString());
      if (filters.namHoc) params = params.set('namHoc', filters.namHoc);
      if (filters.khoaId) params = params.set('khoaId', filters.khoaId.toString());
      if (filters.lopId) params = params.set('lopId', filters.lopId.toString());
      if (filters.keyword) params = params.set('keyword', filters.keyword);
    }
    return this.http.get(`${this.baseUrl}/export-summary-pdf`, {
      params,
      responseType: 'blob',
    });
  }

  downloadBlob(blob: Blob, filename: string): void {
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    link.style.display = 'none';
    document.body.appendChild(link);
    link.click();
    setTimeout(() => {
      try {
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
      } catch {
        // safely ignore cleanup errors
      }
    }, 200);
  }
}
