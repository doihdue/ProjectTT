package com.demo.be.model;

import com.demo.be.dto.thongke.ThongKeKhoaResponse;
import com.demo.be.dto.thongke.ThongKeXepLoaiResponse;
import jakarta.persistence.Column;
import jakarta.persistence.ColumnResult;
import jakarta.persistence.ConstructorResult;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SqlResultSetMapping;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "diem_ren_luyen")
@SqlResultSetMapping(
        name = "ThongKeKhoaMapping",
        classes = @ConstructorResult(
                targetClass = ThongKeKhoaResponse.class,
                columns = {
                        @ColumnResult(name = "khoaId", type = Long.class),
                        @ColumnResult(name = "maKhoa", type = String.class),
                        @ColumnResult(name = "tenKhoa", type = String.class),
                        @ColumnResult(name = "tongSinhVien", type = Long.class),
                        @ColumnResult(name = "soPhieuDaNop", type = Long.class),
                        @ColumnResult(name = "soPhieuDaDuyet", type = Long.class),
                        @ColumnResult(name = "diemTrungBinh", type = Double.class),
                        @ColumnResult(name = "diemCaoNhat", type = Integer.class)
                }
        )
)
@SqlResultSetMapping(
        name = "ThongKeXepLoaiMapping",
        classes = @ConstructorResult(
                targetClass = ThongKeXepLoaiResponse.class,
                columns = {
                        @ColumnResult(name = "xepLoai", type = String.class),
                        @ColumnResult(name = "soLuong", type = Long.class)
                }
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiemRenLuyen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sinh_vien_id", nullable = false)
    private SinhVien sinhVien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dot_danh_gia_id")
    private DotDanhGiaDrl dotDanhGia;

    @Column(nullable = false)
    private Integer hocKy; // 1, 2, 3

    @Column(nullable = false, length = 20)
    private String namHoc; // vd: "2024-2025"

    // 5 Tiêu chuẩn sinh viên tự chấm (Bộ GD&ĐT - Thang 100)
    @Column(nullable = false)
    private Integer tieuChi1Sv; // Max 20: Đánh giá ý thức học tập

    @Column(nullable = false)
    private Integer tieuChi2Sv; // Max 25: Đánh giá ý thức chấp hành nội quy, quy chế

    @Column(nullable = false)
    private Integer tieuChi3Sv; // Max 20: Đánh giá ý thức tham gia hoạt động phong trào, tình nguyện, văn thể mỹ

    @Column(nullable = false)
    private Integer tieuChi4Sv; // Max 25: Đánh giá phẩm chất công dân và quan hệ cộng đồng

    @Column(nullable = false)
    private Integer tieuChi5Sv; // Max 10: Đánh giá ý thức và kết quả phụ trách lớp, đoàn thể

    @Column(nullable = false)
    private Integer tongDiemSv; // Tổng điểm tự chấm (max 100)

    // 5 Tiêu chuẩn Admin / Hội đồng duyệt
    private Integer tieuChi1Admin;
    private Integer tieuChi2Admin;
    private Integer tieuChi3Admin;
    private Integer tieuChi4Admin;
    private Integer tieuChi5Admin;
    private Integer tongDiemAdmin;

    @Column(length = 50, columnDefinition = "NVARCHAR(50)")
    private String xepLoai; // "Xuất sắc", "Tốt", "Khá", "Trung bình", "Yếu", "Kém"

    @Column(length = 2000, columnDefinition = "NVARCHAR(2000)")
    private String ghiChuSinhVien;

    @Column(length = 2000, columnDefinition = "NVARCHAR(2000)")
    private String minhChung;

    @Column(length = 1000, columnDefinition = "NVARCHAR(1000)")
    private String nhanXetAdmin;

    @Column(nullable = false, length = 30)
    private String trangThai; // "CHO_DUYET", "DA_DUYET", "TU_CHOI"

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime ngayNop = LocalDateTime.now();

    private LocalDateTime ngayDuyet;

    @Column(length = 100, columnDefinition = "NVARCHAR(100)")
    private String nguoiDuyet;
}
