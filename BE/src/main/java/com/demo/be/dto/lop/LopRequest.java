package com.demo.be.dto.lop;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record LopRequest(
        @Size(max = 20, message = "Mã lớp tối đa 20 ký tự") String maLop,
        @NotBlank(message = "Tên lớp không được để trống") @Size(max = 150, message = "Tên lớp tối đa 150 ký tự") String tenLop,
        @NotBlank(message = "Niên khóa không được để trống")
        @Pattern(regexp = "^\\d{4}\\s*[-–—]\\s*\\d{4}$", message = "Niên khóa phải có định dạng YYYY-YYYY (ví dụ: 2022-2026)")
        @Size(max = 20, message = "Niên khóa tối đa 20 ký tự")
        String nienKhoa,
        @NotNull(message = "Sĩ số tối đa không được để trống") @Positive(message = "Sĩ số tối đa phải lớn hơn 0") Integer siSoToiDa,
        @NotNull(message = "Vui lòng chọn Khoa trực thuộc") Long khoaId,
        Boolean active
) {
    @AssertTrue(message = "Năm bắt đầu phải nhỏ hơn năm kết thúc")
    public boolean isNienKhoaHopLe() {
        if (nienKhoa == null || !nienKhoa.trim().matches("^\\d{4}\\s*[-–—]\\s*\\d{4}$")) {
            return true;
        }
        String[] parts = nienKhoa.trim().split("[-–—]");
        try {
            int startYear = Integer.parseInt(parts[0].trim());
            int endYear = Integer.parseInt(parts[1].trim());
            return startYear < endYear;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
