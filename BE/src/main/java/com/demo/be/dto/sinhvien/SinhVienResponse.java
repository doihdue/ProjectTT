package com.demo.be.dto.sinhvien;

import java.time.LocalDate;

public record SinhVienResponse(
        Long id,
        String mssv,
        String hoTen,
        LocalDate ngaySinh,
        String gioiTinh,
        String email,
        String soDienThoai,
        String diaChi,
        LocalDate ngayNhapHoc,
        Long lopId,
        String lopTen,
        Long khoaId,
        String khoaTen,
        String khoaHoc,
        boolean active
) {
}
