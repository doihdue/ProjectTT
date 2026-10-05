import { CommonModule } from '@angular/common';
import { Component, HostListener, inject } from '@angular/core';
import { ConfirmDialogService } from '../../services/confirm-dialog.service';

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './confirm-dialog.component.html',
  styleUrl: './confirm-dialog.component.scss',
})
export class ConfirmDialogComponent {
  protected readonly dialogService = inject(ConfirmDialogService);
  protected readonly dialog = this.dialogService.activeDialog;

  @HostListener('document:keydown.escape')
  onEscape(): void {
    if (this.dialog()) {
      this.dialogService.handleCancel();
    }
  }

  @HostListener('document:keydown.enter')
  onEnter(): void {
    if (this.dialog()) {
      this.dialogService.handleConfirm();
    }
  }

  protected onBackdropClick(event: MouseEvent): void {
    if ((event.target as HTMLElement).classList.contains('dialog-backdrop')) {
      this.dialogService.handleCancel();
    }
  }
}
