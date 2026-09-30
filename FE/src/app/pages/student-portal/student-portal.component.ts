import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, DestroyRef, inject, OnInit } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import {
  DiemRenLuyenItem,
  DiemRenLuyenService,
  DiemRenLuyenSubmitRequest,
  DotDanhGiaItem,
} from '../../services/diem-ren-luyen.service';
import {
  ChangePasswordPayload,
  StudentProfile,
  StudentProfileUpdate,
  StudentService,
} from '../../services/student.service';

export type PortalMode = 'evaluation' | 'results' | 'profile';

@Component({
  selector: 'app-student-portal',
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterLink],
  templateUrl: './student-portal.component.html',
  styleUrl: './student-portal.component.scss',
})
export class StudentPortalComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);
  private readonly studentService = inject(StudentService);
  private readonly drlService = inject(DiemRenLuyenService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly cd = inject(ChangeDetectorRef);

  protected mode: PortalMode = 'evaluation';

  // Navigation tabs for student portal
  protected readonly navTabs = [
    {
      key: 'evaluation' as PortalMode,
      label: 'Đánh giá Điểm rèn luyện',
      path: '/sinh-vien/danh-gia-drl',
      icon: 'M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2',
    },
    {
      key: 'results' as PortalMode,
      label: 'Kết quả & Lịch sử rèn luyện',
      path: '/sinh-vien/ket-qua-drl',
      icon: 'M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z',
    },
    {
      key: 'profile' as PortalMode,
      label: 'Hồ sơ cá nhân',
      path: '/sinh-vien/ho-so-ca-nhan',
      icon: 'M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z',
    },
  ];

  // Student Profile
  protected profile: StudentProfile | null = null;

  // Quản lý Đợt đánh giá ĐRL
  protected openDots: DotDanhGiaItem[] = [];
  protected selectedDot: DotDanhGiaItem | null = null;
  protected loadingDots = false;
  protected currentDotExistingDrl: DiemRenLuyenItem | null = null;

  // DRL Form
  protected readonly drlForm = this.fb.group({
    dotDanhGiaId: [null as number | null, Validators.required],
    hocKy: [1, Validators.required],
    namHoc: ['2026-2027', Validators.required],
    tieuChi1Sv: [18, [Validators.required, Validators.min(0), Validators.max(20)]],
    tieuChi2Sv: [23, [Validators.required, Validators.min(0), Validators.max(25)]],
    tieuChi3Sv: [16, [Validators.required, Validators.min(0), Validators.max(20)]],
    tieuChi4Sv: [22, [Validators.required, Validators.min(0), Validators.max(25)]],
    tieuChi5Sv: [8, [Validators.required, Validators.min(0), Validators.max(10)]],
    ghiChuSinhVien: [''],
    minhChung: [''],
  });

  // DRL History
  protected myDrlList: DiemRenLuyenItem[] = [];
  protected submittingDrl = false;

  // Pagination & Filtering for Student DRL History
  protected historyCurrentPage = 1;
  protected historyPageSize = 3;
  protected historyStatusFilter: 'ALL' | 'CHO_DUYET' | 'DA_DUYET' | 'TU_CHOI' = 'ALL';

  protected countByStatus(status: 'CHO_DUYET' | 'DA_DUYET' | 'TU_CHOI'): number {
    return this.myDrlList.filter((item) => item.trangThai === status).length;
  }

  protected get filteredDrlList(): DiemRenLuyenItem[] {
    if (this.historyStatusFilter === 'ALL') {
      return this.myDrlList;
    }
    return this.myDrlList.filter((item) => item.trangThai === this.historyStatusFilter);
  }

  protected get historyTotalPages(): number {
    return Math.max(1, Math.ceil(this.filteredDrlList.length / this.historyPageSize));
  }

  protected get pagedDrlList(): DiemRenLuyenItem[] {
    const startIndex = (this.historyCurrentPage - 1) * this.historyPageSize;
    return this.filteredDrlList.slice(startIndex, startIndex + this.historyPageSize);
  }

  protected get historyStartIndex(): number {
    if (this.filteredDrlList.length === 0) return 0;
    return (this.historyCurrentPage - 1) * this.historyPageSize + 1;
  }

  protected get historyEndIndex(): number {
    return Math.min(this.historyCurrentPage * this.historyPageSize, this.filteredDrlList.length);
  }

  protected get historyPages(): number[] {
    const total = this.historyTotalPages;
    const current = this.historyCurrentPage;
    const pages: number[] = [];

    let start = Math.max(1, current - 2);
    let end = Math.min(total, current + 2);

    if (end - start < 4) {
      if (start === 1) {
        end = Math.min(total, start + 4);
      } else if (end === total) {
        start = Math.max(1, end - 4);
      }
    }

    for (let i = start; i <= end; i++) {
      pages.push(i);
    }
    return pages;
  }

  protected changeHistoryPage(page: number): void {
    if (page >= 1 && page <= this.historyTotalPages) {
      this.historyCurrentPage = page;
      this.cd.markForCheck();
    }
  }

  protected onHistoryPageSizeChange(event: Event): void {
    const select = event.target as HTMLSelectElement;
    this.historyPageSize = Number(select.value);
    this.historyCurrentPage = 1;
    this.cd.markForCheck();
  }

  protected setHistoryStatusFilter(filter: 'ALL' | 'CHO_DUYET' | 'DA_DUYET' | 'TU_CHOI'): void {
    this.historyStatusFilter = filter;
    this.historyCurrentPage = 1;
    this.cd.markForCheck();
  }

  // Profile forms
  protected readonly profileForm = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.pattern(/^[^\s@]+@[^\s@]+$/)]],
    soDienThoai: ['', [Validators.pattern(/^[0-9+() -]{8,20}$/)]],
    diaChi: ['', [Validators.maxLength(255)]],
  });

  protected readonly passwordForm = this.fb.nonNullable.group({
    oldPassword: ['', [Validators.required, Validators.minLength(6)]],
    newPassword: ['', [Validators.required, Validators.minLength(6)]],
    confirmPassword: ['', [Validators.required, Validators.minLength(6)]],
  });

  // UI state
  protected feedbackMessage: { type: 'success' | 'error'; text: string } | null = null;
  protected contactFeedback: { type: 'success' | 'error'; text: string } | null = null;
  protected savingProfile = false;
  protected changingPassword = false;
  protected loadingHistory = false;

  ngOnInit(): void {
    this.route.data.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((data) => {
      const mode = (data['mode'] as PortalMode) || 'evaluation';
      this.mode = mode;
      this.feedbackMessage = null;
    });

    this.loadProfile();
    this.loadDrlHistory();
    this.loadOpenDots();
  }

  protected get studentInitials(): string {
    const name = this.profile?.hoTen;
    if (!name) return 'SV';
    const parts = name.trim().split(/\s+/);
    if (parts.length === 1) return parts[0].substring(0, 2).toUpperCase();
    return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
  }

  protected loadProfile(): void {
    this.studentService.getProfile().subscribe({
      next: (prof) => {
        this.profile = prof;
        this.profileForm.patchValue({
          email: prof.email || '',
          soDienThoai: prof.soDienThoai || '',
          diaChi: prof.diaChi || '',
        });
        this.cd.markForCheck();
      },
    });
  }

  protected loadOpenDots(): void {
    this.loadingDots = true;
    this.drlService.getOpenDots().subscribe({
      next: (dots) => {
        this.openDots = dots;
        this.loadingDots = false;
        if (dots.length > 0) {
          this.selectDot(dots[0]);
        } else {
          this.selectedDot = null;
        }
        this.cd.markForCheck();
      },
      error: () => {
        this.loadingDots = false;
        this.cd.markForCheck();
      },
    });
  }

  protected onDotSelectChange(event: Event): void {
    const target = event.target as HTMLSelectElement;
    const dotId = Number(target.value);
    const found = this.openDots.find((d) => d.id === dotId);
    if (found) {
      this.selectDot(found);
    }
  }

  protected selectDot(dot: DotDanhGiaItem): void {
    this.selectedDot = dot;
    this.drlForm.patchValue({
      dotDanhGiaId: dot.id,
      hocKy: dot.hocKy,
      namHoc: dot.namHoc,
    });

    this.checkExistingDrlForDot(dot);
  }

  protected checkExistingDrlForDot(dot: DotDanhGiaItem): void {
    const existing = this.myDrlList.find(
      (d) => d.dotDanhGiaId === dot.id || (d.hocKy === dot.hocKy && d.namHoc === dot.namHoc)
    );
    this.currentDotExistingDrl = existing || null;

    if (existing) {
      this.drlForm.patchValue({
        tieuChi1Sv: existing.tieuChi1Sv,
        tieuChi2Sv: existing.tieuChi2Sv,
        tieuChi3Sv: existing.tieuChi3Sv,
        tieuChi4Sv: existing.tieuChi4Sv,
        tieuChi5Sv: existing.tieuChi5Sv,
        ghiChuSinhVien: existing.ghiChuSinhVien || '',
        minhChung: existing.minhChung || '',
      });
    }
    this.cd.markForCheck();
  }

  protected loadDrlHistory(): void {
    this.loadingHistory = true;
    this.drlService.getMyDrlList().subscribe({
      next: (list) => {
        this.myDrlList = list;
        this.historyCurrentPage = 1;
        this.loadingHistory = false;
        if (this.selectedDot) {
          this.checkExistingDrlForDot(this.selectedDot);
        }
        this.cd.markForCheck();
      },
      error: () => {
        this.loadingHistory = false;
        this.cd.markForCheck();
      },
    });
  }

  protected get currentSelfTotal(): number {
    const v = this.drlForm.value;
    return (
      Number(v.tieuChi1Sv || 0) +
      Number(v.tieuChi2Sv || 0) +
      Number(v.tieuChi3Sv || 0) +
      Number(v.tieuChi4Sv || 0) +
      Number(v.tieuChi5Sv || 0)
    );
  }

  protected get currentSelfClassification(): string {
    const total = this.currentSelfTotal;
    if (total >= 90) return 'Xuất sắc';
    if (total >= 80) return 'Tốt';
    if (total >= 65) return 'Khá';
    if (total >= 50) return 'Trung bình';
    if (total >= 35) return 'Yếu';
    return 'Kém';
  }

  protected formatDotTitle(drl: any): string {
    if (!drl) return '';
    if (drl.tenDot) {
      if (drl.hocKy && drl.tenDot.includes('Học kỳ') && !drl.tenDot.includes(`Học kỳ ${drl.hocKy}`)) {
        return drl.tenDot.replace(/Học kỳ\s*\d+/, `Học kỳ ${drl.hocKy}`);
      }
      return drl.tenDot;
    }
    return `Đánh giá ĐRL Học kỳ ${drl.hocKy || 1} (${drl.namHoc || ''})`;
  }

  protected submitDrl(): void {
    if (!this.selectedDot) {
      this.feedbackMessage = {
        type: 'error',
        text: 'Hiện tại Nhà trường chưa mở đợt đánh giá rèn luyện nào hoặc các đợt đã đóng.',
      };
      return;
    }

    if (this.currentDotExistingDrl && this.currentDotExistingDrl.trangThai === 'DA_DUYET') {
      this.feedbackMessage = {
        type: 'error',
        text: `Phiếu rèn luyện của đợt "${this.selectedDot.tenDot}" đã được Ban Quản trị phê duyệt chính thức. Bạn không thể nộp lại!`,
      };
      return;
    }

    if (this.drlForm.invalid) {
      this.drlForm.markAllAsTouched();
      this.feedbackMessage = {
        type: 'error',
        text: 'Vui lòng kiểm tra lại điểm các tiêu chí (phải nằm trong khoảng quy định).',
      };
      return;
    }

    const val = this.drlForm.getRawValue();
    const payload: DiemRenLuyenSubmitRequest = {
      dotDanhGiaId: this.selectedDot.id,
      hocKy: this.selectedDot.hocKy,
      namHoc: this.selectedDot.namHoc,
      tieuChi1Sv: Number(val.tieuChi1Sv),
      tieuChi2Sv: Number(val.tieuChi2Sv),
      tieuChi3Sv: Number(val.tieuChi3Sv),
      tieuChi4Sv: Number(val.tieuChi4Sv),
      tieuChi5Sv: Number(val.tieuChi5Sv),
      ghiChuSinhVien: val.ghiChuSinhVien || '',
      minhChung: val.minhChung || '',
    };

    this.submittingDrl = true;
    this.feedbackMessage = null;

    this.drlService.submitDrl(payload).subscribe({
      next: (res) => {
        this.submittingDrl = false;
        this.feedbackMessage = {
          type: 'success',
          text: `Đã nộp phiếu đánh giá ĐRL "${this.selectedDot?.tenDot || `Học kỳ ${res.hocKy}`}" thành công! Phiếu đã được chuyển đến Ban Quản trị chờ xét duyệt.`,
        };
        this.loadDrlHistory();
        this.cd.markForCheck();
      },
      error: (err) => {
        this.submittingDrl = false;
        this.feedbackMessage = {
          type: 'error',
          text: 'Nộp phiếu thất bại: ' + (err?.error?.message || err.message),
        };
        this.cd.markForCheck();
      },
    });
  }

  protected editRejectedDrl(item: DiemRenLuyenItem): void {
    void this.router.navigate(['/sinh-vien/danh-gia-drl']);

    // Tìm đợt tương ứng nếu có
    const matchingDot = this.openDots.find((d) => d.id === item.dotDanhGiaId);
    if (matchingDot) {
      this.selectDot(matchingDot);
    } else {
      this.drlForm.patchValue({
        dotDanhGiaId: item.dotDanhGiaId || null,
        hocKy: item.hocKy,
        namHoc: item.namHoc,
        tieuChi1Sv: item.tieuChi1Sv,
        tieuChi2Sv: item.tieuChi2Sv,
        tieuChi3Sv: item.tieuChi3Sv,
        tieuChi4Sv: item.tieuChi4Sv,
        tieuChi5Sv: item.tieuChi5Sv,
        ghiChuSinhVien: item.ghiChuSinhVien || '',
        minhChung: item.minhChung || '',
      });
    }

    this.feedbackMessage = {
      type: 'success',
      text: `Đã tải thông tin phiếu "${item.tenDot || `Học kỳ ${item.hocKy} (${item.namHoc})`}". Vui lòng cập nhật và nộp lại.`,
    };
  }

  protected saveProfile(): void {
    this.contactFeedback = null;
    if (this.profileForm.invalid) {
      this.profileForm.markAllAsTouched();
      this.contactFeedback = {
        type: 'error',
        text: 'Vui lòng kiểm tra lại thông tin: Email không được để trống và phải đúng định dạng; Số điện thoại từ 8-20 số.',
      };
      this.feedbackMessage = this.contactFeedback;
      this.cd.markForCheck();
      return;
    }

    const payload: StudentProfileUpdate = this.profileForm.getRawValue();
    this.savingProfile = true;
    this.studentService.updateProfile(payload).subscribe({
      next: (prof) => {
        this.profile = prof;
        this.profileForm.patchValue({
          email: prof.email || '',
          soDienThoai: prof.soDienThoai || '',
          diaChi: prof.diaChi || '',
        });
        this.savingProfile = false;
        this.contactFeedback = {
          type: 'success',
          text: 'Cập nhật thông tin liên hệ và địa chỉ thường trú thành công!',
        };
        this.feedbackMessage = this.contactFeedback;
        this.cd.markForCheck();
      },
      error: (err) => {
        this.savingProfile = false;
        this.contactFeedback = {
          type: 'error',
          text: 'Không thể cập nhật hồ sơ: ' + (err?.error?.message || err.message || 'Lỗi hệ thống'),
        };
        this.feedbackMessage = this.contactFeedback;
        this.cd.markForCheck();
      },
    });
  }

  protected changePassword(): void {
    if (this.passwordForm.invalid) {
      this.passwordForm.markAllAsTouched();
      return;
    }

    const { oldPassword, newPassword, confirmPassword } = this.passwordForm.getRawValue();
    if (newPassword !== confirmPassword) {
      this.feedbackMessage = {
        type: 'error',
        text: 'Mật khẩu mới và xác nhận mật khẩu không khớp!',
      };
      return;
    }

    const payload: ChangePasswordPayload = { oldPassword, newPassword };
    this.changingPassword = true;
    this.studentService.changePassword(payload).subscribe({
      next: (res) => {
        this.changingPassword = false;
        this.passwordForm.reset();
        this.feedbackMessage = {
          type: 'success',
          text: res.message || 'Đổi mật khẩu thành công!',
        };
        this.cd.markForCheck();
      },
      error: (err) => {
        this.changingPassword = false;
        this.feedbackMessage = {
          type: 'error',
          text: 'Đổi mật khẩu thất bại: ' + (err?.error?.message || 'Mật khẩu cũ không đúng.'),
        };
        this.cd.markForCheck();
      },
    });
  }

  protected printPage(): void {
    window.print();
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
        return 'Đang chờ duyệt';
      case 'DA_DUYET':
        return 'Đã phê duyệt';
      case 'TU_CHOI':
        return 'Cần sửa đổi';
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

  // --- XUẤT BÁO CÁO PHIẾU ĐRL PDF (JASPER REPORT) ---
  protected exportingPdfId: number | null = null;

  protected exportPhieuPdf(item: DiemRenLuyenItem): void {
    if (!item?.id) return;
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
          const filename = `Phieu_DRL_${item.mssv || 'SV'}_HK${item.hocKy}_${item.namHoc}.pdf`;
          this.drlService.downloadBlob(blob, filename);
          this.feedbackMessage = {
            type: 'success',
            text: `Đã tải phiếu kết quả điểm rèn luyện Học kỳ ${item.hocKy} (${item.namHoc}) thành công!`,
          };
          this.cd.detectChanges();
          setTimeout(() => {
            this.feedbackMessage = null;
            this.cd.detectChanges();
          }, 4000);
        },
        error: (err) => {
          this.feedbackMessage = {
            type: 'error',
            text: 'Không thể tải phiếu ĐRL PDF: ' + (err?.error?.message || err.message),
          };
          this.cd.detectChanges();
        },
      });
  }
}