import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, ElementRef, HostListener, inject, OnDestroy, OnInit } from '@angular/core';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter, Subscription } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { NotificationItem, NotificationService } from '../services/notification.service';

type NavItem = {
  label: string;
  path: string;
  icon?: string;
};

type NavGroup = {
  title: string;
  items: NavItem[];
};

@Component({
  selector: 'app-shell',
  imports: [CommonModule, RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './shell.component.html',
  styleUrl: './shell.component.scss',
})
export class ShellComponent implements OnInit, OnDestroy {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly notificationService = inject(NotificationService);
  private readonly cd = inject(ChangeDetectorRef);
  private readonly elementRef = inject(ElementRef);

  protected notifications: NotificationItem[] = [];
  protected unreadCount = 0;
  protected showNotifications = false;
  protected loadingNotifications = false;
  protected mobileSidebarOpen = false;

  private pollTimer: any = null;
  private routerSub: Subscription | null = null;

  ngOnInit(): void {
    // Tải trước số lượng và danh sách thông báo để khi click vào chuông là có dữ liệu ngay lập tức
    this.refreshUnreadCount();
    this.loadNotifications();

    this.pollTimer = setInterval(() => {
      this.refreshUnreadCount();
      if (this.showNotifications) {
        this.loadNotifications();
      }
    }, 15000);

    // Tự động đóng menu trên mobile khi chuyển trang
    this.routerSub = this.router.events
      .pipe(filter((event) => event instanceof NavigationEnd))
      .subscribe(() => {
        this.mobileSidebarOpen = false;
      });
  }

  ngOnDestroy(): void {
    if (this.pollTimer) {
      clearInterval(this.pollTimer);
    }
    if (this.routerSub) {
      this.routerSub.unsubscribe();
    }
  }

  @HostListener('document:click', ['$event'])
  protected onDocumentClick(event: MouseEvent): void {
    if (!this.showNotifications) return;
    const target = event.target as HTMLElement;
    const container = this.elementRef.nativeElement.querySelector('.notification-container');
    if (container && !container.contains(target)) {
      this.showNotifications = false;
      this.cd.markForCheck();
    }
  }

  protected toggleSidebar(): void {
    this.mobileSidebarOpen = !this.mobileSidebarOpen;
  }

  protected closeSidebar(): void {
    this.mobileSidebarOpen = false;
  }

  protected refreshUnreadCount(): void {
    this.notificationService.getUnreadCount().subscribe({
      next: (res) => {
        this.unreadCount = res.unreadCount;
        this.cd.markForCheck();
      },
      error: () => {},
    });
  }

  protected toggleNotifications(): void {
    this.showNotifications = !this.showNotifications;
    if (this.showNotifications) {
      this.loadNotifications();
    }
    this.cd.markForCheck();
  }

  protected loadNotifications(): void {
    this.loadingNotifications = true;
    this.notificationService.getMyNotifications().subscribe({
      next: (list) => {
        this.notifications = list;
        this.unreadCount = list.filter((n) => !n.daDoc).length;
        this.loadingNotifications = false;
        this.cd.markForCheck();
      },
      error: () => {
        this.loadingNotifications = false;
        this.cd.markForCheck();
      },
    });
  }

  protected markAllRead(): void {
    this.notificationService.markAllAsRead().subscribe({
      next: () => {
        this.notifications = this.notifications.map((n) => ({ ...n, daDoc: true }));
        this.unreadCount = 0;
        this.cd.markForCheck();
      },
      error: () => {},
    });
  }

  protected handleNotificationClick(item: NotificationItem): void {
    if (!item.daDoc) {
      this.notificationService.markAsRead(item.id).subscribe({
        next: () => {
          item.daDoc = true;
          this.unreadCount = Math.max(0, this.unreadCount - 1);
          this.cd.markForCheck();
        },
      });
    }
    this.showNotifications = false;
    this.cd.markForCheck();
    if (item.lienKet) {
      void this.router.navigate([item.lienKet]);
    }
  }

  private readonly adminNavGroups: NavGroup[] = [
    {
      title: 'Tổng quan',
      items: [{ label: 'Bảng điều khiển', path: '/dashboard', icon: '📊' }],
    },
    {
      title: 'Quản lý Cơ sở & Sinh viên',
      items: [
        { label: 'Quản lý Khoa', path: '/quan-ly/khoa', icon: '🏛️' },
        { label: 'Quản lý Lớp hành chính', path: '/quan-ly/lop', icon: '🏫' },
        { label: 'Quản lý Sinh viên', path: '/quan-ly/sinh-vien', icon: '👨‍🎓' },
      ],
    },
    {
      title: 'Học vụ & Rèn luyện',
      items: [{ label: 'Phê duyệt Điểm rèn luyện', path: '/quan-ly/diem-ren-luyen', icon: '⚖️' }],
    },
  ];

  private readonly studentNavGroups: NavGroup[] = [
    {
      title: 'Tổng quan',
      items: [{ label: 'Trang chủ', path: '/dashboard', icon: '🏠' }],
    },
    {
      title: 'Cổng Sinh viên',
      items: [
        { label: 'Đánh giá Điểm rèn luyện', path: '/sinh-vien/danh-gia-drl', icon: '📝' },
        { label: 'Kết quả rèn luyện', path: '/sinh-vien/ket-qua-drl', icon: '🏆' },
        { label: 'Hồ sơ cá nhân', path: '/sinh-vien/ho-so-ca-nhan', icon: '👤' },
      ],
    },
  ];

  protected get navGroups(): NavGroup[] {
    if (this.authService.isStudent) return this.studentNavGroups;
    return this.adminNavGroups;
  }

  protected get currentUserName(): string {
    const fullName = this.authService.currentUser?.fullName;
    if (!fullName || fullName.includes('?')) {
      if (this.authService.isStudent) return 'Sinh viên';
      if (this.authService.isLecturer) return 'Giảng viên';
      return 'Quản trị hệ thống';
    }
    return fullName;
  }

  protected get currentUserRole(): string {
    const role = this.authService.currentUser?.role;
    if (role === 'STUDENT') return 'Sinh viên';
    if (role === 'LECTURER') return 'Giảng viên';
    return 'Quản trị viên';
  }

  protected get userInitials(): string {
    const name = this.currentUserName;
    if (!name || name === 'Quản trị hệ thống') return this.authService.isStudent ? 'SV' : 'QT';
    const parts = name.trim().split(/\s+/);
    if (parts.length === 1) return parts[0].substring(0, 2).toUpperCase();
    return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
  }

  protected logout(): void {
    this.authService.logout();
    void this.router.navigate(['/login']);
  }
}
