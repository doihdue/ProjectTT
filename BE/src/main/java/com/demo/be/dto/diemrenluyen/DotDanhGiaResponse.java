package com.demo.be.dto.diemrenluyen;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DotDanhGiaResponse(
        Long id,
        String tenDot,
        Integer hocKy,
        String namHoc,
        LocalDate ngayBatDau,
        LocalDate ngayKetThuc,
        String trangThai,
        String ghiChu,
        LocalDateTime thoiGianTao,
        long soLuongPhieu
) {}
