import { Injectable, signal } from '@angular/core';
import { AbstractControl, FormGroup } from '@angular/forms';

export type ToastType = 'success' | 'error' | 'warning' | 'info';

export interface ToastItem {
  id: string;
  type: ToastType;
  title: string;
  message?: string;
  details?: string[];
  duration: number;
}

@Injectable({
  providedIn: 'root',
})
export class ToastService {
  private readonly _toasts = signal<ToastItem[]>([]);
  public readonly toasts = this._toasts.asReadonly();

  private counter = 0;

  show(
    type: ToastType,
    title: string,
    message?: string,
    details?: string[],
    duration = 5000
  ): string {
    const id = `toast-${Date.now()}-${++this.counter}`;
    const newToast: ToastItem = {
      id,
      type,
      title,
      message,
      details: details && details.length > 0 ? details : undefined,
      duration,
    };

    // Add to toasts (limit max 5 at once)
    this._toasts.update((current) => [newToast, ...current].slice(0, 5));

    if (duration > 0) {
      setTimeout(() => {
        this.dismiss(id);
      }, duration);
    }

    return id;
  }

  success(title: string, message?: string, details?: string[]): string {
    return this.show('success', title, message, details, 4500);
  }

  error(title: string, message?: string, details?: string[]): string {
    return this.show('error', title, message, details, 7000);
  }

  warning(title: string, message?: string, details?: string[]): string {
    return this.show('warning', title, message, details, 6000);
  }

  info(title: string, message?: string, details?: string[]): string {
    return this.show('info', title, message, details, 4500);
  }

  dismiss(id: string): void {
    this._toasts.update((current) => current.filter((t) => t.id !== id));
  }

  clear(): void {
    this._toasts.set([]);
  }

  /**
   * Trích xuất thông tin lỗi từ HTTP response (Spring Boot ApiErrorResponse)
   * và hiển thị thông báo lỗi chi tiết ra Toast.
   */
  showHttpError(err: any, fallbackTitle = 'Thao tác thất bại', form?: FormGroup): void {
    if (!err) {
      this.error('Đã xảy ra lỗi không xác định.');
      return;
    }

    if (err.status === 0) {
      this.error(
        'Lỗi kết nối máy chủ',
        'Không thể kết nối đến máy chủ Backend (Port 8080). Vui lòng kiểm tra lại dịch vụ BE.'
      );
      return;
    }

    const errorBody = err.error;
    let message = '';
    let details: string[] = [];

    if (typeof errorBody === 'string') {
      message = errorBody;
    } else if (errorBody && typeof errorBody === 'object') {
      if (Array.isArray(errorBody.details) && errorBody.details.length > 0) {
        details = errorBody.details.map((d: any) => String(d));
      }
      message = errorBody.message || errorBody.error || '';
    }

    if (!message && details.length === 0) {
      message = err.message || fallbackTitle || 'Yêu cầu không thể hoàn tất.';
    }

    // Tự động đánh dấu gạch đỏ dọc và lưu message lỗi BE cho các trường bị lỗi
    if (form && errorBody && typeof errorBody === 'object') {
      let matchedAnyField = false;

      if (Array.isArray(errorBody.details)) {
        for (const d of errorBody.details) {
          const str = String(d);
          const colonIdx = str.indexOf(':');
          if (colonIdx > 0) {
            const fieldName = str.substring(0, colonIdx).trim();
            const fieldMsg = str.substring(colonIdx + 1).trim();
            const ctrl = form.get(fieldName);
            if (ctrl) {
              ctrl.setErrors({ serverError: fieldMsg || true });
              ctrl.markAsTouched();
              ctrl.markAsDirty();
              matchedAnyField = true;
            }
          }
        }
      }

      // Quét thông điệp chung (message hoặc error) nếu chưa map được hoặc map thêm
      const fullMsg = ((errorBody.message || '') + ' ' + (errorBody.error || '')).toLowerCase();
      const displayMsg = errorBody.message || errorBody.error || 'Dữ liệu không hợp lệ.';

      const checkAndSet = (fieldName: string, keywords: string[]) => {
        const ctrl = form.get(fieldName);
        if (ctrl && (!matchedAnyField || !ctrl.errors?.['serverError'])) {
          if (keywords.some((k) => fullMsg.includes(k))) {
            ctrl.setErrors({ serverError: displayMsg });
            ctrl.markAsTouched();
            ctrl.markAsDirty();
            matchedAnyField = true;
          }
        }
      };

      checkAndSet('maKhoa', ['mã khoa', 'makhoa']);
      checkAndSet('tenKhoa', ['tên khoa', 'tenkhoa']);
      checkAndSet('maLop', ['mã lớp', 'malop']);
      checkAndSet('tenLop', ['tên lớp', 'tenlop']);
      checkAndSet('mssv', ['mssv', 'mã số sinh viên']);
      checkAndSet('hoTen', ['họ và tên', 'họ tên', 'hoten']);
      checkAndSet('ngaySinh', ['ngày sinh', 'ngaysinh']);
      checkAndSet('email', ['email']);
      checkAndSet('soDienThoai', ['số điện thoại', 'sdt', 'sodienthoai']);
      checkAndSet('diaChi', ['địa chỉ', 'diachi']);
      checkAndSet('lopId', ['lớp học', 'lopid', 'lớp hành chính']);
      checkAndSet('khoaId', ['thuộc khoa', 'khoaid', 'đơn vị khoa']);
      checkAndSet('nienKhoa', ['niên khóa', 'nienkhoa']);
      checkAndSet('siSoToiDa', ['sĩ số', 'sisotoida']);
      checkAndSet('moTa', ['mô tả', 'mota']);
    }

    // Chỉ hiển thị message từ BE làm tiêu đề chính
    const title = message || fallbackTitle;

    // Lọc bỏ details nếu trùng lặp hoàn toàn với title
    const displayDetails = details.filter(
      (d) => d !== title && !d.endsWith(': ' + title)
    );

    this.error(
      title,
      undefined,
      displayDetails.length > 0 ? displayDetails : undefined
    );
  }

  /**
   * Kiểm tra tính hợp lệ của Form trước khi gửi lên máy chủ.
   * Nếu form không hợp lệ, tự động markAllAsTouched() và hiển thị thông báo cảnh báo
   * liệt kê rõ các trường bắt buộc đang bị bỏ trống hoặc sai quy cách.
   * Trả về true nếu hợp lệ, false nếu không hợp lệ.
   */
  validateForm(
    form: FormGroup,
    fieldLabels: Record<string, string> = {},
    title = 'Dữ liệu không hợp lệ!'
  ): boolean {
    if (form.valid) {
      return true;
    }

    form.markAllAsTouched();

    const missingErrors: string[] = [];

    const inspectControls = (group: FormGroup, prefix = '') => {
      Object.keys(group.controls).forEach((key) => {
        const control: AbstractControl | null = group.get(key);
        if (!control) return;

        if (control instanceof FormGroup) {
          inspectControls(control, `${prefix}${key}.`);
        } else if (control.invalid) {
          const fieldKey = `${prefix}${key}`;
          const label = fieldLabels[fieldKey] || fieldLabels[key] || key;
          const errors = control.errors;

          if (errors) {
            if (errors['required'] || errors['mustBeTrue']) {
              missingErrors.push(`"${label}" bắt buộc phải điền/chọn.`);
            } else if (errors['minlength']) {
              missingErrors.push(
                `"${label}" phải có ít nhất ${errors['minlength'].requiredLength} ký tự.`
              );
            } else if (errors['maxlength']) {
              missingErrors.push(
                `"${label}" không được vượt quá ${errors['maxlength'].requiredLength} ký tự.`
              );
            } else if (errors['min']) {
              missingErrors.push(
                `"${label}" tối thiểu phải từ ${errors['min'].min}.`
              );
            } else if (errors['max']) {
              missingErrors.push(
                `"${label}" tối đa không quá ${errors['max'].max}.`
              );
            } else if (errors['invalidYearRange']) {
              missingErrors.push(
                `"${label}": Năm bắt đầu (${errors['invalidYearRange'].startYear}) phải nhỏ hơn năm kết thúc (${errors['invalidYearRange'].endYear}).`
              );
            } else if (errors['invalidFormat']) {
              missingErrors.push(`"${label}" phải có định dạng YYYY-YYYY (ví dụ: 2022-2026).`);
            } else if (errors['invalidDuration']) {
              missingErrors.push(`"${label}" thời gian niên khóa không vượt quá 10 năm.`);
            } else if (errors['email'] || errors['pattern']) {
              missingErrors.push(`"${label}" sai định dạng quy định.`);
            } else {
              missingErrors.push(`"${label}" giá trị không hợp lệ.`);
            }
          }
        }
      });
    };

    inspectControls(form);

    const cleanTitle = title.replace(/^\[FE\s*[-–]?\s*Client\]\s*/i, '').replace(/^\[FE\]\s*/i, '');
    this.warning(
      cleanTitle,
      undefined,
      missingErrors.length > 0 ? missingErrors : undefined
    );

    return false;
  }
}
