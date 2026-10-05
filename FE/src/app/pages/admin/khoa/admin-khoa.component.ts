import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { Khoa, KhoaPayload, KhoaService } from '../../../services/khoa.service';
import { ToastService } from '../../../services/toast.service';
import { ConfirmDialogService } from '../../../services/confirm-dialog.service';

@Component({
  selector: 'app-admin-khoa',
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './admin-khoa.component.html',
  styleUrl: './admin-khoa.component.scss',
})
export class AdminKhoaComponent {
  private readonly fb = inject(FormBuilder);
  private readonly khoaService = inject(KhoaService);
  private readonly changeDetector = inject(ChangeDetectorRef);
  private readonly toastService = inject(ToastService);
  private readonly confirmDialog = inject(ConfirmDialogService);

  protected readonly form = this.fb.nonNullable.group({
    maKhoa: ['', [Validators.required, Validators.maxLength(20)]],
    tenKhoa: ['', [Validators.required, Validators.maxLength(150)]],
    moTa: ['', [Validators.maxLength(500)]],
    active: [true],
  });

  protected khoas: Khoa[] = [];
  protected searchText = '';
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

  constructor() {
    this.searchSubject
      .pipe(debounceTime(350), distinctUntilChanged())
      .subscribe(() => {
        this.currentPage = 1;
        this.loadKhoas();
      });

    this.loadKhoas();
  }

  /**
   * Tải danh sách khoa theo tìm kiếm & phân trang trực tiếp từ Backend EntityManager
   */
  protected loadKhoas(): void {
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

    this.khoaService.search(params).subscribe({
      next: (res) => {
        this.khoas = res?.content || [];
        this.totalElements = res?.totalElements || 0;
        this.totalPages = res?.totalPages || 1;
        this.loading = false;
        this.changeDetector.detectChanges();
      },
      error: () => {
        this.errorMessage = 'Không thể tải danh sách khoa. Hãy kiểm tra BE đang chạy.';
        this.loading = false;
        this.changeDetector.detectChanges();
      },
    });
  }

  protected onSearchInput(val: string): void {
    this.searchText = val;
    this.searchSubject.next(val);
  }

  protected onFilterChange(): void {
    this.currentPage = 1;
    this.loadKhoas();
  }

  protected onPageSizeChange(): void {
    this.currentPage = 1;
    this.loadKhoas();
  }

  protected get filteredKhoas(): Khoa[] {
    return this.khoas;
  }

  protected get paginatedKhoas(): Khoa[] {
    return this.khoas;
  }

  protected get startIndex(): number {
    return (this.currentPage - 1) * this.pageSize;
  }

  protected get endIndex(): number {
    return Math.min(this.startIndex + this.khoas.length, this.totalElements);
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
      this.loadKhoas();
    }
  }

  protected submit(): void {
    const isValid = this.toastService.validateForm(
      this.form,
      {
        maKhoa: 'Mã khoa',
        tenKhoa: 'Tên khoa',
        moTa: 'Mô tả',
      },
      'Vui lòng điền đúng thông tin Khoa'
    );

    if (!isValid) {
      this.errorMessage = 'Vui lòng kiểm tra lại các trường thông tin bắt buộc (được đánh dấu đỏ).';
      return;
    }

    const payload: KhoaPayload = this.form.getRawValue();
    const request = this.editingId === null
      ? this.khoaService.create(payload)
      : this.khoaService.update(this.editingId, payload);

    this.saving = true;
    this.errorMessage = '';
    this.successMessage = '';
    request.subscribe({
      next: (savedKhoa) => {
        this.saving = false;
        const msg = this.editingId === null ? 'Thêm mới Khoa thành công!' : 'Cập nhật Khoa thành công!';
        this.successMessage = msg;
        this.toastService.success(
          this.editingId === null ? 'Thêm mới thành công' : 'Cập nhật thành công',
          msg
        );
        this.khoas = this.editingId === null
          ? [...this.khoas, savedKhoa]
          : this.khoas.map((khoa) => (khoa.id === savedKhoa.id ? savedKhoa : khoa));
        this.resetForm();
        this.changeDetector.detectChanges();
      },
      error: (err) => {
        this.saving = false;
        this.errorMessage = err?.error?.message || 'Không thể lưu khoa. Vui lòng kiểm tra lại thông tin.';
        this.toastService.showHttpError(err, 'Lưu thông tin Khoa thất bại', this.form);
        this.changeDetector.detectChanges();
      },
    });
  }

  protected edit(khoa: Khoa): void {
    this.editingId = khoa.id;
    this.form.setValue({
      maKhoa: khoa.maKhoa,
      tenKhoa: khoa.tenKhoa,
      moTa: khoa.moTa ?? '',
      active: khoa.active,
    });
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  protected async remove(khoa: Khoa): Promise<void> {
    const confirmed = await this.confirmDialog.confirm({
      title: 'Xác nhận xóa Khoa',
      message: `Bạn có chắc chắn muốn xóa khoa "${khoa.tenKhoa}" (Mã: ${khoa.maKhoa})?`,
      detailMessage: 'Lưu ý: Không thể xóa khoa nếu đang có các lớp hành chính hoặc học phần trực thuộc đơn vị này.',
      confirmText: 'Xác nhận xóa',
      cancelText: 'Hủy bỏ',
      type: 'danger',
    });

    if (!confirmed) {
      return;
    }

    this.khoaService.delete(khoa.id).subscribe({
      next: () => {
        const msg = `Đã xóa khoa ${khoa.maKhoa} (${khoa.tenKhoa}) thành công.`;
        this.successMessage = msg;
        this.toastService.success('Đã xóa thành công', msg);
        if (this.khoas.length === 1 && this.currentPage > 1) {
          this.currentPage--;
        }
        this.loadKhoas();
        this.changeDetector.detectChanges();
      },
      error: (err) => {
        this.errorMessage = 'Không thể xóa khoa này (có thể có lớp hoặc môn học đang trực thuộc khoa).';
        this.toastService.showHttpError(err, 'Không thể xóa khoa');
        this.changeDetector.detectChanges();
      },
    });
  }

  protected resetForm(): void {
    this.editingId = null;
    this.form.reset({ maKhoa: '', tenKhoa: '', moTa: '', active: true });
  }
}

export { AdminKhoaComponent as KhoaComponent };
