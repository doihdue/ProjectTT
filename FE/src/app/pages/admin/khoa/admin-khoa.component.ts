import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { Khoa, KhoaPayload, KhoaService } from '../../../services/khoa.service';

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

  constructor() {
    this.loadKhoas();
  }

  protected loadKhoas(): void {
    this.loading = true;
    this.errorMessage = '';
    this.khoaService.findAll().subscribe({
      next: (khoas) => {
        this.khoas = khoas;
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

  protected get filteredKhoas(): Khoa[] {
    const q = this.searchText.trim().toLowerCase();
    return this.khoas.filter((k) => {
      return (
        !q ||
        k.maKhoa.toLowerCase().includes(q) ||
        k.tenKhoa.toLowerCase().includes(q) ||
        (k.moTa && k.moTa.toLowerCase().includes(q))
      );
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
    return Math.ceil(this.filteredKhoas.length / this.pageSize) || 1;
  }

  protected get startIndex(): number {
    return (this.currentPage - 1) * this.pageSize;
  }

  protected get endIndex(): number {
    return Math.min(this.startIndex + this.pageSize, this.filteredKhoas.length);
  }

  protected get paginatedKhoas(): Khoa[] {
    const start = this.startIndex;
    return this.filteredKhoas.slice(start, start + this.pageSize);
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
      this.changeDetector.detectChanges();
    }
  }

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
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
        this.successMessage = this.editingId === null ? 'Thêm mới Khoa thành công!' : 'Cập nhật Khoa thành công!';
        this.khoas = this.editingId === null
          ? [...this.khoas, savedKhoa]
          : this.khoas.map((khoa) => (khoa.id === savedKhoa.id ? savedKhoa : khoa));
        this.resetForm();
        this.changeDetector.detectChanges();
      },
      error: (err) => {
        this.saving = false;
        this.errorMessage = err?.error?.message || 'Không thể lưu khoa. Kiểm tra mã khoa đã tồn tại.';
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

  protected remove(khoa: Khoa): void {
    if (!confirm(`Bạn có chắc muốn xóa khoa ${khoa.tenKhoa} (${khoa.maKhoa})?`)) {
      return;
    }

    this.khoaService.delete(khoa.id).subscribe({
      next: () => {
        this.khoas = this.khoas.filter((item) => item.id !== khoa.id);
        this.successMessage = `Đã xóa khoa ${khoa.maKhoa} thành công.`;
        this.changeDetector.detectChanges();
      },
      error: () => {
        this.errorMessage = 'Không thể xóa khoa này (có thể có lớp hoặc môn học đang trực thuộc khoa).';
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
