package com.demo.be.repository.impl;

import com.demo.be.dto.thongke.ThongKeKhoaResponse;
import com.demo.be.dto.thongke.ThongKeXepLoaiResponse;
import com.demo.be.repository.DiemRenLuyenRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DiemRenLuyenRepositoryImpl implements DiemRenLuyenRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @SuppressWarnings("unchecked")
    public List<ThongKeKhoaResponse> thongKeDrlTheoKhoaMapping() {
        // Viết câu lệnh Native SQL trực tiếp tại Repository
        String sql = "SELECT " +
                     "  CAST(k.id AS BIGINT) AS khoaId, " +
                     "  k.ma_khoa AS maKhoa, " +
                     "  k.ten_khoa AS tenKhoa, " +
                     "  CAST(COUNT(DISTINCT sv.id) AS BIGINT) AS tongSinhVien, " +
                     "  CAST(COUNT(d.id) AS BIGINT) AS soPhieuDaNop, " +
                     "  CAST(COUNT(CASE WHEN d.trang_thai = 'DA_DUYET' THEN 1 END) AS BIGINT) AS soPhieuDaDuyet, " +
                     "  CAST(ROUND(AVG(CAST(COALESCE(d.tong_diem_admin, d.tong_diem_sv, 0) AS FLOAT)), 1) AS FLOAT) AS diemTrungBinh, " +
                     "  CAST(COALESCE(MAX(COALESCE(d.tong_diem_admin, d.tong_diem_sv, 0)), 0) AS INT) AS diemCaoNhat " +
                     "FROM khoa k " +
                     "LEFT JOIN lop l ON l.khoa_id = k.id " +
                     "LEFT JOIN sinh_vien sv ON sv.lop_id = l.id " +
                     "LEFT JOIN diem_ren_luyen d ON d.sinh_vien_id = sv.id " +
                     "GROUP BY k.id, k.ma_khoa, k.ten_khoa " +
                     "ORDER BY k.id ASC";

        // Thực thi Native Query bằng EntityManager và ánh xạ tự động qua @SqlResultSetMapping ("ThongKeKhoaMapping")
        return entityManager.createNativeQuery(sql, "ThongKeKhoaMapping")
                            .getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ThongKeXepLoaiResponse> thongKeXepLoaiMapping() {
        String sql = "SELECT " +
                     "  d.xep_loai AS xepLoai, " +
                     "  CAST(COUNT(d.id) AS BIGINT) AS soLuong " +
                     "FROM diem_ren_luyen d " +
                     "WHERE d.xep_loai IS NOT NULL AND d.xep_loai <> '' " +
                     "GROUP BY d.xep_loai";

        // Thực thi Native Query bằng EntityManager và ánh xạ tự động qua @SqlResultSetMapping ("ThongKeXepLoaiMapping")
        return entityManager.createNativeQuery(sql, "ThongKeXepLoaiMapping")
                            .getResultList();
    }
}
