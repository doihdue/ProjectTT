package com.demo.be.dto.sinhvien;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record SinhVienRequest(
        @Size(max = 20, message = "MSSV must be at most 20 characters") String mssv,
        @NotBlank(message = "Ho ten is required") @Size(max = 150, message = "Ho ten must be at most 150 characters") String hoTen,
        LocalDate ngaySinh,
        @Size(max = 10, message = "Gioi tinh must be at most 10 characters") String gioiTinh,
        @Size(max = 150, message = "Email must be at most 150 characters") String email,
        @Size(max = 20, message = "So dien thoai must be at most 20 characters") String soDienThoai,
        @Size(max = 255, message = "Dia chi must be at most 255 characters") String diaChi,
        LocalDate ngayNhapHoc,
        @NotNull(message = "Lop id is required") Long lopId,
        Boolean active
) {
}
