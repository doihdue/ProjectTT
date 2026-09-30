import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { DiemRenLuyenItem, DiemRenLuyenService } from '../../services/diem-ren-luyen.service';

export type DashboardStats = {
  tongSinhVien: number;
  tongKhoa: number;
  tongLop: number;
  phieuDrlChoDuyet: number;
  phieuDrlDaDuyet: number;
  phanBoXepLoaiDrl: { [key: string]: number };
};

export type ThongKeKhoaItem = {
  khoaId: number;
  maKhoa: string;
  tenKhoa: string;
  tongSinhVien: number;
  soPhieuDaNop: number;
  soPhieuDaDuyet: number;
  diemTrungBinh: number;
  diemCaoNhat: number;
};

@Component({
  selector: 'app-section',
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './section.component.html',
  styleUrl: './section.component.scss',
})
export class SectionComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly authService = inject(AuthService);
  private readonly drlService = inject(DiemRenLuyenService);
  private readonly cd = inject(ChangeDetectorRef);

  protected get isStudent(): boolean {
    return this.authService.isStudent;
  }

  protected get currentUserName(): string {
    const name = this.authService.currentUser?.fullName;
    if (!name || name.includes('?')) {
      return this.isStudent ? 'Sinh viên' : 'Quản trị hệ thống';
    }
    return name;
  }

  protected stats: DashboardStats = {
    tongSinhVien: 0,
    tongKhoa: 0,
    tongLop: 0,
    phieuDrlChoDuyet: 0,
    phieuDrlDaDuyet: 0,
    phanBoXepLoaiDrl: {},
  };

  protected khoaStats: ThongKeKhoaItem[] = [];
  protected latestDrl: DiemRenLuyenItem | null = null;
  protected loading = false;

  // --- PHÂN TRANG (PAGINATION) BẢNG THỐNG KÊ KHOA ---
  protected currentPage = 1;
  protected pageSize = 5;

  protected get totalPages(): number {
    return Math.ceil(this.khoaStats.length / this.pageSize) || 1;
  }

  protected get startIndex(): number {
    return (this.currentPage - 1) * this.pageSize;
  }

  protected get endIndex(): number {
    return Math.min(this.startIndex + this.pageSize, this.khoaStats.length);
  }

  protected get paginatedKhoaStats(): ThongKeKhoaItem[] {
    const start = this.startIndex;
    return this.khoaStats.slice(start, start + this.pageSize);
  }

  protected get visiblePages(): number[] {
    const total = this.totalPages;
    const current = this.currentPage;
    const delta = 2;
    const range: number[] = [];
    for (let i = Math.max(1, current - delta); i <= Math.min(total, current + delta); i++) {
      range.push(i);
    }
    return range;
  }

  protected goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
      this.cd.markForCheck();
    }
  }

  protected onPageSizeChange(): void {
    this.currentPage = 1;
    this.cd.markForCheck();
  }

  ngOnInit(): void {
    if (!this.isStudent) {
      this.loadAdminStats();
    } else {
      this.loadStudentDrl();
    }
  }

  protected loadAdminStats(): void {
    this.loading = true;
    this.http.get<DashboardStats>('http://localhost:8080/api/thong-ke/dashboard').subscribe({
      next: (res) => {
        this.stats = res;
        this.loading = false;
        this.cd.markForCheck();
      },
      error: () => {
        this.loading = false;
        this.cd.markForCheck();
      },
    });

    this.http.get<ThongKeKhoaItem[]>('http://localhost:8080/api/thong-ke/khoa').subscribe({
      next: (res) => {
        this.khoaStats = res || [];
        this.cd.markForCheck();
      },
      error: () => {},
    });
  }


  protected loadStudentDrl(): void {
    this.drlService.getMyDrlList().subscribe({
      next: (list) => {
        if (list && list.length > 0) {
          this.latestDrl = list[0];
        }
        this.cd.markForCheck();
      },
      error: () => {},
    });
  }

  protected getBadgeClass(trangThai?: string): string {
    switch (trangThai) {
      case 'CHO_DUYET':
        return 'badge-pending';
      case 'DA_DUYET':
        return 'badge-approved';
      case 'TU_CHOI':
        return 'badge-rejected';
      default:
        return 'badge-secondary';
    }
  }

  protected getTrangThaiText(trangThai?: string): string {
    switch (trangThai) {
      case 'CHO_DUYET':
        return 'Đang chờ duyệt';
      case 'DA_DUYET':
        return 'Đã phê duyệt';
      case 'TU_CHOI':
        return 'Bị từ chối';
      default:
        return 'Chưa nộp phiếu';
    }
  }

  protected formatXepLoai(xepLoai?: string | null): string {
    if (!xepLoai) return '';
    const trimmed = xepLoai.trim();
    if (trimmed === 'T?t' || trimmed.toLowerCase() === 't?t' || trimmed.includes('?')) {
      return 'Tốt';
    }
    return trimmed;
  }

  protected getXepLoaiClass(xepLoai?: string | null): string {
    if (!xepLoai) return 'rank-weak';
    const formatted = this.formatXepLoai(xepLoai);
    switch (formatted) {
      case 'Xuất sắc':
        return 'rank-excellent';
      case 'Tốt':
        return 'rank-good';
      case 'Khá':
        return 'rank-fair';
      case 'Trung bình':
        return 'rank-average';
      default:
        return 'rank-weak';
    }
  }
}
