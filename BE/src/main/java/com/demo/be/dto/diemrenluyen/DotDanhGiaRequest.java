package com.demo.be.dto.diemrenluyen;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record DotDanhGiaRequest(
        @NotBlank(message = "Tên đợt đánh giá không được để trống")
        String tenDot,

        @NotNull(message = "Học kỳ không được để trống")
        @Min(value = 1, message = "Học kỳ phải từ 1 đến 3")
        @Max(value = 3, message = "Học kỳ phải từ 1 đến 3")
        Integer hocKy,

        @NotBlank(message = "Năm học không được để trống")
        String namHoc,

        LocalDate ngayBatDau,
        LocalDate ngayKetThuc,
        String trangThai, // "DANG_MO", "DONG"
        String ghiChu
) {}
