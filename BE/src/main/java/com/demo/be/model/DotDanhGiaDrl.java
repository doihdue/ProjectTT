package com.demo.be.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "dot_danh_gia_drl")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DotDanhGiaDrl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150, columnDefinition = "NVARCHAR(150)")
    private String tenDot; // VD: "Đánh giá ĐRL Học kỳ 1 (2024 - 2025)"

    @Column(nullable = false)
    private Integer hocKy; // 1, 2, 3

    @Column(nullable = false, length = 20)
    private String namHoc; // VD: "2024-2025"

    private LocalDate ngayBatDau;

    private LocalDate ngayKetThuc;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String trangThai = "DANG_MO"; // "DANG_MO", "DONG"

    @Column(length = 1000, columnDefinition = "NVARCHAR(1000)")
    private String ghiChu;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime thoiGianTao = LocalDateTime.now();
}
