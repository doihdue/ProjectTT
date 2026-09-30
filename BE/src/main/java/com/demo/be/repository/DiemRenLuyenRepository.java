package com.demo.be.repository;

import com.demo.be.model.DiemRenLuyen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DiemRenLuyenRepository extends JpaRepository<DiemRenLuyen, Long>, DiemRenLuyenRepositoryCustom {

    List<DiemRenLuyen> findBySinhVien_MssvOrderByNgayNopDesc(String mssv);

    List<DiemRenLuyen> findBySinhVien_IdOrderByNgayNopDesc(Long sinhVienId);

    Optional<DiemRenLuyen> findBySinhVien_MssvAndHocKyAndNamHoc(String mssv, Integer hocKy, String namHoc);

    Optional<DiemRenLuyen> findBySinhVien_MssvAndDotDanhGia_Id(String mssv, Long dotDanhGiaId);

    long countByDotDanhGia_Id(Long dotDanhGiaId);

    List<DiemRenLuyen> findByDotDanhGia_Id(Long dotDanhGiaId);

    @Query("SELECT d FROM DiemRenLuyen d " +
           "JOIN d.sinhVien sv " +
           "LEFT JOIN sv.lop l " +
           "LEFT JOIN l.khoa k " +
           "LEFT JOIN d.dotDanhGia dot " +
           "WHERE (:dotDanhGiaId IS NULL OR dot.id = :dotDanhGiaId) " +
           "AND (:trangThai IS NULL OR :trangThai = '' OR d.trangThai = :trangThai) " +
           "AND (:hocKy IS NULL OR d.hocKy = :hocKy) " +
           "AND (:namHoc IS NULL OR :namHoc = '' OR d.namHoc = :namHoc) " +
           "AND (:khoaId IS NULL OR k.id = :khoaId) " +
           "AND (:lopId IS NULL OR l.id = :lopId) " +
           "AND (:keyword IS NULL OR :keyword = '' OR LOWER(sv.mssv) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(sv.hoTen) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY d.ngayNop DESC")
    List<DiemRenLuyen> findByFilters(
            @Param("dotDanhGiaId") Long dotDanhGiaId,
            @Param("trangThai") String trangThai,
            @Param("hocKy") Integer hocKy,
            @Param("namHoc") String namHoc,
            @Param("khoaId") Long khoaId,
            @Param("lopId") Long lopId,
            @Param("keyword") String keyword
    );

    long countByTrangThai(String trangThai);

    long countByXepLoai(String xepLoai);

    void deleteBySinhVien_Id(Long sinhVienId);


    /**
     * API Native Query: Thống kê số lượng sinh viên theo từng xếp loại rèn luyện
     */
    @Query(value = "SELECT d.xep_loai AS xepLoai, COUNT(d.id) AS soLuong " +
                   "FROM diem_ren_luyen d " +
                   "WHERE d.xep_loai IS NOT NULL AND d.xep_loai <> '' " +
                   "GROUP BY d.xep_loai",
           nativeQuery = true)
    List<Object[]> thongKeXepLoaiNative();
}

