import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import {
  DiemRenLuyenDuyetRequest,
  DiemRenLuyenItem,
  DiemRenLuyenService,
  DotDanhGiaItem,
  DotDanhGiaRequest,
  DrlFilterParams,
  DrlThongKe,
} from '../../../services/diem-ren-luyen.service';

export type KhoaOption = {
  id: number;
  maKhoa: string;
  tenKhoa: string;
};

export type LopOption = {
  id: number;
  maLop: string;
  tenLop: string;
  khoaId?: number;
};

import { ToastService } from '../../../services/toast.service';
import { ConfirmDialogService } from '../../../services/confirm-dialog.service';

@Component({
  selector: 'app-admin-drl',
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './admin-drl.component.html',
  styleUrl: './admin-drl.component.scss',
})
export class AdminDrlComponent implements OnInit {
  private readonly drlService = inject(DiemRenLuyenService);
  private readonly http = inject(HttpClient);
  private readonly fb = inject(FormBuilder);
  private readonly cd = inject(ChangeDetectorRef);
  private readonly toastService = inject(ToastService);
  private readonly confirmDialog = inject(ConfirmDialogService);

  // Active Tab
  protected activeTab: 'drl' | 'dot' = 'drl';

  // Data lists
  protected drlList: DiemRenLuyenItem[] = [];
  protected dotList: DotDanhGiaItem[] = [];
  protected khoas: KhoaOption[] = [];
  protected lops: LopOption[] = [];
  protected filteredLops: LopOption[] = [];

  protected thongKe: DrlThongKe = {
    tongSoPhieu: 0,
    soChoDuyet: 0,
    soDaDuyet: 0,
    soTuChoi: 0,
    phanBoXepLoai: {},
  };

  // Bộ lọc phiếu ĐRL
  protected filterDotId = '';
  protected filterTrangThai = '';
  protected filterHocKy = '';
  protected filterNamHoc = '';
  protected filterKhoaId = '';
  protected filterLopId = '';
  protected filterKeyword = '';

  protected loading = false;
  protected saving = false;
  protected errorMessage = '';
  protected successMessage = '';

  // Modal duyệt phiếu ĐRL
  protected showModal = false;
  protected selectedDrl: DiemRenLuyenItem | null = null;

  protected readonly duyetForm = this.fb.group({
    tieuChi1Admin: [20, [Validators.required, Validators.min(0), Validators.max(20)]],
    tieuChi2Admin: [25, [Validators.required, Validators.min(0), Validators.max(25)]],
    tieuChi3Admin: [20, [Validators.required, Validators.min(0), Validators.max(20)]],
    tieuChi4Admin: [25, [Validators.required, Validators.min(0), Validators.max(25)]],
    tieuChi5Admin: [10, [Validators.required, Validators.min(0), Validators.max(10)]],
    nhanXetAdmin: [''],
  });

  // Modal Đợt đánh giá
  protected showDotModal = false;
  protected selectedDot: DotDanhGiaItem | null = null;
  protected savingDot = false;

  protected readonly dotForm = this.fb.group({
    tenDot: ['', [Validators.required, Validators.maxLength(150)]],
    hocKy: [1, [Validators.required, Validators.min(1), Validators.max(3)]],
    namHoc: ['2026-2027', [Validators.required, Validators.pattern(/^\d{4}-\d{4}$/)]],
    ngayBatDau: ['', [Validators.required]],
    ngayKetThuc: ['', [Validators.required]],
    trangThai: ['DANG_MO', [Validators.required]],
    ghiChu: [''],
  });

  protected get isEndDateInvalid(): boolean {
    const s = this.dotForm.get('ngayBatDau')?.value;
    const e = this.dotForm.get('ngayKetThuc')?.value;
    if (s && e) {
      return new Date(e) <= new Date(s);
    }
    return false;
  }

  protected get isNamHocInvalid(): boolean {
    const nh = (this.dotForm.get('namHoc')?.value || '').trim();
    if (!nh) return false;
    const match = nh.match(/^(\d{4})-(\d{4})$/);
    if (!match) return true;
    const startYear = parseInt(match[1], 10);
    const endYear = parseInt(match[2], 10);
    return endYear !== startYear + 1;
  }

  ngOnInit(): void {
    this.loadDots();
    this.loadFilterOptions();
    this.loadDrlList();
    this.loadThongKe();

    this.dotForm.get('hocKy')?.valueChanges.subscribe(() => this.autoUpdateTenDot());
    this.dotForm.get('namHoc')?.valueChanges.subscribe(() => this.autoUpdateTenDot());
  }

  protected autoUpdateTenDot(): void {
    if (this.selectedDot) return;
    const hk = this.dotForm.get('hocKy')?.value || 1;
    const nh = (this.dotForm.get('namHoc')?.value || '2026-2027').trim();
    this.dotForm.patchValue({ tenDot: `Đánh giá ĐRL Học kỳ ${hk} (${nh})` }, { emitEvent: false });
  }

  protected regenerateTenDot(): void {
    const hk = this.dotForm.get('hocKy')?.value || 1;
    const nh = (this.dotForm.get('namHoc')?.value || '2026-2027').trim();
    this.dotForm.patchValue({ tenDot: `Đánh giá ĐRL Học kỳ ${hk} (${nh})` });
  }

  // --- QUẢN LÝ ĐỢT ĐÁNH GIÁ ---
  protected loadDots(): void {
    this.drlService.getAllDots().subscribe({
      next: (dots) => {
        this.dotList = dots;
        this.cd.markForCheck();
      },
    });
  }

  protected openCreateDotModal(): void {
    this.selectedDot = null;
    this.errorMessage = '';

    let nextHk = 1;
    let nextNamHoc = '2026-2027';
    if (this.dotList && this.dotList.length > 0) {
      const latest = this.dotList[0];
      if (latest.hocKy === 1) {
        nextHk = 2;
        nextNamHoc = latest.namHoc || '2026-2027';
      } else if (latest.hocKy === 2) {
        nextHk = 3;
        nextNamHoc = latest.namHoc || '2026-2027';
      } else {
        nextHk = 1;
        const match = (latest.namHoc || '').match(/^(\d{4})-(\d{4})$/);
        if (match) {
          nextNamHoc = `${Number(match[1]) + 1}-${Number(match[2]) + 1}`;
        }
      }
    }

    const today = new Date();
    const defaultEndDate = new Date();
    defaultEndDate.setDate(today.getDate() + 14);

    this.dotForm.reset({
      tenDot: `Đánh giá ĐRL Học kỳ ${nextHk} (${nextNamHoc})`,
      hocKy: nextHk,
      namHoc: nextNamHoc,
      ngayBatDau: today.toISOString().split('T')[0],
      ngayKetThuc: defaultEndDate.toISOString().split('T')[0],
      trangThai: 'DANG_MO',
      ghiChu: '',
    });
    this.showDotModal = true;
  }

  protected openEditDotModal(dot: DotDanhGiaItem): void {
    this.selectedDot = dot;
    this.errorMessage = '';
    this.dotForm.patchValue({
      tenDot: dot.tenDot,
      hocKy: dot.hocKy,
      namHoc: dot.namHoc,
      ngayBatDau: dot.ngayBatDau || '',
      ngayKetThuc: dot.ngayKetThuc || '',
      trangThai: dot.trangThai,
      ghiChu: dot.ghiChu || '',
    });
    this.showDotModal = true;
  }

  protected closeDotModal(): void {
    this.showDotModal = false;
    this.selectedDot = null;
  }

  protected saveDot(): void {
    const isValid = this.toastService.validateForm(
      this.dotForm,
      {
        tenDot: 'Tên đợt đánh giá',
        hocKy: 'Học kỳ',
        namHoc: 'Năm học (định dạng YYYY-YYYY)',
        ngayBatDau: 'Thời gian bắt đầu mở đợt',
        ngayKetThuc: 'Hạn chót kết thúc đợt',
        trangThai: 'Trạng thái đợt',
      },
      'Vui lòng điền đúng thông tin Đợt đánh giá'
    );

    if (!isValid) return;

    const val = this.dotForm.getRawValue();

    // 1. Kiểm tra logic năm học (Năm sau = Năm trước + 1)
    const nh = (val.namHoc || '').trim();
    const nhMatch = nh.match(/^(\d{4})-(\d{4})$/);
    if (!nhMatch) {
      this.toastService.warning(
        'Năm học không đúng định dạng',
        'Năm học phải theo đúng định dạng YYYY-YYYY (ví dụ: 2026-2027).'
      );
      return;
    }
    const startYear = parseInt(nhMatch[1], 10);
    const endYear = parseInt(nhMatch[2], 10);
    if (endYear !== startYear + 1) {
      this.toastService.warning(
        'Năm học không hợp lệ',
        `Năm học "${nh}" không hợp lệ: Năm kết thúc (${endYear}) phải sau năm bắt đầu (${startYear}) đúng 1 năm (ví dụ: ${startYear}-${startYear + 1}).`
      );
      return;
    }

    // 2. Kiểm tra thời gian kết thúc luôn phải sau thời gian bắt đầu
    if (!val.ngayBatDau) {
      this.toastService.warning('Thiếu thời gian bắt đầu', 'Vui lòng chọn ngày bắt đầu mở đợt đánh giá.');
      return;
    }
    if (!val.ngayKetThuc) {
      this.toastService.warning('Thiếu thời gian kết thúc', 'Vui lòng chọn hạn chót kết thúc đợt đánh giá.');
      return;
    }

    const startDate = new Date(val.ngayBatDau);
    const endDate = new Date(val.ngayKetThuc);
    if (endDate <= startDate) {
      this.toastService.warning(
        'Thời gian kết thúc không hợp lệ',
        `Thời gian kết thúc (${val.ngayKetThuc}) luôn phải sau thời gian bắt đầu (${val.ngayBatDau}) ít nhất 1 ngày.`
      );
      return;
    }

    const payload: DotDanhGiaRequest = {
      tenDot: val.tenDot || '',
      hocKy: Number(val.hocKy),
      namHoc: nh,
      ngayBatDau: val.ngayBatDau,
      ngayKetThuc: val.ngayKetThuc,
      trangThai: (val.trangThai as 'DANG_MO' | 'DONG') || 'DANG_MO',
      ghiChu: val.ghiChu || undefined,
    };

    this.savingDot = true;
    this.errorMessage = '';

    if (this.selectedDot) {
      this.drlService.updateDot(this.selectedDot.id, payload).subscribe({
        next: (res) => {
          this.savingDot = false;
          this.closeDotModal();
          const msg = `Đã cập nhật đợt đánh giá "${res.tenDot}" thành công!`;
          this.successMessage = msg;
          this.toastService.success('Cập nhật thành công', msg);
          this.loadDots();
          setTimeout(() => (this.successMessage = ''), 4000);
        },
        error: (err) => {
          this.savingDot = false;
          this.errorMessage = 'Cập nhật đợt thất bại: ' + (err?.error?.message || err.message);
          this.toastService.showHttpError(err, 'Cập nhật đợt đánh giá thất bại');
        },
      });
    } else {
      this.drlService.createDot(payload).subscribe({
        next: (res) => {
          this.savingDot = false;
          this.closeDotModal();
          const msg = `Đã tạo đợt đánh giá "${res.tenDot}" thành công! Sinh viên hiện có thể thực hiện đánh giá.`;
          this.successMessage = msg;
          this.toastService.success('Tạo đợt thành công', msg);
          this.loadDots();
          setTimeout(() => (this.successMessage = ''), 4000);
        },
        error: (err) => {
          this.savingDot = false;
          this.errorMessage = 'Tạo đợt đánh giá thất bại: ' + (err?.error?.message || err.message);
          this.toastService.showHttpError(err, 'Tạo đợt đánh giá thất bại');
        },
      });
    }
  }

  protected async toggleDotStatus(dot: DotDanhGiaItem): Promise<void> {
    const actionText = dot.trangThai === 'DANG_MO' ? 'ĐÓNG' : 'MỞ';
    const confirmed = await this.confirmDialog.confirm({
      title: `${actionText} đợt đánh giá`,
      message: `Bạn có chắc muốn ${actionText} đợt đánh giá "${dot.tenDot}"?`,
      detailMessage:
        dot.trangThai === 'DANG_MO'
          ? 'Khi ĐÓNG đợt, sinh viên sẽ không thể nộp phiếu tự đánh giá rèn luyện cho đợt này nữa.'
          : 'Khi MỞ đợt, tất cả sinh viên thuộc diện đánh giá có thể truy cập và nộp phiếu.',
      confirmText: `Xác nhận ${actionText}`,
      cancelText: 'Hủy bỏ',
      type: dot.trangThai === 'DANG_MO' ? 'warning' : 'info',
    });

    if (!confirmed) {
      return;
    }

    this.drlService.toggleDotStatus(dot.id).subscribe({
      next: (res) => {
        const msg = res.trangThai === 'DANG_MO'
          ? `Đã MỞ đợt "${res.tenDot}". Sinh viên có thể nộp đánh giá rèn luyện!`
          : `Đã ĐÓNG đợt "${res.tenDot}". Sinh viên sẽ không thể nộp đánh giá cho đợt này.`;
        this.successMessage = msg;
        this.toastService.success('Thành công', msg);
        this.loadDots();
        setTimeout(() => (this.successMessage = ''), 4000);
      },
      error: (err) => {
        this.errorMessage = 'Không thể thay đổi trạng thái đợt: ' + (err?.error?.message || err.message);
        this.toastService.showHttpError(err, 'Lỗi cập nhật trạng thái đợt');
      },
    });
  }

  protected async deleteDot(dot: DotDanhGiaItem): Promise<void> {
    if (dot.soLuongPhieu > 0) {
      await this.confirmDialog.alert({
        title: 'Không thể xóa đợt đánh giá',
        message: `Đợt đánh giá "${dot.tenDot}" hiện đã có ${dot.soLuongPhieu} sinh viên nộp phiếu rèn luyện.`,
        detailMessage: 'Hệ thống không cho phép xóa đợt đã phát sinh dữ liệu đánh giá của sinh viên. Nếu cần ngừng nhận phiếu, bạn vui lòng chuyển sang trạng thái "Đóng đợt".',
        confirmText: 'Đã hiểu',
        type: 'warning',
      });
      return;
    }

    const confirmed = await this.confirmDialog.confirm({
      title: 'Xác nhận xóa đợt đánh giá',
      message: `Bạn có chắc chắn muốn xóa vĩnh viễn đợt đánh giá "${dot.tenDot}"?`,
      detailMessage: 'Thao tác này sẽ xóa hoàn toàn đợt đánh giá khỏi danh mục của hệ thống.',
      confirmText: 'Xác nhận xóa',
      cancelText: 'Hủy bỏ',
      type: 'danger',
    });

    if (!confirmed) {
      return;
    }

    this.drlService.deleteDot(dot.id).subscribe({
      next: () => {
        const msg = `Đã xóa đợt đánh giá "${dot.tenDot}" thành công!`;
        this.successMessage = msg;
        this.toastService.success('Đã xóa thành công', msg);
        this.loadDots();
        setTimeout(() => (this.successMessage = ''), 4000);
      },
      error: (err) => {
        this.errorMessage = 'Xóa đợt thất bại: ' + (err?.error?.message || err.message);
        this.toastService.showHttpError(err, 'Xóa đợt đánh giá thất bại');
      },
    });
  }

  // --- QUẢN LÝ PHIẾU ĐIỂM RÈN LUYỆN ---
  protected loadFilterOptions(): void {
    this.http.get<KhoaOption[]>('http://localhost:8080/api/khoa').subscribe({
      next: (res) => {
        this.khoas = res;
      },
    });

    this.http.get<LopOption[]>('http://localhost:8080/api/lop').subscribe({
      next: (res) => {
        this.lops = res;
        this.filteredLops = res;
      },
    });
  }

  protected onKhoaChange(): void {
    if (!this.filterKhoaId) {
      this.filteredLops = this.lops;
    } else {
      const kId = Number(this.filterKhoaId);
      this.filteredLops = this.lops.filter((l) => l.khoaId === kId);
    }
    this.filterLopId = '';
    this.loadDrlList();
  }

  protected loadThongKe(): void {
    this.drlService.getThongKe().subscribe({
      next: (res) => {
        this.thongKe = res;
        this.cd.markForCheck();
      },
    });
  }

  // --- PHÂN TRANG (PAGINATION) ---
  protected currentPage = 1;
  protected pageSize = 10;

  protected get totalPages(): number {
    return Math.ceil(this.drlList.length / this.pageSize) || 1;
  }

  protected get startIndex(): number {
    return (this.currentPage - 1) * this.pageSize;
  }

  protected get endIndex(): number {
    return Math.min(this.startIndex + this.pageSize, this.drlList.length);
  }

  protected get paginatedDrlList(): DiemRenLuyenItem[] {
    const start = this.startIndex;
    return this.drlList.slice(start, start + this.pageSize);
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

  protected loadDrlList(): void {
    this.loading = true;
    this.errorMessage = '';
    this.currentPage = 1;

    const params: DrlFilterParams = {};
    if (this.filterDotId) params.dotDanhGiaId = Number(this.filterDotId);
    if (this.filterTrangThai) params.trangThai = this.filterTrangThai;
    if (this.filterHocKy) params.hocKy = Number(this.filterHocKy);
    if (this.filterNamHoc) params.namHoc = this.filterNamHoc;
    if (this.filterKhoaId) params.khoaId = Number(this.filterKhoaId);
    if (this.filterLopId) params.lopId = Number(this.filterLopId);
    if (this.filterKeyword.trim()) params.keyword = this.filterKeyword.trim();

    this.drlService.getAllForAdmin(params).subscribe({
      next: (list) => {
        this.drlList = list;
        this.loading = false;
        this.cd.markForCheck();
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = 'Không thể tải danh sách phiếu ĐRL: ' + (err?.error?.message || err.message);
        this.cd.markForCheck();
      },
    });
  }

  protected openDuyetModal(item: DiemRenLuyenItem): void {
    this.selectedDrl = item;
    this.errorMessage = '';
    this.successMessage = '';

    this.duyetForm.patchValue({
      tieuChi1Admin: item.tieuChi1Admin != null ? item.tieuChi1Admin : item.tieuChi1Sv,
      tieuChi2Admin: item.tieuChi2Admin != null ? item.tieuChi2Admin : item.tieuChi2Sv,
      tieuChi3Admin: item.tieuChi3Admin != null ? item.tieuChi3Admin : item.tieuChi3Sv,
      tieuChi4Admin: item.tieuChi4Admin != null ? item.tieuChi4Admin : item.tieuChi4Sv,
      tieuChi5Admin: item.tieuChi5Admin != null ? item.tieuChi5Admin : item.tieuChi5Sv,
      nhanXetAdmin: item.nhanXetAdmin || '',
    });

    this.showModal = true;
  }

  protected closeModal(): void {
    this.showModal = false;
    this.selectedDrl = null;
  }

  protected copyStudentScores(): void {
    if (!this.selectedDrl) return;
    this.duyetForm.patchValue({
      tieuChi1Admin: this.selectedDrl.tieuChi1Sv,
      tieuChi2Admin: this.selectedDrl.tieuChi2Sv,
      tieuChi3Admin: this.selectedDrl.tieuChi3Sv,
      tieuChi4Admin: this.selectedDrl.tieuChi4Sv,
      tieuChi5Admin: this.selectedDrl.tieuChi5Sv,
    });
  }

  protected get currentAdminTotal(): number {
    const v = this.duyetForm.value;
    return (
      Number(v.tieuChi1Admin || 0) +
      Number(v.tieuChi2Admin || 0) +
      Number(v.tieuChi3Admin || 0) +
      Number(v.tieuChi4Admin || 0) +
      Number(v.tieuChi5Admin || 0)
    );
  }

  protected get currentAdminClassification(): string {
    const total = this.currentAdminTotal;
    if (total >= 90) return 'Xuất sắc';
    if (total >= 80) return 'Tốt';
    if (total >= 65) return 'Khá';
    if (total >= 50) return 'Trung bình';
    if (total >= 35) return 'Yếu';
    return 'Kém';
  }

  protected submitDuyet(chapNhan: boolean): void {
    if (!this.selectedDrl) return;

    if (chapNhan) {
      const isValid = this.toastService.validateForm(
        this.duyetForm,
        {
          tieuChi1Admin: 'Tiêu chí 1 (Ý thức học tập: 0-20)',
          tieuChi2Admin: 'Tiêu chí 2 (Kỷ luật & quy chế: 0-25)',
          tieuChi3Admin: 'Tiêu chí 3 (Hoạt động chính trị, XH: 0-20)',
          tieuChi4Admin: 'Tiêu chí 4 (Phẩm chất công dân: 0-25)',
          tieuChi5Admin: 'Tiêu chí 5 (Cán bộ lớp/đoàn thể: 0-10)',
        },
        'Điểm phê duyệt chưa hợp lệ'
      );
      if (!isValid) return;
    }

    const formVal = this.duyetForm.getRawValue();

    const payload: DiemRenLuyenDuyetRequest = {
      chapNhan,
      tieuChi1Admin: Number(formVal.tieuChi1Admin),
      tieuChi2Admin: Number(formVal.tieuChi2Admin),
      tieuChi3Admin: Number(formVal.tieuChi3Admin),
      tieuChi4Admin: Number(formVal.tieuChi4Admin),
      tieuChi5Admin: Number(formVal.tieuChi5Admin),
      nhanXetAdmin: formVal.nhanXetAdmin || (chapNhan ? 'Đã phê duyệt' : 'Cần bổ sung minh chứng'),
    };

    this.saving = true;
    this.errorMessage = '';

    this.drlService.duyetDrl(this.selectedDrl.id, payload).subscribe({
      next: (res) => {
        this.saving = false;
        this.closeModal();
        const msg = chapNhan
          ? `Đã phê duyệt ĐRL cho sinh viên ${res.hoTen} (${res.mssv}) thành công: ${res.tongDiemAdmin} điểm (${res.xepLoai})!`
          : `Đã từ chối phiếu ĐRL của sinh viên ${res.hoTen} (${res.mssv}). Thông báo đã gửi về sinh viên!`;
        this.successMessage = msg;
        this.toastService.success(chapNhan ? 'Phê duyệt thành công' : 'Đã từ chối phiếu', msg);
        this.loadDrlList();
        this.loadThongKe();
        setTimeout(() => (this.successMessage = ''), 5000);
      },
      error: (err) => {
        this.saving = false;
        this.errorMessage = 'Thao tác phê duyệt thất bại: ' + (err?.error?.message || err.message);
        this.toastService.showHttpError(err, 'Thao tác phê duyệt thất bại');
      },
    });
  }

  protected async deleteDrl(item: DiemRenLuyenItem): Promise<void> {
    const confirmed = await this.confirmDialog.confirm({
      title: 'Xác nhận xóa phiếu ĐRL',
      message: `Bạn có chắc muốn xóa phiếu điểm rèn luyện ${item.tenDot || `Học kỳ ${item.hocKy}`} của sinh viên "${item.hoTen}" (${item.mssv})?`,
      detailMessage: 'Dữ liệu đánh giá của học kỳ này sẽ bị hủy bỏ vĩnh viễn khỏi danh sách thống kê.',
      confirmText: 'Xác nhận xóa',
      cancelText: 'Hủy bỏ',
      type: 'danger',
    });

    if (!confirmed) {
      return;
    }

    this.drlService.delete(item.id).subscribe({
      next: () => {
        const msg = 'Đã xóa phiếu điểm rèn luyện thành công!';
        this.successMessage = msg;
        this.toastService.success('Đã xóa thành công', msg);
        this.loadDrlList();
        this.loadThongKe();
        setTimeout(() => (this.successMessage = ''), 4000);
      },
      error: (err) => {
        this.errorMessage = 'Không thể xóa phiếu: ' + (err?.error?.message || err.message);
        this.toastService.showHttpError(err, 'Xóa phiếu ĐRL thất bại');
      },
    });
  }

  protected getBadgeClass(trangThai: string): string {
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

  protected getTrangThaiText(trangThai: string): string {
    switch (trangThai) {
      case 'CHO_DUYET':
        return 'Chờ phê duyệt';
      case 'DA_DUYET':
        return 'Đã phê duyệt';
      case 'TU_CHOI':
        return 'Bị từ chối';
      default:
        return trangThai;
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

  // --- XUẤT BÁO CÁO PDF (JASPER REPORT) ---
  protected exportingPdfId: number | null = null;
  protected exportingSummary = false;

  protected exportPhieuPdf(item: DiemRenLuyenItem): void {
    if (item?.id == null) return;
    this.exportingPdfId = item.id;
    this.cd.detectChanges();

    this.drlService
      .exportPhieuPdf(item.id)
      .pipe(
        finalize(() => {
          this.exportingPdfId = null;
          this.cd.detectChanges();
        })
      )
      .subscribe({
        next: (blob) => {
          const filename = `Phieu_DRL_${item.mssv || item.id}_HK${item.hocKy || ''}.pdf`;
          this.drlService.downloadBlob(blob, filename);
          this.successMessage = `Đã xuất phiếu ĐRL cho sinh viên ${item.hoTen || item.mssv} thành công!`;
          this.cd.detectChanges();
          setTimeout(() => {
            this.successMessage = '';
            this.cd.detectChanges();
          }, 4000);
        },
        error: (err) => {
          this.errorMessage = 'Không thể xuất báo cáo PDF: ' + (err?.error?.message || err.message);
          this.cd.detectChanges();
        },
      });
  }

  protected exportSummaryPdf(): void {
    const filters: DrlFilterParams = {};
    if (this.filterDotId) filters.dotDanhGiaId = Number(this.filterDotId);
    if (this.filterTrangThai) filters.trangThai = this.filterTrangThai;
    if (this.filterKhoaId) filters.khoaId = Number(this.filterKhoaId);
    if (this.filterLopId) filters.lopId = Number(this.filterLopId);
    if (this.filterKeyword) filters.keyword = this.filterKeyword.trim();

    this.exportingSummary = true;
    this.cd.detectChanges();

    this.drlService
      .exportSummaryPdf(filters)
      .pipe(
        finalize(() => {
          this.exportingSummary = false;
          this.cd.detectChanges();
        })
      )
      .subscribe({
        next: (blob) => {
          const dateStr = new Date().toISOString().slice(0, 10);
          const filename = `Bang_Tong_Hop_DRL_${dateStr}.pdf`;
          this.drlService.downloadBlob(blob, filename);
          this.successMessage = 'Đã xuất Bảng tổng hợp điểm rèn luyện PDF thành công!';
          this.cd.detectChanges();
          setTimeout(() => {
            this.successMessage = '';
            this.cd.detectChanges();
          }, 4000);
        },
        error: (err) => {
          this.errorMessage = 'Không thể xuất bảng tổng hợp PDF: ' + (err?.error?.message || err.message);
          this.cd.detectChanges();
        },
      });
  }
}
