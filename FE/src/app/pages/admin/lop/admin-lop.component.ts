import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, FormsModule, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { ToastService } from '../../../services/toast.service';
import { ConfirmDialogService } from '../../../services/confirm-dialog.service';
import { PageResponse } from '../sinh-vien/admin-sinh-vien.component';

export type LopItem = {
  id: number;
  maLop: string;
  tenLop: string;
  nienKhoa: string;
  siSoToiDa: number;
  khoaId: number;
  khoaTen?: string;
  active: boolean;
};

export type KhoaOption = {
  id: number;
  maKhoa: string;
  tenKhoa: string;
};

function nienKhoaValidator(control: AbstractControl): ValidationErrors | null {
  const value = control.value;
  if (!value) return null;
  const trimmed = String(value).trim();
  const match = trimmed.match(/^(\d{4})\s*[-–—]\s*(\d{4})$/);
  if (!match) {
    return { invalidFormat: true };
  }
  const start = parseInt(match[1], 10);
  const end = parseInt(match[2], 10);
  if (start >= end) {
    return { invalidYearRange: { startYear: start, endYear: end } };
  }
  if (end - start > 10) {
    return { invalidDuration: true };
  }
  return null;
}

@Component({
  selector: 'app-admin-lop',
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './admin-lop.component.html',
  styleUrl: './admin-lop.component.scss',
})
export class AdminLopComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly fb = inject(FormBuilder);
  private readonly cd = inject(ChangeDetectorRef);
  private readonly toastService = inject(ToastService);
  private readonly confirmDialog = inject(ConfirmDialogService);

  protected lops: LopItem[] = [];
  protected khoas: KhoaOption[] = [];

  protected searchText = '';
  protected filterKhoaId = '';

  protected editingId: number | null = null;
  protected loading = false;
  protected saving = false;
  protected errorMessage = '';
  protected successMessage = '';

  // Server-side pagination & search via EntityManager
  protected currentPage = 1;
  protected pageSize = 10;
  protected totalElements = 0;
  protected totalPages = 1;

  private readonly searchSubject = new Subject<string>();

  protected readonly form = this.fb.group({
    khoaId: ['', Validators.required],
    nienKhoa: ['2022-2026', [Validators.required, Validators.maxLength(20), nienKhoaValidator]],
    maLop: [''],
    tenLop: ['', [Validators.required, Validators.maxLength(150)]],
    siSoToiDa: [45, [Validators.required, Validators.min(1)]],
    active: [true],
  });

  ngOnInit(): void {
    this.loadOptions();
    this.loadLops();

    // Debounce tìm kiếm 350ms
    this.searchSubject
      .pipe(debounceTime(350), distinctUntilChanged())
      .subscribe(() => {
        this.currentPage = 1;
        this.loadLops();
      });

    this.form.get('khoaId')?.valueChanges.subscribe(() => {
      if (this.editingId === null) {
        this.autoFillMaLop();
      }
    });

    this.form.get('nienKhoa')?.valueChanges.subscribe(() => {
      if (this.editingId === null && this.form.get('khoaId')?.value) {
        this.autoFillMaLop();
      }
    });
  }

  protected autoFillMaLop(): void {
    const khoaId = this.form.get('khoaId')?.value;
    const nienKhoa = this.form.get('nienKhoa')?.value || '';
    if (!khoaId) return;

    this.http
      .get<{ maLop: string }>('http://localhost:8080/api/lop/next-ma-lop', {
        params: {
          khoaId: String(khoaId),
          nienKhoa: nienKhoa,
        },
      })
      .subscribe({
        next: (res) => {
          if (res?.maLop) {
            this.form.patchValue({ maLop: res.maLop });
            this.cd.detectChanges();
          }
        },
      });
  }

  protected loadOptions(): void {
    this.http.get<KhoaOption[]>('http://localhost:8080/api/khoa').subscribe({
      next: (res) => {
        this.khoas = res;
        this.cd.detectChanges();
      },
    });
  }

  /**
   * Tải danh sách lớp học theo tìm kiếm & phân trang trực tiếp từ Backend EntityManager
   */
  protected loadLops(): void {
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
    if (this.filterKhoaId) {
      params['khoaId'] = this.filterKhoaId;
    }

    this.http
      .get<PageResponse<LopItem>>('http://localhost:8080/api/lop/search', { params })
      .subscribe({
        next: (res) => {
          this.lops = res?.content || [];
          this.totalElements = res?.totalElements || 0;
          this.totalPages = res?.totalPages || 1;
          this.loading = false;
          this.cd.detectChanges();
        },
        error: () => {
          this.errorMessage = 'Không thể tải danh sách lớp học từ máy chủ.';
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
    this.loadLops();
  }

  protected onPageSizeChange(): void {
    this.currentPage = 1;
    this.loadLops();
  }

  protected get filteredLops(): LopItem[] {
    return this.lops;
  }

  protected get paginatedLops(): LopItem[] {
    return this.lops;
  }

  protected get startIndex(): number {
    return (this.currentPage - 1) * this.pageSize;
  }

  protected get endIndex(): number {
    return Math.min(this.startIndex + this.lops.length, this.totalElements);
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
      this.loadLops();
    }
  }

  protected submit(): void {
    const isValid = this.toastService.validateForm(
      this.form,
      {
        khoaId: 'Khoa trực thuộc',
        tenLop: 'Tên lớp',
        nienKhoa: 'Niên khóa',
        siSoToiDa: 'Sĩ số tối đa',
      },
      'Vui lòng điền đúng thông tin Lớp học'
    );

    if (!isValid) {
      this.errorMessage = 'Vui lòng kiểm tra lại các trường thông tin bắt buộc (được đánh dấu đỏ).';
      return;
    }

    const val = this.form.getRawValue();
    const payload = {
      maLop: val.maLop?.trim(),
      tenLop: val.tenLop?.trim(),
      nienKhoa: val.nienKhoa?.trim(),
      siSoToiDa: Number(val.siSoToiDa),
      khoaId: Number(val.khoaId),
      active: val.active ?? true,
    };

    this.saving = true;
    this.errorMessage = '';
    this.successMessage = '';

    const req = this.editingId === null
      ? this.http.post<LopItem>('http://localhost:8080/api/lop', payload)
      : this.http.put<LopItem>(`http://localhost:8080/api/lop/${this.editingId}`, payload);

    req.subscribe({
      next: (saved) => {
        this.saving = false;
        const msg = this.editingId === null ? 'Thêm mới lớp học thành công!' : 'Cập nhật lớp học thành công!';
        this.successMessage = msg;
        this.toastService.success(
          this.editingId === null ? 'Thêm mới thành công' : 'Cập nhật thành công',
          msg
        );
        this.loadLops();
        this.resetForm();
      },
      error: (err) => {
        this.saving = false;
        this.errorMessage = err?.error?.message || 'Không thể lưu lớp học. Vui lòng kiểm tra lại thông tin.';
        this.toastService.showHttpError(err, 'Lưu thông tin Lớp thất bại', this.form);
        this.cd.detectChanges();
      },
    });
  }

  protected edit(item: LopItem): void {
    this.editingId = item.id;
    this.form.setValue({
      maLop: item.maLop,
      tenLop: item.tenLop,
      nienKhoa: item.nienKhoa,
      siSoToiDa: item.siSoToiDa,
      khoaId: String(item.khoaId),
      active: item.active,
    });
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  protected async remove(item: LopItem): Promise<void> {
    const confirmed = await this.confirmDialog.confirm({
      title: 'Xác nhận xóa Lớp học',
      message: `Bạn có chắc chắn muốn xóa lớp "${item.tenLop}" (Mã: ${item.maLop})?`,
      detailMessage: 'Lưu ý: Không thể xóa lớp học nếu đã có sinh viên đăng ký thuộc lớp này.',
      confirmText: 'Xác nhận xóa',
      cancelText: 'Hủy bỏ',
      type: 'danger',
    });

    if (!confirmed) {
      return;
    }

    this.http.delete(`http://localhost:8080/api/lop/${item.id}`).subscribe({
      next: () => {
        const msg = `Đã xóa lớp ${item.maLop} (${item.tenLop}) thành công.`;
        this.successMessage = msg;
        this.toastService.success('Đã xóa thành công', msg);
        if (this.lops.length === 1 && this.currentPage > 1) {
          this.currentPage--;
        }
        this.loadLops();
        this.cd.detectChanges();
      },
      error: (err) => {
        this.errorMessage = 'Không thể xóa lớp học này (có thể đã có sinh viên thuộc lớp).';
        this.toastService.showHttpError(err, 'Không thể xóa lớp học');
        this.cd.detectChanges();
      },
    });
  }

  protected resetForm(): void {
    this.editingId = null;
    this.form.reset({
      khoaId: '',
      nienKhoa: '2022-2026',
      maLop: '',
      tenLop: '',
      siSoToiDa: 45,
      active: true,
    });
  }
}
