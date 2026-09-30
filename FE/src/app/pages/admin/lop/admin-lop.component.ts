import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';

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

  protected lops: LopItem[] = [];
  protected khoas: KhoaOption[] = [];

  protected searchText = '';
  protected filterKhoaId = '';

  protected editingId: number | null = null;
  protected loading = false;
  protected saving = false;
  protected errorMessage = '';
  protected successMessage = '';

  protected readonly form = this.fb.group({
    khoaId: ['', Validators.required],
    nienKhoa: ['2022-2026', [Validators.required, Validators.maxLength(20)]],
    maLop: [''],
    tenLop: ['', [Validators.required, Validators.maxLength(150)]],
    siSoToiDa: [45, [Validators.required, Validators.min(1)]],
    active: [true],
  });

  ngOnInit(): void {
    this.loadOptions();
    this.loadLops();

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

  protected loadLops(): void {
    this.loading = true;
    this.errorMessage = '';
    this.http.get<LopItem[]>('http://localhost:8080/api/lop').subscribe({
      next: (res) => {
        this.lops = res;
        this.loading = false;
        this.cd.detectChanges();
      },
      error: () => {
        this.errorMessage = 'Không thể tải danh sách lớp học.';
        this.loading = false;
        this.cd.detectChanges();
      },
    });
  }

  protected get filteredLops(): LopItem[] {
    const q = this.searchText.trim().toLowerCase();
    return this.lops.filter((item) => {
      const matchSearch =
        !q ||
        item.maLop.toLowerCase().includes(q) ||
        item.tenLop.toLowerCase().includes(q);
      const matchKhoa = !this.filterKhoaId || String(item.khoaId) === this.filterKhoaId;
      return matchSearch && matchKhoa;
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
    return Math.ceil(this.filteredLops.length / this.pageSize) || 1;
  }

  protected get startIndex(): number {
    return (this.currentPage - 1) * this.pageSize;
  }

  protected get endIndex(): number {
    return Math.min(this.startIndex + this.pageSize, this.filteredLops.length);
  }

  protected get paginatedLops(): LopItem[] {
    const start = this.startIndex;
    return this.filteredLops.slice(start, start + this.pageSize);
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
        this.successMessage = this.editingId === null ? 'Thêm mới lớp học thành công!' : 'Cập nhật lớp học thành công!';
        this.loadLops();
        this.resetForm();
      },
      error: (err) => {
        this.saving = false;
        this.errorMessage = err?.error?.message || 'Không thể lưu lớp học. Vui lòng kiểm tra mã lớp đã tồn tại chưa.';
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

  protected remove(item: LopItem): void {
    if (!confirm(`Bạn có chắc muốn xóa lớp ${item.tenLop} (${item.maLop})?`)) {
      return;
    }

    this.http.delete(`http://localhost:8080/api/lop/${item.id}`).subscribe({
      next: () => {
        this.lops = this.lops.filter((x) => x.id !== item.id);
        this.successMessage = `Đã xóa lớp ${item.maLop} thành công.`;
        this.cd.detectChanges();
      },
      error: () => {
        this.errorMessage = 'Không thể xóa lớp học này (có thể đã có sinh viên thuộc lớp).';
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
