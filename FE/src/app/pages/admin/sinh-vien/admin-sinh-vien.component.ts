import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';

export type SinhVienItem = {
  id: number;
  mssv: string;
  hoTen: string;
  ngaySinh?: string;
  gioiTinh?: string;
  email: string;
  soDienThoai?: string;
  diaChi?: string;
  ngayNhapHoc?: string;
  lopId: number;
  lopTen?: string;
  khoaId?: number;
  khoaTen?: string;
  khoaHoc?: string;
  active: boolean;
};

export type LopOption = {
  id: number;
  maLop: string;
  tenLop: string;
  khoaTen?: string;
};

export function removeVietnameseTones(str: string): string {
  if (!str) return '';
  str = str.normalize('NFD').replace(/[\u0300-\u036f]/g, '');
  str = str.replace(/[đĐ]/g, 'd');
  return str;
}

export function generateStudentEmail(hoTen: string, mssv: string): string {
  if (!hoTen && !mssv) return '';
  const cleanName = removeVietnameseTones(hoTen.trim()).toLowerCase();
  const words = cleanName.split(/\s+/).filter(Boolean);
  let namePart = '';
  if (words.length > 0) {
    const lastName = words[words.length - 1];
    const initials = words.slice(0, words.length - 1).map((w) => w[0]).join('');
    namePart = lastName + initials;
  }
  let mssvPart = (mssv || '').trim();
  if (/^\d{4}/.test(mssvPart)) {
    mssvPart = mssvPart.substring(4);
  }
  if (!namePart && !mssvPart) return '';
  if (!namePart) return `${mssvPart}@.stu.edu.vn`;
  if (!mssvPart) return `${namePart}@.stu.edu.vn`;
  return `${namePart}.${mssvPart}@.stu.edu.vn`;
}

@Component({
  selector: 'app-admin-sinh-vien',
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './admin-sinh-vien.component.html',
  styleUrl: './admin-sinh-vien.component.scss',
})
export class AdminSinhVienComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly fb = inject(FormBuilder);
  private readonly cd = inject(ChangeDetectorRef);

  protected sinhViens: SinhVienItem[] = [];
  protected lops: LopOption[] = [];

  protected searchText = '';
  protected filterLopId = '';
  protected filterGender = '';

  protected editingId: number | null = null;
  protected loading = false;
  protected saving = false;
  protected errorMessage = '';
  protected successMessage = '';

  protected readonly form = this.fb.group({
    lopId: ['', Validators.required],
    ngayNhapHoc: ['2022-09-05'],
    mssv: [''],
    hoTen: ['', [Validators.required, Validators.maxLength(150)]],
    ngaySinh: [''],
    gioiTinh: ['Nam'],
    email: [''],
    soDienThoai: ['', [Validators.maxLength(20)]],
    diaChi: ['', [Validators.maxLength(255)]],
    active: [true],
  });

  protected get previewEmail(): string {
    const hoTen = this.form.get('hoTen')?.value || '';
    const mssv = this.form.get('mssv')?.value || '';
    return generateStudentEmail(hoTen, mssv);
  }

  ngOnInit(): void {
    this.loadLops();
    this.loadSinhViens();

    this.form.get('lopId')?.valueChanges.subscribe(() => {
      if (this.editingId === null) {
        this.autoFillMssv();
      }
    });

    this.form.get('ngayNhapHoc')?.valueChanges.subscribe(() => {
      if (this.editingId === null && this.form.get('lopId')?.value) {
        this.autoFillMssv();
      }
    });
  }

  protected autoFillMssv(): void {
    const lopId = this.form.get('lopId')?.value;
    const ngayNhapHoc = this.form.get('ngayNhapHoc')?.value || '';
    if (!lopId) return;

    const params: Record<string, string> = { lopId: String(lopId) };
    if (ngayNhapHoc) {
      params['ngayNhapHoc'] = ngayNhapHoc;
    }

    this.http
      .get<{ mssv: string }>('http://localhost:8080/api/sinh-vien/next-mssv', { params })
      .subscribe({
        next: (res) => {
          if (res?.mssv) {
            this.form.patchValue({ mssv: res.mssv });
            this.cd.detectChanges();
          }
        },
      });
  }

  protected loadLops(): void {
    this.http.get<LopOption[]>('http://localhost:8080/api/lop').subscribe({
      next: (res) => {
        this.lops = res;
        this.cd.detectChanges();
      },
    });
  }

  protected loadSinhViens(): void {
    this.loading = true;
    this.errorMessage = '';
    this.http.get<SinhVienItem[]>('http://localhost:8080/api/sinh-vien').subscribe({
      next: (res) => {
        this.sinhViens = res;
        this.loading = false;
        this.cd.detectChanges();
      },
      error: () => {
        this.errorMessage = 'Không thể tải danh sách sinh viên.';
        this.loading = false;
        this.cd.detectChanges();
      },
    });
  }

  protected get filteredSinhViens(): SinhVienItem[] {
    const q = this.searchText.trim().toLowerCase();
    return this.sinhViens.filter((item) => {
      const matchSearch =
        !q ||
        item.mssv.toLowerCase().includes(q) ||
        item.hoTen.toLowerCase().includes(q) ||
        item.email.toLowerCase().includes(q) ||
        (item.soDienThoai && item.soDienThoai.includes(q));
      const matchLop = !this.filterLopId || String(item.lopId) === this.filterLopId;
      const matchGender = !this.filterGender || item.gioiTinh === this.filterGender;
      return matchSearch && matchLop && matchGender;
    });
  }

  // --- PHÂN TRANG (PAGINATION) ---
  protected currentPage = 1;
  protected pageSize = 10;

  protected onFilterChange(): void {
    this.currentPage = 1;
  }

  protected onPageSizeChange(): void {
    this.currentPage = 1;
  }

  protected get totalPages(): number {
    return Math.ceil(this.filteredSinhViens.length / this.pageSize) || 1;
  }

  protected get startIndex(): number {
    return (this.currentPage - 1) * this.pageSize;
  }

  protected get endIndex(): number {
    return Math.min(this.startIndex + this.pageSize, this.filteredSinhViens.length);
  }

  protected get paginatedSinhViens(): SinhVienItem[] {
    const start = this.startIndex;
    return this.filteredSinhViens.slice(start, start + this.pageSize);
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
      this.cd.detectChanges();
    }
  }

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const val = this.form.getRawValue();
    const payload = {
      mssv: val.mssv?.trim(),
      hoTen: val.hoTen?.trim(),
      ngaySinh: val.ngaySinh || null,
      gioiTinh: val.gioiTinh || 'Nam',
      email: val.email?.trim() || null,
      soDienThoai: val.soDienThoai?.trim() || null,
      diaChi: val.diaChi?.trim() || null,
      ngayNhapHoc: val.ngayNhapHoc || null,
      lopId: Number(val.lopId),
      active: val.active ?? true,
    };

    this.saving = true;
    this.errorMessage = '';
    this.successMessage = '';

    const req = this.editingId === null
      ? this.http.post<SinhVienItem>('http://localhost:8080/api/sinh-vien', payload)
      : this.http.put<SinhVienItem>(`http://localhost:8080/api/sinh-vien/${this.editingId}`, payload);

    req.subscribe({
      next: () => {
        this.saving = false;
        this.successMessage = this.editingId === null
          ? 'Thêm mới sinh viên thành công! Tài khoản đăng nhập đã được tạo (mật khẩu mặc định: ngày sinh ddMMyyyy).'
          : 'Cập nhật thông tin sinh viên thành công!';
        this.loadSinhViens();
        this.resetForm();
      },
      error: (err) => {
        this.saving = false;
        this.errorMessage = err?.error?.message || 'Không thể lưu sinh viên. Kiểm tra MSSV và email có bị trùng không.';
        this.cd.detectChanges();
      },
    });
  }

  protected edit(item: SinhVienItem): void {
    this.editingId = item.id;
    this.form.setValue({
      mssv: item.mssv,
      hoTen: item.hoTen,
      ngaySinh: item.ngaySinh || '',
      gioiTinh: item.gioiTinh || 'Nam',
      email: item.email,
      soDienThoai: item.soDienThoai || '',
      diaChi: item.diaChi || '',
      ngayNhapHoc: item.ngayNhapHoc || '',
      lopId: String(item.lopId),
      active: item.active,
    });
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  protected remove(item: SinhVienItem): void {
    if (!confirm(`Bạn có chắc muốn xóa sinh viên ${item.hoTen} (MSSV: ${item.mssv})? Phiếu điểm rèn luyện liên quan cũng sẽ bị xóa.`)) {
      return;
    }

    this.http.delete(`http://localhost:8080/api/sinh-vien/${item.id}`).subscribe({
      next: () => {
        this.sinhViens = this.sinhViens.filter((x) => x.id !== item.id);
        this.successMessage = `Đã xóa sinh viên ${item.mssv} thành công.`;
        this.cd.detectChanges();
      },
      error: () => {
        this.errorMessage = 'Không thể xóa sinh viên này.';
        this.cd.detectChanges();
      },
    });
  }

  protected resetForm(): void {
    this.editingId = null;
    this.form.reset({
      lopId: '',
      ngayNhapHoc: '2022-09-05',
      mssv: '',
      hoTen: '',
      ngaySinh: '',
      gioiTinh: 'Nam',
      email: '',
      soDienThoai: '',
      diaChi: '',
      active: true,
    });
  }

  protected getCohort(sv: SinhVienItem): string {
    if (sv.khoaHoc) {
      const num = sv.khoaHoc.replace(/\D+/g, '');
      if (num) return `Khóa ${num}`;
    }
    const matchYear = sv.mssv?.match(/^(\d{4})/);
    if (matchYear) return `Khóa ${matchYear[1]}`;
    const match = sv.mssv?.match(/^[A-Za-z]*(\d{2})/);
    return match ? `Khóa ${match[1]}` : '';
  }

  protected resetPassword(sv: SinhVienItem): void {
    if (!confirm(`Bạn có chắc muốn cấp lại mật khẩu đăng nhập cho sinh viên ${sv.hoTen} (${sv.mssv})?`)) {
      return;
    }
    this.http.post(`http://localhost:8080/api/sinh-vien/${sv.id}/reset-password`, {}, { responseType: 'text' }).subscribe({
      next: (pw) => {
        alert(`Đã cấp lại mật khẩu thành công!\n\nTài khoản (MSSV): ${sv.mssv}\nMật khẩu đăng nhập: ${pw}\n\n(Quy tắc: Ngày sinh dạng ddMMyyyy, hoặc 'student123' nếu không có ngày sinh)`);
      },
      error: () => {
        alert('Không thể đặt lại mật khẩu cho sinh viên này. Vui lòng kiểm tra lại!');
      },
    });
  }
}
