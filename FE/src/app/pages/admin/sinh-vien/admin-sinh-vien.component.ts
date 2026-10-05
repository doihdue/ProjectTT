import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';

export type PageResponse<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
};

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

import { ToastService } from '../../../services/toast.service';
import { ConfirmDialogService } from '../../../services/confirm-dialog.service';

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
  private readonly toastService = inject(ToastService);
  private readonly confirmDialog = inject(ConfirmDialogService);

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

  // --- SERVER-SIDE PHÂN TRANG & TÌM KIẾM (ENTITY MANAGER BE) ---
  protected currentPage = 1;
  protected pageSize = 10;
  protected totalElements = 0;
  protected totalPages = 1;

  private readonly searchSubject = new Subject<string>();

  protected readonly form = this.fb.group({
    lopId: ['', Validators.required],
    ngayNhapHoc: ['2022-09-05'],
    mssv: [''],
    hoTen: ['', [Validators.required, Validators.maxLength(150)]],
    ngaySinh: ['', [Validators.required]],
    gioiTinh: ['Nam'],
    email: [''],
    soDienThoai: ['', [Validators.maxLength(20)]],
    diaChi: ['', [Validators.maxLength(255)]],
    active: [true],
  });

  protected get todayDate(): string {
    return new Date().toISOString().split('T')[0];
  }

  protected get previewEmail(): string {
    const hoTen = this.form.get('hoTen')?.value || '';
    const mssv = this.form.get('mssv')?.value || '';
    return generateStudentEmail(hoTen, mssv);
  }

  ngOnInit(): void {
    this.loadLops();
    this.loadSinhViens();

    // Debounce tìm kiếm 350ms để tối ưu số lần gọi API về Backend EntityManager
    this.searchSubject
      .pipe(debounceTime(350), distinctUntilChanged())
      .subscribe(() => {
        this.currentPage = 1;
        this.loadSinhViens();
      });

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

  /**
   * Gọi API tìm kiếm & phân trang trực tiếp từ Backend (EntityManager + Dynamic JPQL)
   */
  protected loadSinhViens(): void {
    this.loading = true;
    this.errorMessage = '';

    const params: Record<string, string> = {
      page: String(this.currentPage - 1),
      size: String(this.pageSize),
    };

    const q = this.searchText.trim();
    if (q) {
      params['keyword'] = q;
    }
    if (this.filterLopId) {
      params['lopId'] = this.filterLopId;
    }
    if (this.filterGender) {
      params['gioiTinh'] = this.filterGender;
    }

    this.http
      .get<PageResponse<SinhVienItem>>('http://localhost:8080/api/sinh-vien/search', { params })
      .subscribe({
        next: (res) => {
          this.sinhViens = res?.content || [];
          this.totalElements = res?.totalElements || 0;
          this.totalPages = res?.totalPages || 1;
          this.loading = false;
          this.cd.detectChanges();
        },
        error: () => {
          this.errorMessage = 'Không thể tải danh sách sinh viên từ máy chủ.';
          this.loading = false;
          this.cd.detectChanges();
        },
      });
  }

  protected onSearchInput(val: string): void {
    this.searchText = val;
    this.searchSubject.next(val);
  }

  protected onFilterChange(): void {
    this.currentPage = 1;
    this.loadSinhViens();
  }

  protected onPageSizeChange(): void {
    this.currentPage = 1;
    this.loadSinhViens();
  }

  // Tương thích template
  protected get filteredSinhViens(): SinhVienItem[] {
    return this.sinhViens;
  }

  protected get paginatedSinhViens(): SinhVienItem[] {
    return this.sinhViens;
  }

  protected get startIndex(): number {
    return (this.currentPage - 1) * this.pageSize;
  }

  protected get endIndex(): number {
    return Math.min(this.startIndex + this.sinhViens.length, this.totalElements);
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
    if (page >= 1 && page <= this.totalPages && page !== this.currentPage) {
      this.currentPage = page;
      this.loadSinhViens();
    }
  }

  protected submit(): void {
    const isValid = this.toastService.validateForm(
      this.form,
      {
        lopId: 'Lớp học',
        hoTen: 'Họ và tên sinh viên',
        mssv: 'Mã số sinh viên (MSSV)',
        ngaySinh: 'Ngày sinh',
        soDienThoai: 'Số điện thoại',
        diaChi: 'Địa chỉ',
      },
      'Vui lòng điền đúng thông tin Sinh viên'
    );

    if (!isValid) {
      this.errorMessage = 'Vui lòng kiểm tra lại các trường thông tin bắt buộc (được đánh dấu đỏ).';
      return;
    }

    const val = this.form.getRawValue();

    if (val.ngaySinh && new Date(val.ngaySinh) > new Date()) {
      this.form.get('ngaySinh')?.setErrors({ futureDate: true });
      this.toastService.warning('Thông tin không hợp lệ', 'Ngày sinh phải là một ngày trong quá khứ.');
      this.errorMessage = 'Ngày sinh phải là một ngày trong quá khứ.';
      return;
    }

    const payload = {
      mssv: val.mssv?.trim(),
      hoTen: val.hoTen?.trim(),
      ngaySinh: val.ngaySinh,
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
        const msg = this.editingId === null
          ? 'Thêm mới sinh viên thành công! Tài khoản đăng nhập đã được tạo.'
          : 'Cập nhật thông tin sinh viên thành công!';
        this.successMessage = msg;
        this.toastService.success(
          this.editingId === null ? 'Thêm mới thành công' : 'Cập nhật thành công',
          msg
        );
        this.loadSinhViens();
        this.resetForm();
      },
      error: (err) => {
        this.saving = false;
        this.errorMessage = err?.error?.message || 'Không thể lưu sinh viên. Vui lòng kiểm tra lại thông tin.';
        this.toastService.showHttpError(err, 'Lưu thông tin Sinh viên thất bại', this.form);
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

  protected async remove(item: SinhVienItem): Promise<void> {
    const confirmed = await this.confirmDialog.confirm({
      title: 'Xác nhận xóa sinh viên',
      message: `Bạn có chắc chắn muốn xóa sinh viên ${item.hoTen} (MSSV: ${item.mssv})?`,
      detailMessage: 'Toàn bộ phiếu điểm rèn luyện và lịch sử liên quan của sinh viên này cũng sẽ bị xóa vĩnh viễn khỏi hệ thống.',
      confirmText: 'Xác nhận xóa',
      cancelText: 'Hủy bỏ',
      type: 'danger',
    });

    if (!confirmed) {
      return;
    }

    this.http.delete(`http://localhost:8080/api/sinh-vien/${item.id}`).subscribe({
      next: () => {
        const msg = `Đã xóa sinh viên ${item.mssv} (${item.hoTen}) thành công.`;
        this.successMessage = msg;
        this.toastService.success('Đã xóa thành công', msg);
        if (this.sinhViens.length === 1 && this.currentPage > 1) {
          this.currentPage--;
        }
        this.loadSinhViens();
        this.cd.detectChanges();
      },
      error: (err) => {
        this.errorMessage = 'Không thể xóa sinh viên này.';
        this.toastService.showHttpError(err, 'Không thể xóa sinh viên');
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

  protected async resetPassword(sv: SinhVienItem): Promise<void> {
    const confirmed = await this.confirmDialog.confirm({
      title: 'Cấp lại mật khẩu',
      message: `Bạn có chắc muốn cấp lại mật khẩu đăng nhập cho sinh viên ${sv.hoTen} (${sv.mssv})?`,
      detailMessage: 'Mật khẩu cũ của sinh viên sẽ bị thay thế bằng mật khẩu mặc định mới.',
      confirmText: 'Cấp lại mật khẩu',
      cancelText: 'Hủy bỏ',
      type: 'warning',
    });

    if (!confirmed) {
      return;
    }

    this.http.post(`http://localhost:8080/api/sinh-vien/${sv.id}/reset-password`, {}, { responseType: 'text' }).subscribe({
      next: (pw) => {
        this.confirmDialog.alert({
          title: 'Cấp lại mật khẩu thành công',
          message: `Tài khoản (MSSV): ${sv.mssv}\nMật khẩu mới: ${pw}\n\n(Quy tắc: Ngày sinh ddMMyyyy hoặc 'student123')`,
          confirmText: 'Đã sao lưu / Hoàn tất',
          type: 'success',
        });
      },
      error: (err) => {
        this.toastService.showHttpError(err, 'Không thể đặt lại mật khẩu');
      },
    });
  }
}
