package com.demo.be.dto.diemrenluyen;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record DotDanhGiaRequest(
        @NotBlank(message = "Tên đợt đánh giá không được để trống")
        @Size(max = 150, message = "Tên đợt đánh giá tối đa 150 ký tự")
        String tenDot,

        @NotNull(message = "Học kỳ không được để trống")
        @Min(value = 1, message = "Học kỳ phải từ 1 đến 3")
        @Max(value = 3, message = "Học kỳ phải từ 1 đến 3")
        Integer hocKy,

        @NotBlank(message = "Năm học không được để trống")
        @Pattern(regexp = "^\\d{4}-\\d{4}$", message = "Năm học phải theo đúng định dạng YYYY-YYYY (ví dụ: 2026-2027)")
        String namHoc,

        @NotNull(message = "Thời gian bắt đầu mở đợt không được để trống")
        LocalDate ngayBatDau,

        @NotNull(message = "Hạn chót kết thúc đợt không được để trống")
        LocalDate ngayKetThuc,

        String trangThai, // "DANG_MO", "DONG"
        String ghiChu
) {
    @AssertTrue(message = "Thời gian kết thúc đợt phải sau ngày bắt đầu mở đợt")
    public boolean isNgayKetThucHopLe() {
        if (ngayBatDau == null || ngayKetThuc == null) {
            return true;
        }
        return ngayKetThuc.isAfter(ngayBatDau);
    }

    @AssertTrue(message = "Năm kết thúc phải sau năm bắt đầu đúng 1 năm (ví dụ: 2026-2027)")
    public boolean isNamHocHopLe() {
        if (namHoc == null || !namHoc.trim().matches("^\\d{4}-\\d{4}$")) {
            return true;
        }
        String[] parts = namHoc.trim().split("-");
        try {
            int startYear = Integer.parseInt(parts[0]);
            int endYear = Integer.parseInt(parts[1]);
            return endYear == startYear + 1;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
