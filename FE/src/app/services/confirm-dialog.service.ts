import { Injectable, signal } from '@angular/core';

export type DialogType = 'danger' | 'warning' | 'info' | 'success';

export interface ConfirmDialogOptions {
  title?: string;
  message: string;
  detailMessage?: string;
  confirmText?: string;
  cancelText?: string;
  type?: DialogType;
  showCancel?: boolean;
}

interface ActiveDialog extends ConfirmDialogOptions {
  resolve: (value: boolean) => void;
}

@Injectable({
  providedIn: 'root',
})
export class ConfirmDialogService {
  private readonly _activeDialog = signal<ActiveDialog | null>(null);
  public readonly activeDialog = this._activeDialog.asReadonly();

  confirm(options: ConfirmDialogOptions): Promise<boolean> {
    return new Promise<boolean>((resolve) => {
      this._activeDialog.set({
        title: options.title || 'Xác nhận hành động',
        message: options.message,
        detailMessage: options.detailMessage,
        confirmText: options.confirmText || 'Đồng ý',
        cancelText: options.cancelText || 'Hủy bỏ',
        type: options.type || 'danger',
        showCancel: options.showCancel !== false,
        resolve,
      });
    });
  }

  alert(options: Omit<ConfirmDialogOptions, 'showCancel'>): Promise<boolean> {
    return this.confirm({
      ...options,
      showCancel: false,
      confirmText: options.confirmText || 'Đã hiểu',
    });
  }

  handleConfirm(): void {
    const current = this._activeDialog();
    if (current) {
      current.resolve(true);
      this._activeDialog.set(null);
    }
  }

  handleCancel(): void {
    const current = this._activeDialog();
    if (current) {
      current.resolve(false);
      this._activeDialog.set(null);
    }
  }
}
