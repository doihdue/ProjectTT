import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ToastComponent } from './layout/toast/toast.component';
import { ConfirmDialogComponent } from './layout/confirm-dialog/confirm-dialog.component';

@Component({
  standalone: true,
  imports: [RouterOutlet, ToastComponent, ConfirmDialogComponent],
  selector: 'app-root',
  styleUrl: './app.scss',
  templateUrl: './app.html',
})
export class App {}
